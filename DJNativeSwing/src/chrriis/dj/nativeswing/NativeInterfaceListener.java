/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.util.EventListener;

/**
 * A listener to notify of the various events of the native interface.
 * @author Christopher Deckers
 */
public interface NativeInterfaceListener extends EventListener {
  
  /**
   * Invoked when the native interface is initialized, which can happen only once.
   */
  public void nativeInterfaceInitialized();
  
  /**
   * Invoked when the native interface is opened, which can happen because of a user action or when the framework automatically re-opens the interface after an error closed it.
   */
  public void nativeInterfaceOpened();
  
  /**
   * Invoked when the native interface is closed, which can happen because of a user action or if the interface gets closed because of an error.
   */
  public void nativeInterfaceClosed();
  
}