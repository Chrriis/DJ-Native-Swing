/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.io.Serializable;

/**
 * The superclass of all the messages that are exchanged at the native interface.
 * @author Christopher Deckers
 */
public class Message implements Serializable {

  private static int nextID = 1;
  
  private int id;
  private boolean isSyncExec;
  private boolean isUI = true;
  
  /**
   * Create an empty message.
   */
  public Message() {
    if(NativeInterface.isNativeSide()) {
      id = -nextID++;
    } else {
      id = nextID++;
    }
  }
  
  void setUI(boolean isUI) {
    this.isUI = isUI;
  }
  
  boolean isUI() {
    return isUI;
  }

  int getID() {
    return id;
  }
  
  void setSyncExec(boolean isSyncExec) {
    this.isSyncExec = isSyncExec;
  }
  
  boolean isSyncExec() {
    return isSyncExec;
  }
  
  /**
   * Send that message asynchronously.
   */
  public void asyncSend() {
    NativeInterface.asyncSend(this);
  }
  
  /**
   * Send that message synchronously, potentially returning a result if the message type allows that.
   * @return the result if any.
   */
  public Object syncSend() {
    return NativeInterface.syncSend(this);
  }
  
  /**
   * Indicate whether the message is valid. This is called before interpreting it to give a chance for the message to prevent its interpretation.
   * @return true if the message is valid and should be interpreted, false otherwise.
   */
  protected boolean isValid() {
    return true;
  }
  
  @Override
  public String toString() {
    String name = getClass().getName();
    if(name.startsWith("chrriis.dj.nativeswing.")) {
      name = name.substring("chrriis.dj.nativeswing.".length());
    }
    return name;
  }
  
}