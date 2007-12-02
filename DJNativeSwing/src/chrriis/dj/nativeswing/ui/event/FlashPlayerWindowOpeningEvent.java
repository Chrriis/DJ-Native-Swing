/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui.event;

import java.awt.Dimension;
import java.awt.Point;

import chrriis.dj.nativeswing.ui.JFlashPlayer;
import chrriis.dj.nativeswing.ui.JWebBrowser;

/**
 * @author Christopher Deckers
 */
public class FlashPlayerWindowOpeningEvent extends FlashPlayerEvent {

  protected JWebBrowser newWebBrowser;
  protected String newURL;
  protected Point location;
  protected Dimension size;

  public FlashPlayerWindowOpeningEvent(JFlashPlayer flashPlayer, JWebBrowser newWebBrowser, String newURL, Point location, Dimension size) {
    super(flashPlayer);
    this.newWebBrowser = newWebBrowser;
    this.newURL = newURL;
    this.location = location;
    this.size = size;
  }
  
  public String getNewURL() {
    return newURL;
  }
  
  /**
   * @return the location, or null for default behavior.
   */
  public Point getLocation() {
    return location;
  }
  
  /**
   * @return the size, or null for default behavior.
   */
  public Dimension getSize() {
    return size;
  }
  
  public JWebBrowser getNewWebBrowser() {
    return newWebBrowser;
  }
  
  /**
   * Set a different web browser. Note that null is not allowed: to prevent the window from opening, use the consume() method.
   */
  public void setNewWebBrowser(JWebBrowser newWebBrowser) {
    if(newWebBrowser == null) {
      throw new IllegalArgumentException("The new web browser cannot be null. To prevent the window to open, use the consume() method.");
    }
    this.newWebBrowser = newWebBrowser;
  }
  
  public void consume() {
    this.newWebBrowser = null;
  }
  
  public boolean isConsumed() {
    return newWebBrowser == null;
  }
  
}
