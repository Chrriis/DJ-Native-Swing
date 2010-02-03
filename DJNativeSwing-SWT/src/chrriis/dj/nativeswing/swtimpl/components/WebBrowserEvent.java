/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.util.EventObject;


/**
 * @author Christopher Deckers
 */
public class WebBrowserEvent extends EventObject {

  private JWebBrowser webBrowser;

  /**
   * Construct a web browser event.
   * @param webBrowser the web browser.
   */
  public WebBrowserEvent(JWebBrowser webBrowser) {
    super(webBrowser);
    this.webBrowser = webBrowser;
  }

  /**
   * Get the web browser.
   * @return the web browser.
   */
  public JWebBrowser getWebBrowser() {
    return webBrowser;
  }

}
