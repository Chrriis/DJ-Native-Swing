/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components;


/**
 * @author Christopher Deckers
 */
public class WebBrowserNavigationEvent extends WebBrowserEvent {

  private String url;
  private boolean isTopFrame;

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
  
  private boolean isConsumed;
  
  public void consume() {
    isConsumed = true;
  }
  
  public boolean isConsumed() {
    return isConsumed;
  }
  
}
