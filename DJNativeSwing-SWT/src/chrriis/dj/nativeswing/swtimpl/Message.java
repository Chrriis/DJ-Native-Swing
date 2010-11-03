/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

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
   * @param isTargetNativeSide true if the target is the native side, false otherwise.
   */
  public void asyncSend(boolean isTargetNativeSide) {
    NativeInterface.asyncSend(isTargetNativeSide, this);
  }

  /**
   * Send that message synchronously, potentially returning a result if the message type allows that.
   * @param isTargetNativeSide true if the target is the native side, false otherwise.
   * @return the result if any.
   */
  public Object syncSend(boolean isTargetNativeSide) {
    return NativeInterface.syncSend(isTargetNativeSide, this);
  }

  void computeID(boolean isTargetNativeSide) {
    if(id != 0) {
      return;
    }
    if(isTargetNativeSide) {
      id = nextID++;
    } else {
      id = -nextID++;
    }
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
    if(name.startsWith("chrriis.dj.nativeswing.swtimpl.")) {
      name = name.substring("chrriis.dj.nativeswing.swtimpl.".length());
    }
    return name;
  }

}