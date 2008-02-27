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

import chrriis.common.Registry;

/**
 * @author Christopher Deckers
 */
abstract class MessagingInterface {

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
    
  }

  private Object RECEIVER_LOCK = new Object();
  
  private ObjectOutputStream oos;
  private ObjectInputStream ois;

  public MessagingInterface(final Socket socket, final boolean exitOnEndOfStream) {
    try {
      oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
      oos.flush();
      ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
    Thread receiverThread = new Thread("NativeSwing Receiver") {
      @Override
      public void run() {
        while(true) {
          Message message = null;
          try {
            message = readMessage();
          } catch(Exception e) {
            if(exitOnEndOfStream) {
              System.exit(0);
            }
            e.printStackTrace();
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
    if(message instanceof CommandMessage) {
      CommandMessage commandMessage = (CommandMessage)message;
      Object result = null;
      Throwable throwable = null;
      if(message.isValid()) {
        try {
          result = commandMessage.run();
        } catch(Throwable t) {
          throwable = t;
        }
      }
      if(commandMessage.isSyncExec()) {
        CommandResultMessage commandResultMessage = new CommandResultMessage(commandMessage.getID(), result, throwable);
        asyncExec(commandResultMessage);
        return commandResultMessage;
      }
      if(throwable != null) {
        throwable.printStackTrace();
      }
      return new CommandResultMessage(message.getID(), result, throwable);
    }
    CommandResultMessage commandResultMessage = new CommandResultMessage(message.getID(), null, null);
    if(message.isSyncExec()) {
      asyncExec(commandResultMessage);
    }
    return commandResultMessage;
  }
  
  protected abstract void asyncUIExec(Runnable runnable);
  
  public abstract boolean isUIThread();
  
  public void checkUIThread() {
    if(!isUIThread()) {
      throw new IllegalStateException("This call must happen in the UI thread!");
    }
  }
  
  private List<Message> receivedMessageList = new LinkedList<Message>();
  private boolean isWaitingResponse;
  
  private static class CM_asyncExecResponse extends CommandMessage {
    @Override
    public Object run() throws Exception {
      MessagingInterface messagingInterface = NativeInterfaceHandler.getMessagingInterface();
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
    public Object run() throws Exception {
      Message message = (Message)args[1];
      message.setSyncExec(false);
      new CM_asyncExecResponse().asyncExecArgs(args[0], NativeInterfaceHandler.getMessagingInterface().runMessage(message));
      return null;
    }
  }
  
  private Registry syncThreadRegistry = new Registry();
  
  private Object nonUISyncExec(Message message) {
    Thread thread = Thread.currentThread();
    final int instanceID = syncThreadRegistry.add(Thread.currentThread());
    new CM_asyncExec().asyncExecArgs(instanceID, message);
    synchronized(thread) {
      while(syncThreadRegistry.get(instanceID) instanceof Thread) {
        try {
          thread.wait();
        } catch(Exception e) {
        }
      }
      CommandResultMessage commandResultMessage = (CommandResultMessage)syncThreadRegistry.get(instanceID);
      syncThreadRegistry.remove(instanceID);
      return processCommandResult(commandResultMessage);
    }
  }
  
  public Object syncExec(Message message) {
    if(!isUIThread()) {
      return nonUISyncExec(message);
    }
    message.setUI(true);
    message.setSyncExec(true);
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
            isWaitingResponse = true;
            RECEIVER_LOCK.wait();
            isWaitingResponse = false;
          }
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
  
  private Object processCommandResult(CommandResultMessage commandResultMessage) {
    Throwable exception = commandResultMessage.getException();
    if(exception != null) {
      if(exception instanceof RuntimeException) {
        throw (RuntimeException)exception;
      }
      throw new RuntimeException(exception);
    }
    return commandResultMessage.getResult();
  }
  
  public void asyncExec(Message message) {
    message.setUI(isUIThread());
    message.setSyncExec(false);
    try {
      writeMessage(message);
    } catch(Exception e) {
      throw new IllegalStateException(e);
    }
  }
  
  private void writeMessage(Message message) throws IOException {
    synchronized(oos) {
//      System.err.println("SEND: " + message.getID() + ", " + message.getClass().getName());
      oos.writeObject(message);
      oos.flush();
    }
  }
  
  private Message readMessage() throws IOException, ClassNotFoundException {
    Object o = MessagingInterface.this.ois.readObject();
    if(o instanceof Message) {
      Message message = (Message)o;
//      System.err.println("RECV: " + message.getID() + ", " + message.getClass().getName());
      return message;
    }
    System.err.println("Unknown message: " + o);
    return null;
  }
  
}
