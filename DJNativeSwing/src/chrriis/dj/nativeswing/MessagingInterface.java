/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;

import chrriis.common.ObjectRegistry;

/**
 * @author Christopher Deckers
 */
abstract class MessagingInterface {

  private static final boolean IS_DEBUGGING_MESSAGES = Boolean.parseBoolean(System.getProperty("nativeswing.interface.debug.printmessages"));
  
  private static class CommandResultMessage extends Message {

    private int originalID;
    private Object result;
    private Throwable exception;

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
  
  private ObjectOutputStream oos;
  private ObjectInputStream ois;

  private boolean isAlive = true;
  
  public boolean isAlive() {
    return isAlive;
  }
  
  public void destroy() {
    isAlive = false;
    try {
      ois.close();
    } catch(Exception e) {
    }
  }
  
  public MessagingInterface(final Socket socket, final boolean exitOnEndOfStream) {
    try {
      oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()) {
        @Override
        public synchronized void write(int b) throws IOException {
          super.write(b);
          oosByteCount++;
        }
        @Override
        public synchronized void write(byte[] b, int off, int len) throws IOException {
          super.write(b, off, len);
          oosByteCount += len;
        }
      });
      oos.flush();
      ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
    Thread receiverThread = new Thread("NativeSwing Receiver") {
      @Override
      public void run() {
        while(isAlive) {
          Message message = null;
          try {
            message = readMessage();
          } catch(Exception e) {
            boolean isRespawned = false;
            if(isAlive) {
              isAlive = false;
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
                      processReceivedMessages();
                    }
                  });
                }
              }
            }
          }
        }
        try {
          oos.close();
        } catch(Exception e) {
        }
        try {
          ois.close();
        } catch(Exception e) {
        }
        try {
          socket.close();
        } catch(Exception e) {
        }
      }
    };
    receiverThread.setDaemon(true);
    receiverThread.start();
  }
  
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
  
  public abstract boolean isUIThread();
  
  public void checkUIThread() {
    if(!isUIThread()) {
      if(NativeInterface.isNativeSide()) {
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
      MessagingInterface messagingInterface = NativeInterface.getMessagingInterface();
      int instanceID = (Integer)args[0];
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
      message.setSyncExec(false);
      MessagingInterface messagingInterface = NativeInterface.getMessagingInterface();
      CM_asyncExecResponse asyncExecResponse = new CM_asyncExecResponse();
      asyncExecResponse.setArgs(args[0], messagingInterface.runMessage(message));
      messagingInterface.asyncSend(asyncExecResponse);
      return null;
    }
  }
  
  private ObjectRegistry syncThreadRegistry = new ObjectRegistry();
  
  private Object nonUISyncExec(Message message) {
    Thread thread = Thread.currentThread();
    final int instanceID = syncThreadRegistry.add(Thread.currentThread());
    CM_asyncExec asyncExec = new CM_asyncExec();
    asyncExec.setArgs(instanceID, message);
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
  
  private void printFailedInvocation(Message message) {
    System.err.println("Failed messaging: " + message);
  }
  
  private final Object LOCK = new Object();
  
  public Object syncSend(Message message) {
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
              if(receivedMessageList.isEmpty()) {
                isWaitingResponse = true;
                RECEIVER_LOCK.wait();
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
    message.setUI(isUIThread());
    message.setSyncExec(false);
    try {
      writeMessage(message);
    } catch(Exception e) {
      throw new IllegalStateException(e);
    }
  }
  
  private static final int OOS_RESET_THRESHOLD;
  
  static {
    String maxByteCountProperty = System.getProperty("nativeswing.interface.streamresetthreshold");
    if(maxByteCountProperty != null) {
      OOS_RESET_THRESHOLD = Integer.parseInt(maxByteCountProperty);
    } else {
      OOS_RESET_THRESHOLD = 500000;
    }
  }
  
  private int oosByteCount;
  
  private void writeMessage(Message message) throws IOException {
    if(!isAlive()) {
      printFailedInvocation(message);
      return;
    }
    if(IS_DEBUGGING_MESSAGES) {
      System.err.println((message.isSyncExec()? "SYNDS": "SYNDA") + ": " + message.getID() + ", " + message);
    }
    synchronized(oos) {
      oos.writeUnshared(message);
      oos.flush();
      // Messages are cached, so we need to reset() from time to time to clean the cache, or else we get an OutOfMemoryError.
      if(oosByteCount > OOS_RESET_THRESHOLD) {
        oos.reset();
        oosByteCount = 0;
      }
    }
  }
  
  private Message readMessage() throws IOException, ClassNotFoundException {
    Object o = MessagingInterface.this.ois.readUnshared();
    if(o instanceof Message) {
      Message message = (Message)o;
      if(IS_DEBUGGING_MESSAGES) {
        System.err.println("RECV: " + message.getID() + ", " + message);
      }
      return message;
    }
    System.err.println("Unknown message: " + o);
    return null;
  }
  
}
