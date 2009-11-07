/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

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


/**
 * @author Christopher Deckers
 */
abstract class MessagingInterface {

  protected static final boolean IS_DEBUGGING_MESSAGES = Boolean.parseBoolean(System.getProperty("nativeswing.interface.debug.printmessages"));

  public MessagingInterface(boolean isNativeSide) {
    this.isNativeSide = isNativeSide;
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
      System.err.println(">RUN: " + message.getID() + ", " + message);
    }
    CommandResultMessage commandResultMessage;
    if(message instanceof CommandMessage) {
      CommandMessage commandMessage = (CommandMessage)message;
      Object result = null;
      Throwable throwable = null;
      if(message.isValid()) {
        try {
          result = commandMessage.runCommand();
        } catch(Throwable t) {
          throwable = t;
        }
      }
      if(commandMessage.isSyncExec()) {
        commandResultMessage = new CommandResultMessage(commandMessage.getID(), result, throwable);
        asyncSend(commandResultMessage);
      } else {
        if(throwable != null) {
          throwable.printStackTrace();
        }
        commandResultMessage = new CommandResultMessage(message.getID(), result, throwable);
      }
    } else {
      commandResultMessage = new CommandResultMessage(message.getID(), null, null);
      if(message.isSyncExec()) {
        asyncSend(commandResultMessage);
      }
    }
    if(IS_DEBUGGING_MESSAGES) {
      System.err.println("<RUN: " + message.getID());
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
      MessagingInterface messagingInterface = NativeInterface.getMessagingInterface(!isOriginatorNativeSide);
      Thread thread = (Thread)messagingInterface.syncThreadRegistry.get(instanceID);
      messagingInterface.syncThreadRegistry.remove(instanceID);
      if(thread == null) {
        return null;
      }
      synchronized(thread) {
        messagingInterface.syncThreadRegistry.add(args[1], instanceID);
        thread.notify();
      }
      return null;
    }
  }

  private static class CM_asyncExec extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      Message message = (Message)args[1];
      boolean isOriginatorNativeSide = (Boolean)args[2];
      message.setSyncExec(false);
      MessagingInterface messagingInterface = NativeInterface.getMessagingInterface(!isOriginatorNativeSide);
      CM_asyncExecResponse asyncExecResponse = new CM_asyncExecResponse();
      asyncExecResponse.setArgs(args[0], messagingInterface.runMessage(message), messagingInterface.isNativeSide);
      messagingInterface.asyncSend(asyncExecResponse);
      return null;
    }
  }

  private ObjectRegistry syncThreadRegistry = new ObjectRegistry();

  private Object nonUISyncExec(Message message) {
    Thread thread = Thread.currentThread();
    final int instanceID = syncThreadRegistry.add(Thread.currentThread());
    CM_asyncExec asyncExec = new CM_asyncExec();
    asyncExec.setArgs(instanceID, message, isNativeSide);
    asyncSend(asyncExec);
    synchronized(thread) {
      while(syncThreadRegistry.get(instanceID) instanceof Thread) {
        try {
          thread.wait();
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
    message.computeId(!isNativeSide);
    if(!isUIThread()) {
      return nonUISyncExec(message);
    }
    synchronized(LOCK) {
      message.setUI(true);
      message.setSyncExec(true);
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
            if(commandResultMessage.getOriginalID() != message.getID()) {
              commandResultMessageList.add(commandResultMessage);
              commandResultMessage = null;
            } else {
              break;
            }
          } else {
            synchronized(RECEIVER_LOCK) {
              boolean isFirst = true;
              while(receivedMessageList.isEmpty()) {
                if(!isFirst) {
                  isFirst = true;
                  if(isNativeSide) {
                    // Sometimes, AWT is synchronously waiting for the native side to pump some event.
                    // The native side is currently waiting, so we set a timeout and do some pumping.
                    NativeInterface.getDisplay().readAndDispatch();
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
                if(isNativeSide) {
                  RECEIVER_LOCK.wait(500);
                } else {
                  // The Mac OS case is very rare, so we set a long timeout.
                  RECEIVER_LOCK.wait(5000);
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
      System.err.println("<USE: " + commandResultMessage.getID());
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
    message.computeId(!isNativeSide);
    message.setUI(isUIThread());
    message.setSyncExec(false);
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
      System.err.println((message.isSyncExec()? "SENDS": "SENDA") + ": " + message.getID() + ", " + message);
    }
    writeMessageToChannel(message);
  }

  protected abstract void writeMessageToChannel(Message message) throws IOException;

  protected abstract Message readMessageFromChannel() throws IOException, ClassNotFoundException;

  private void printFailedInvocation(Message message) {
    System.err.println("Failed messaging: " + message);
  }

  protected void createReceiverThread(final boolean exitOnEndOfStream) {
    Thread receiverThread = new Thread("NativeSwing Receiver - " + (isNativeSide()? "SWT": "Swing")) {
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
                System.exit(0);
              }
              e.printStackTrace();
              try {
                isRespawned = NativeInterface.notifyKilled();
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
              Thread thread = (Thread)syncThreadRegistry.get(instanceID);
              if(thread != null) {
                synchronized(thread) {
                  thread.notify();
                }
              }
            }
            if(isRespawned) {
              NativeInterface.notifyRespawned();
            }
          }
          if(message != null) {
            if(!message.isUI()) {
              final Message message_ = message;
              new Thread("NativeSwing Async") {
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
