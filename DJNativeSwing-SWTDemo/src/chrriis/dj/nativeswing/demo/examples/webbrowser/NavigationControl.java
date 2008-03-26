/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.webbrowser;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.NativeInterface;
import chrriis.dj.nativeswing.NativeInterface.NativeInterfaceInitOptions;
import chrriis.dj.nativeswing.ui.JWebBrowser;
import chrriis.dj.nativeswing.ui.JWebBrowserWindow;
import chrriis.dj.nativeswing.ui.event.WebBrowserAdapter;
import chrriis.dj.nativeswing.ui.event.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.ui.event.WebBrowserWindowWillOpenEvent;

/**
 * @author Christopher Deckers
 */
public class NavigationControl extends JPanel {

  protected static final String LS = System.getProperty("line.separator");

  public NavigationControl() {
    super(new BorderLayout(0, 0));
    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    final JWebBrowser webBrowser = new JWebBrowser();
    webBrowser.setBarsVisible(false);
    webBrowser.setStatusBarVisible(true);
    webBrowser.setHTMLContent(
        "<html>" + LS +
        "  <body>" + LS +
        "    <a href=\"http://java.sun.com\">http://java.sun.com</a>: create a new tab.<br/>" + LS +
        "    <a href=\"http://www.google.com\">http://www.google.com</a>: open in a new window.<br/>" + LS +
        "    <a href=\"http://www.eclipse.org\">http://www.eclipse.org</a>: link is blocked. Context menu \"Open in new Window\" creates a new tab.<br/>" + LS +
        "    <a href=\"http://www.microsoft.com\">http://www.microsoft.com</a>: link and \"Open in new Window\" are blocked.<br/>" + LS +
        "  </body>" + LS +
        "</html>");
    webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
      @Override
      public void urlChanging(WebBrowserNavigationEvent e) {
        final String newURL = e.getNewURL();
        if(newURL.startsWith("http://www.google.com/")) {
          e.consume();
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              JWebBrowserWindow webBrowserWindow = new JWebBrowserWindow();
              webBrowserWindow.getWebBrowser().setURL(newURL);
              webBrowserWindow.setVisible(true);
            }
          });
        } else if(newURL.startsWith("http://java.sun.com/")) {
          e.consume();
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              JWebBrowser webBrowser = new JWebBrowser();
              webBrowser.setURL(newURL);
              tabbedPane.addTab("java.sun.com", webBrowser);
            }
          });
        } else if(newURL.startsWith("http://www.eclipse.org/")) {
          e.consume();
        } else if(newURL.startsWith("http://www.microsoft.com/")) {
          e.consume();
        }
      }
      @Override
      public void windowWillOpen(WebBrowserWindowWillOpenEvent e) {
        // We let the window to be created, but we will check the first URL that is set on it.
        e.getNewWebBrowser().addWebBrowserListener(new WebBrowserAdapter() {
          @Override
          public void urlChanging(WebBrowserNavigationEvent e) {
            final JWebBrowser webBrowser = e.getWebBrowser();
            webBrowser.removeWebBrowserListener(this);
            String newURL = e.getNewURL();
            boolean isBlocked = false;
            if(newURL.startsWith("http://www.microsoft.com/")) {
              isBlocked = true;
            } else if(newURL.startsWith("http://www.eclipse.org/")) {
              isBlocked = true;
              JWebBrowser newWebBrowser = new JWebBrowser();
              newWebBrowser.copyAppearance(webBrowser);
              newWebBrowser.setURL(newURL);
              tabbedPane.addTab("www.eclipse.org", newWebBrowser);
            }
            if(isBlocked) {
              e.consume();
              // The URL Changing event is special: it is synchronous so disposal must be deferred.
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  webBrowser.getBrowserWindow().dispose();
                }
              });
            }
          }
        });
      }
    });
    tabbedPane.addTab("Controled Browser", webBrowser);
    add(tabbedPane, BorderLayout.CENTER);
  }
  
  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    NativeInterfaceInitOptions options = new NativeInterfaceInitOptions();
    options.setPreferredLookAndFeelApplied(true);
    NativeInterface.init(options);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new NavigationControl(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
  }
  
}
