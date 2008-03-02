/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui.event;


/**
 * @author Christopher Deckers
 */
public abstract class WebBrowserAdapter implements WebBrowserListener {

  public void windowCreation(WebBrowserWindowCreationEvent e) {}
  public void windowOpening(WebBrowserWindowOpeningEvent e) {}
  public void windowClosing(WebBrowserEvent e) {}
  
  public void urlChanging(WebBrowserNavigationEvent e) {}
  public void urlChanged(WebBrowserNavigationEvent e) {}
  public void urlChangeCanceled(WebBrowserNavigationEvent e) {}
  
  public void loadingProgressChanged(WebBrowserEvent e) {}
  public void titleChanged(WebBrowserEvent e) {}
  public void statusChanged(WebBrowserEvent e) {}

  public void commandReceived(WebBrowserEvent e, String command) {}
  
}
