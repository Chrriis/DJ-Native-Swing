/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui.event;

import chrriis.dj.nativeswing.ui.JWebBrowser;

/**
 * @author Christopher Deckers
 */
public class WebBrowserNavigationEvent extends WebBrowserEvent {

  protected String url;
  protected boolean isTopFrame;

  public WebBrowserNavigationEvent(JWebBrowser webBrowser, String newURL, boolean isTopFrame) {
    super(webBrowser);
    this.url = newURL;
    this.isTopFrame = isTopFrame;
  }
  
  public String getNewURL() {
    return url;
  }
  
  public boolean isTopFrame() {
    return isTopFrame;
  }
  
  protected boolean isConsumed;
  
  public void consume() {
    isConsumed = true;
  }
  
  public boolean isConsumed() {
    return isConsumed;
  }
  
}
