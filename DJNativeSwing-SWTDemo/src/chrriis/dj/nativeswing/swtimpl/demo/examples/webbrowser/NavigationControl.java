/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowserWindow;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowFactory;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;

/**
 * @author Christopher Deckers
 */
public class NavigationControl {

  protected static final String LS = System.getProperty("line.separator");

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    final JWebBrowser webBrowser = new JWebBrowser();
    webBrowser.setBarsVisible(false);
    webBrowser.setStatusBarVisible(true);
    webBrowser.setHTMLContent(
        "<html>" + LS +
        "  <body>" + LS +
        "    <a href=\"http://java.sun.com\">http://java.sun.com</a>: force link to open in a new tab.<br/>" + LS +
        "    <a href=\"http://www.google.com\">http://www.google.com</a>: force link to open in a new window.<br/>" + LS +
        "    <a href=\"http://www.eclipse.org\">http://www.eclipse.org</a>: block link. Context menu \"Open in new Window\" creates a new tab.<br/>" + LS +
        "    <a href=\"http://www.yahoo.com\" target=\"_blank\">http://www.yahoo.com</a>: link normally opens in a new window but creates a new tab.<br/>" + LS +
        "    <a href=\"http://www.microsoft.com\">http://www.microsoft.com</a>: link and \"Open in new Window\" are blocked.<br/>" + LS +
        "  </body>" + LS +
        "</html>");
    webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
      @Override
      public void locationChanging(WebBrowserNavigationEvent e) {
        final String newResourceLocation = e.getNewResourceLocation();
        if(newResourceLocation.startsWith("http://www.google.com/")) {
          e.consume();
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              JWebBrowser webBrowser = new JWebBrowser();
              JWebBrowserWindow webBrowserWindow = WebBrowserWindowFactory.create(webBrowser);
              webBrowser.navigate(newResourceLocation);
              webBrowserWindow.setVisible(true);
            }
          });
        } else if(newResourceLocation.startsWith("http://java.sun.com/")) {
          e.consume();
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              JWebBrowser webBrowser = new JWebBrowser();
              webBrowser.navigate(newResourceLocation);
              tabbedPane.addTab("java.sun.com", webBrowser);
            }
          });
        } else if(newResourceLocation.startsWith("http://www.eclipse.org/")) {
          e.consume();
        } else if(newResourceLocation.startsWith("http://www.microsoft.com/")) {
          e.consume();
        }
      }
      @Override
      public void windowWillOpen(WebBrowserWindowWillOpenEvent e) {
        // We let the window to be created, but we will check the first location that is set on it.
        e.getNewWebBrowser().addWebBrowserListener(new WebBrowserAdapter() {
          @Override
          public void locationChanging(WebBrowserNavigationEvent e) {
            final JWebBrowser webBrowser = e.getWebBrowser();
            webBrowser.removeWebBrowserListener(this);
            String newResourceLocation = e.getNewResourceLocation();
            boolean isBlocked = false;
            if(newResourceLocation.startsWith("http://www.microsoft.com/")) {
              isBlocked = true;
            } else if(newResourceLocation.startsWith("http://www.eclipse.org/")) {
              isBlocked = true;
              JWebBrowser newWebBrowser = new JWebBrowser();
              JWebBrowser.copyAppearance(webBrowser, newWebBrowser);
              newWebBrowser.navigate(newResourceLocation);
              tabbedPane.addTab("www.eclipse.org", newWebBrowser);
            } else if(newResourceLocation.startsWith("http://www.yahoo.com/")) {
              isBlocked = true;
              JWebBrowser newWebBrowser = new JWebBrowser();
              JWebBrowser.copyAppearance(webBrowser, newWebBrowser);
              newWebBrowser.navigate(newResourceLocation);
              tabbedPane.addTab("www.yahoo.com", newWebBrowser);
            }
            if(isBlocked) {
              e.consume();
              // The URL Changing event is special: it is synchronous so disposal must be deferred.
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  webBrowser.getWebBrowserWindow().dispose();
                }
              });
            }
          }
        });
      }
    });
    tabbedPane.addTab("Controled Browser", webBrowser);
    contentPane.add(tabbedPane, BorderLayout.CENTER);
    return contentPane;
  }

  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    NativeInterface.open();
    UIUtils.setPreferredLookAndFeel();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(createContent(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
    NativeInterface.runEventPump();
  }

}
