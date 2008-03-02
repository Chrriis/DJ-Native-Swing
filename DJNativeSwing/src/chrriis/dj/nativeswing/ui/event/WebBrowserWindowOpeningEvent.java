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

import chrriis.dj.nativeswing.ui.JWebBrowser;

/**
 * This event is sent after the window was created (creation event). The new web browser will have its appearance set (bars visibility, bounds of the containing window if any).
 * This new web browser may or may not have received navigation events.
 * @author Christopher Deckers
 */
public class WebBrowserWindowOpeningEvent extends WebBrowserEvent {

  protected JWebBrowser newWebBrowser;
  protected Point location;
  protected Dimension size;

  public WebBrowserWindowOpeningEvent(JWebBrowser webBrowser, JWebBrowser newWebBrowser, Point location, Dimension size) {
    super(webBrowser);
    this.newWebBrowser = newWebBrowser;
    this.location = location;
    this.size = size;
  }
  
  public JWebBrowser getNewWebBrowser() {
    return newWebBrowser;
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
  
}
