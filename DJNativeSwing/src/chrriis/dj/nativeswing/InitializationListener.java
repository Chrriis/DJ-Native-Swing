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
 * @author Christopher Deckers
 */
public interface InitializationListener extends EventListener {

  public void objectInitialized(InitializationEvent e);
  
}
