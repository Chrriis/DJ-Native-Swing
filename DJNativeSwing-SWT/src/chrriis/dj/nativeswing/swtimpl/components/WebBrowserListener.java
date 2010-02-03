/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.util.EventListener;

/**
 * @author Christopher Deckers
 */
public interface WebBrowserListener extends EventListener {

  /**
   * This event is sent to the browser that originated the event, and can be consumed.
   */
  public void windowWillOpen(WebBrowserWindowWillOpenEvent e);

  /**
   * This event is sent to the browser that originated the event.
   */
  public void windowOpening(WebBrowserWindowOpeningEvent e);

  /**
   * The system is closing the browser component, so all ancestors or related components can be closed upon receiving this notification.
   * If the window ancestor is a JWebBrowserWindow, the system will close it automatically.
   */
  public void windowClosing(WebBrowserEvent e);

  /**
   * The event can be consumed.
   */
  public void locationChanging(WebBrowserNavigationEvent e);
  public void locationChanged(WebBrowserNavigationEvent e);
  /**
   * Invoked when the location was changing but one of the listener returned false to block the change.
   */
  public void locationChangeCanceled(WebBrowserNavigationEvent e);

  public void loadingProgressChanged(WebBrowserEvent e);

  public void titleChanged(WebBrowserEvent e);
  public void statusChanged(WebBrowserEvent e);

  /**
   * The web browser can invoke special commands to the application simply by calling through Javascript:<br/>
   * <code>window.location = 'command://' + encodeURIComponent('somecommand') + '&' + encodeURIComponent('param1') + '&' + encodeURIComponent('param2') + ...</code><br/>
   * or as a static HREF link.
   */
  public void commandReceived(WebBrowserCommandEvent e);

}
