/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui.event;

import java.util.EventListener;

/**
 * @author Christopher Deckers
 */
public interface FlashPlayerListener extends EventListener {

  /**
   * This event can be consumed.
   * Note that the URL should be read from the event as it is not yet set on the newWebBrowser component. 
   */
  public void windowOpening(FlashPlayerWindowOpeningEvent e);

}
