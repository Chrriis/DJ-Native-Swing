/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;


/**
 * An abstract adapter class for receiving web browser events.
 * @author Christopher Deckers
 */
public abstract class WebBrowserAdapter implements WebBrowserListener {

  public void windowWillOpen(WebBrowserWindowWillOpenEvent e) {}
  public void windowOpening(WebBrowserWindowOpeningEvent e) {}
  public void windowClosing(WebBrowserEvent e) {}

  public void locationChanging(WebBrowserNavigationEvent e) {}
  public void locationChanged(WebBrowserNavigationEvent e) {}
  public void locationChangeCanceled(WebBrowserNavigationEvent e) {}

  public void loadingProgressChanged(WebBrowserEvent e) {}
  public void titleChanged(WebBrowserEvent e) {}
  public void statusChanged(WebBrowserEvent e) {}

  public void commandReceived(WebBrowserCommandEvent e) {}

}
