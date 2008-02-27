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
 * @author Christopher Deckers
 */
public abstract class Message implements Serializable {

  public static class EmptyMessage extends Message {
  }
  
  private static int nextID = 1;
  
  private int id = nextID++;
  private boolean isSyncExec;
  private boolean isUI = true;
  
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
  
  public void asyncExec() {
    NativeInterfaceHandler.asyncExec(this);
  }
  
  public Object syncExec() {
    return NativeInterfaceHandler.syncExec(this);
  }
  
  /**
   * Valid message will be executed.
   */
  public boolean isValid() {
    return true;
  }
  
  @Override
  public String toString() {
    return getClass().getName();
  }
  
}