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
   * Invoked when the native interface is restarted after an error happening on the native side (peer VM).
   */
  public void nativeInterfaceRestarted();

}