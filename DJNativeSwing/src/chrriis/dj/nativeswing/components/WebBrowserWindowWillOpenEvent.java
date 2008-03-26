/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components;


/**
 * This event is sent when a new window needs to be created. It allows to consume the event to prevent the opening, or to give a different web browser object to open the content elsewhere (for example in a tab).
 * If this event is not consumed, then it will be followed by a window opening event, where the appearance will be defined.
 * Note that navigation events can happen after this event, but may be before or after the opening event.
 * @author Christopher Deckers
 */
public class WebBrowserWindowWillOpenEvent extends WebBrowserEvent {

  private JWebBrowser newWebBrowser;

  public WebBrowserWindowWillOpenEvent(JWebBrowser webBrowser, JWebBrowser newWebBrowser) {
    super(webBrowser);
    this.newWebBrowser = newWebBrowser;
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
