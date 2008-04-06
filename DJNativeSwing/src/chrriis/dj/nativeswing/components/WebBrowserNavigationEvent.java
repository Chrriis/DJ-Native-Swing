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

  private String location;
  private boolean isTopFrame;

  public WebBrowserNavigationEvent(JWebBrowser webBrowser, String newLocation, boolean isTopFrame) {
    super(webBrowser);
    this.location = newLocation;
    this.isTopFrame = isTopFrame;
  }
  
  public String getNewLocation() {
    return location;
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
