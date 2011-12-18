/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.core;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;

import chrriis.common.ObjectRegistry;
import chrriis.dj.nativeswing.swtimpl.CommandMessage;
import chrriis.dj.nativeswing.swtimpl.Message;
import chrriis.dj.nativeswing.swtimpl.NSSystemPropertySWT;


/**
 * @author Christopher Deckers
 */
abstract class MessagingInterface {

  protected static final boolean IS_DEBUGGING_MESSAGES = Boolean.parseBoolean(NSSystemPropertySWT.INTERFACE_DEBUG_PRINTMESSAGES.get());

  private int pid;

  public MessagingInterface(boolean isNativeSide, int pid) {
    this.isNativeSide = isNativeSide;
    this.pid = pid;
  }

  public abstract void destroy();

  public abstract boolean isUIThread();

  private volatile boolean isAlive;

  protected void setAlive(boolean isAlive) {
    this.isAlive = isAlive;
  }

  public boolean isAlive() {
    return isAlive;
  }

  protected void initialize(boolean exitOnEndOfStream) {
    setAlive(true);
    openChannel();
    createReceiverThread(exitOnEndOfStream);
  }

  private static class CommandResultMessage extends Message {

    private final int originalID;
    private final Object result;
    private final Throwable exception;

    CommandResultMessage(int originalID, Object result, Throwable exception) {
      this.originalID = originalID;
      this.result = result;
      this.exception = exception;
    }

    int getOriginalID() {
      return originalID;
    }

    public Object getResult() {
      return result;
    }

    public Throwable getException() {
      return exception;
    }

    @Override
    public String toString() {
      return super.toString() + "(" + originalID + ")";
    }

  }

  private Object RECEIVER_LOCK = new Object();

  private CommandResultMessage processReceivedMessages() {
    while(true) {
      Message message;
      synchronized(RECEIVER_LOCK) {
        if(receivedMessageList.isEmpty()) {
          return null;
        }
        message = receivedMessageList.remove(0);
      }
      if(message instanceof CommandResultMessage) {
        return (CommandResultMessage)message;
      }
      runMessage(message);
    }
  }

  private CommandResultMessage runMessage(Message message) {
    if(IS_DEBUGGING_MESSAGES) {
      System.err.println(">RUN: " + SWTNativeInterface.getMessageID(message) + ", " + message);
    }
    CommandResultMessage commandResultMessage;
    if(message instanceof CommandMessage) {
      CommandMessage commandMessage = (CommandMessage)message;
      Object result = null;
      Throwable throwable = null;
      if(SWTNativeInterface.isMessageValid(message)) {
        try {
          result = SWTNativeInterface.runMessageCommand(commandMessage);
        } catch(Throwable t) {
          throwable = t;
        }
      }
      if(SWTNativeInterface.isMessageSyncExec(commandMessage)) {
        commandResultMessage = new CommandResultMessage(SWTNativeInterface.getMessageID(commandMessage), result, throwable);
        asyncSend(commandResultMessage);
      } else {
        if(throwable != null) {
          throwable.printStackTrace();
        }
        commandResultMessage = new CommandResultMessage(SWTNativeInterface.getMessageID(message), result, throwable);
      }
    } else {
      commandResultMessage = new CommandResultMessage(SWTNativeInterface.getMessageID(message), null, null);
      if(SWTNativeInterface.isMessageSyncExec(message)) {
        asyncSend(commandResultMessage);
      }
    }
    if(IS_DEBUGGING_MESSAGES) {
      System.err.println("<RUN: " + SWTNativeInterface.getMessageID(message));
    }
    return commandResultMessage;
  }

  protected abstract void asyncUIExec(Runnable runnable);

  private final boolean isNativeSide;

  protected boolean isNativeSide() {
    return isNativeSide;
  }

  public void checkUIThread() {
    if(!isUIThread()) {
      if(isNativeSide()) {
        SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
        return;
      }
      throw new IllegalStateException("This call must happen in the AWT Event Dispatch Thread! Please refer to http://java.sun.com/docs/books/tutorial/uiswing/concurrency/index.html and http://java.sun.com/javase/6/docs/api/javax/swing/SwingUtilities.html#invokeLater(java.lang.Runnable)");
    }
  }

  private List<Message> receivedMessageList = new LinkedList<Message>();
  private boolean isWaitingResponse;

  private static class CM_asyncExecResponse extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      int instanceID = (Integer)args[0];
      boolean isOriginatorNativeSide = (Boolean)args[2];
      MessagingInterface messagingInterface = SWTNativeInterface.getInstance().getMessagingInterface(!isOriginatorNativeSide);
      ThreadLock threadLock = (ThreadLock)messagingInterface.syncThreadRegistry.get(instanceID);
      messagingInterface.syncThreadRegistry.remove(instanceID);
      if(threadLock == null) {
        return null;
      }
      synchronized(threadLock) {
        messagingInterface.syncThreadRegistry.add(args[1], instanceID);
        threadLock.notify();
      }
      return null;
    }
  }

  private static class CM_asyncExec extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      Message message = (Message)args[1];
      boolean isOriginatorNativeSide = (Boolean)args[2];
      SWTNativeInterface.setMessageSyncExec(message, false);
      MessagingInterface messagingInterface = SWTNativeInterface.getInstance().getMessagingInterface(!isOriginatorNativeSide);
      CM_asyncExecResponse asyncExecResponse = new CM_asyncExecResponse();
      SWTNativeInterface.setMessageArgs(asyncExecResponse, args[0], messagingInterface.runMessage(message), messagingInterface.isNativeSide());
      messagingInterface.asyncSend(asyncExecResponse);
      return null;
    }
  }

  private ObjectRegistry syncThreadRegistry = new ObjectRegistry();

  private static class ThreadLock {
  }

  private Object nonUISyncExec(Message message) {
    ThreadLock threadLock = new ThreadLock();
    final int instanceID = syncThreadRegistry.add(threadLock);
    CM_asyncExec asyncExec = new CM_asyncExec();
    SWTNativeInterface.setMessageArgs(asyncExec, instanceID, message, isNativeSide());
    asyncSend(asyncExec);
    synchronized(threadLock) {
      while(syncThreadRegistry.get(instanceID) instanceof ThreadLock) {
        try {
          threadLock.wait();
        } catch(Exception e) {
        }
        if(!isAlive()) {
          syncThreadRegistry.remove(instanceID);
          printFailedInvocation(message);
          return null;
        }
      }
      CommandResultMessage commandResultMessage = (CommandResultMessage)syncThreadRegistry.get(instanceID);
      syncThreadRegistry.remove(instanceID);
      return processCommandResult(commandResultMessage);
    }
  }

  private final Object LOCK = new Object();

  public Object syncSend(Message message) {
    SWTNativeInterface.computeMessageID(message, !isNativeSide());
    if(!isUIThread()) {
      return nonUISyncExec(message);
    }
    synchronized(LOCK) {
      SWTNativeInterface.setMessageUI(message, true);
      SWTNativeInterface.setMessageSyncExec(message, true);
      if(!isAlive()) {
        printFailedInvocation(message);
        return null;
      }
      CommandResultMessage commandResultMessage = null;
      try {
        writeMessage(message);
        List<CommandResultMessage> commandResultMessageList = new ArrayList<CommandResultMessage>();
        while(true) {
          commandResultMessage = processReceivedMessages();
          if(commandResultMessage != null) {
            if(commandResultMessage.getOriginalID() != SWTNativeInterface.getMessageID(message)) {
              commandResultMessageList.add(commandResultMessage);
              commandResultMessage = null;
            } else {
              break;
            }
          } else {
            synchronized(RECEIVER_LOCK) {
              boolean isFirst = true;
              while(receivedMessageList.isEmpty()) {
                if(!isAlive()) {
                  printFailedInvocation(message);
                  return null;
                }
                if(!isFirst) {
                  isFirst = true;
                  if(isNativeSide()) {
                    // Sometimes, AWT is synchronously waiting for the native side to pump some event.
                    // The native side is currently waiting, so we set a timeout and do some pumping.
                    SWTNativeInterface.getInstance().getDisplay().readAndDispatch();
                  } else {
                    // On Mac OS, under rare circumstances, we have a situation where SWT is waiting synchronously on AWT, while AWT is blocked here.
                    // We have to use a similar forced dispatching trick.
                    EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
                    AWTEvent nextEvent = eventQueue.peekEvent();
                    if(nextEvent != null) {
                      nextEvent = eventQueue.getNextEvent();
                      if(nextEvent != null) {
                        Method dispatchMethod = EventQueue.class.getDeclaredMethod("dispatchEvent", AWTEvent.class);
                        dispatchMethod.setAccessible(true);
                        dispatchMethod.invoke(eventQueue, nextEvent);
                      }
                    }
                  }
                }
                isFirst = false;
                isWaitingResponse = true;
                if(isNativeSide()) {
                  String timeout = NSSystemPropertySWT.INTERFACE_SYNCSEND_NATIVE_TIMEOUT.get();
                  if(timeout != null) {
                    RECEIVER_LOCK.wait(Long.parseLong(timeout));
                  } else {
                    RECEIVER_LOCK.wait(500);
                  }
                } else {
                  // The Mac OS case is very rare, so we set a long timeout.
                  String timeout = NSSystemPropertySWT.INTERFACE_SYNCSEND_LOCAL_TIMEOUT.get();
                  if(timeout != null) {
                    RECEIVER_LOCK.wait(Long.parseLong(timeout));
                  } else {
                    RECEIVER_LOCK.wait(5000);
                  }
                }
                isWaitingResponse = false;
              }
            }
          }
          if(!isAlive()) {
            printFailedInvocation(message);
            return null;
          }
        }
        synchronized(RECEIVER_LOCK) {
          if(!commandResultMessageList.isEmpty()) {
            receivedMessageList.addAll(0, commandResultMessageList);
          } else {
            if(!receivedMessageList.isEmpty()) {
              asyncUIExec(new Runnable() {
                public void run() {
                  processReceivedMessages();
                }
              });
            }
          }
        }
      } catch(Exception e) {
        throw new IllegalStateException(e);
      }
      return processCommandResult(commandResultMessage);
    }
  }

  private Object processCommandResult(CommandResultMessage commandResultMessage) {
    if(IS_DEBUGGING_MESSAGES) {
      System.err.println("<USE: " + SWTNativeInterface.getMessageID(commandResultMessage));
    }
    Throwable exception = commandResultMessage.getException();
    if(exception != null) {
//      if(exception instanceof RuntimeException) {
//        throw (RuntimeException)exception;
//      }
      throw new RuntimeException(exception);
    }
    return commandResultMessage.getResult();
  }

  public void asyncSend(Message message) {
    SWTNativeInterface.computeMessageID(message, !isNativeSide());
    SWTNativeInterface.setMessageUI(message, isUIThread());
    SWTNativeInterface.setMessageSyncExec(message, false);
    try {
      writeMessage(message);
    } catch(Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private void writeMessage(Message message) throws IOException {
    if(!isAlive()) {
      printFailedInvocation(message);
      return;
    }
    if(IS_DEBUGGING_MESSAGES) {
      System.err.println((SWTNativeInterface.isMessageSyncExec(message)? "SENDS": "SENDA") + ": " + SWTNativeInterface.getMessageID(message) + ", " + message);
    }
    writeMessageToChannel(message);
  }

  protected abstract void writeMessageToChannel(Message message) throws IOException;

  protected abstract Message readMessageFromChannel() throws IOException, ClassNotFoundException;

  private void printFailedInvocation(Message message) {
    System.err.println("Failed messaging: " + message);
  }

  protected void terminate() {
    System.exit(0);
  }

  protected int getPID() {
    return pid;
  }

  private void createReceiverThread(final boolean exitOnEndOfStream) {
    Thread receiverThread = new Thread("NativeSwing[" + pid + "] " + (isNativeSide()? "SWT": "Swing") + " Receiver") {
      @Override
      public void run() {
        while(MessagingInterface.this.isAlive()) {
          Message message = null;
          try {
            message = readMessageFromChannel();
          } catch(Exception e) {
            boolean isRespawned = false;
            if(MessagingInterface.this.isAlive()) {
              setAlive(false);
              if(exitOnEndOfStream) {
                terminate();
                return;
              }
              e.printStackTrace();
              try {
                isRespawned = SWTNativeInterface.getInstance().notifyKilled();
              } catch(Exception ex) {
                ex.printStackTrace();
              }
            }
            // Unlock all locked sync calls
            synchronized(RECEIVER_LOCK) {
              receivedMessageList.clear();
              RECEIVER_LOCK.notify();
            }
            for(int instanceID: syncThreadRegistry.getInstanceIDs()) {
              Object o = syncThreadRegistry.get(instanceID);
              if(o instanceof ThreadLock) {
                synchronized(o) {
                  o.notify();
                }
              }
            }
            if(isRespawned) {
              SWTNativeInterface.getInstance().notifyRespawned();
            }
          }
          if(message != null) {
            if(!SWTNativeInterface.isMessageUI(message)) {
              final Message message_ = message;
              new Thread("NativeSwing[" + getPID() + "] Non-UI Message [" + SWTNativeInterface.getMessageID(message) + "] Executor") {
                @Override
                public void run() {
                  runMessage(message_);
                }
              }.start();
            } else {
              synchronized(RECEIVER_LOCK) {
                receivedMessageList.add(message);
                if(isWaitingResponse) {
                  RECEIVER_LOCK.notify();
                } else if(receivedMessageList.size() == 1) {
                  asyncUIExec(new Runnable() {
                    public void run() {
                      CommandResultMessage commandResultMessage = processReceivedMessages();
                      if(commandResultMessage != null) {
                        synchronized(RECEIVER_LOCK) {
                          receivedMessageList.add(0, commandResultMessage);
                        }
                      }
                    }
                  });
                }
              }
            }
          }
        }
        closeChannel();
      }
    };
    receiverThread.setDaemon(true);
    receiverThread.start();
  }

  protected abstract void openChannel();

  protected abstract void closeChannel();

}
