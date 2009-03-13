/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import java.util.EventListener;

/**
 * A listener to notify of the various events of the native interface.
 * @author Christopher Deckers
 */
public interface NativeInterfaceListener extends EventListener {

  /**
   * Invoked when the native interface is initialized, which can happen only once. This event is not necessarily received in the UI thread.
   */
  public void nativeInterfaceInitialized();

  /**
   * Invoked when the native interface is opened, which can happen because of a user action or when the framework automatically re-opens the interface after an error closed it. This event is not necessarily received in the UI thread.
   */
  public void nativeInterfaceOpened();

  /**
   * Invoked when the native interface is closed, which can happen because of a user action or if the interface gets closed because of an error. Note that an error usually means that the user killed the process or that one of the native components crashed the process, which should be a rare condition. This event is not necessarily received in the UI thread.
   */
  public void nativeInterfaceClosed();

}