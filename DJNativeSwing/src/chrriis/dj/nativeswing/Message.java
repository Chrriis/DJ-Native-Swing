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
public class Message implements Serializable {

  private static int nextID = 1;
  
  private int id;
  private boolean isSyncExec;
  private boolean isUI = true;
  
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
  
  public void asyncExec() {
    NativeInterface.asyncExec(this);
  }
  
  public Object syncExec() {
    return NativeInterface.syncExec(this);
  }
  
  /**
   * Valid message will be executed.
   */
  public boolean isValid() {
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