/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;


/**
 * @author Christopher Deckers
 */
public class WebBrowserNavigationEvent extends WebBrowserEvent {

  private String newResourceLocation;
  private boolean isTopFrame;

  public WebBrowserNavigationEvent(JWebBrowser webBrowser, String newResourceLocation, boolean isTopFrame) {
    super(webBrowser);
    this.newResourceLocation = newResourceLocation;
    this.isTopFrame = isTopFrame;
  }

  public String getNewResourceLocation() {
    return newResourceLocation;
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
