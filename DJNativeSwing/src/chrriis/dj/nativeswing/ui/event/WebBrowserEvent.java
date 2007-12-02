/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui.event;

import java.util.EventObject;

import chrriis.dj.nativeswing.ui.JWebBrowser;

/**
 * @author Christopher Deckers
 */
public class WebBrowserEvent extends EventObject{

  protected JWebBrowser webBrowser;
  
  public WebBrowserEvent(JWebBrowser webBrowser) {
    super(webBrowser);
    this.webBrowser = webBrowser;
  }
  
  public JWebBrowser getWebBrowser() {
    return webBrowser;
  }
  
}
