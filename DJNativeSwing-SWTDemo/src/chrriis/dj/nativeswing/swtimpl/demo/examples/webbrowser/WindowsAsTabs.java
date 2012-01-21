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
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowOpeningEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;

/**
 * @author Christopher Deckers
 */
public class WindowsAsTabs {

  protected static final String LS = System.getProperty("line.separator");

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    JWebBrowser webBrowser = new JWebBrowser();
    webBrowser.setBarsVisible(false);
    webBrowser.setStatusBarVisible(true);
    webBrowser.setHTMLContent(
        "<html>" + LS +
        "  <body>" + LS +
        "    <a href=\"http://www.google.com\">http://www.google.com</a>: normal link.<br/>" + LS +
        "    <a href=\"http://www.google.com\" target=\"_blank\">http://www.google.com</a>: link that normally opens in new window.<br/>" + LS +
        "  </body>" + LS +
        "</html>");
    addWebBrowserListener(tabbedPane, webBrowser);
    tabbedPane.addTab("Startup page", webBrowser);
    contentPane.add(tabbedPane, BorderLayout.CENTER);
    return contentPane;
  }

  private static void addWebBrowserListener(final JTabbedPane tabbedPane, final JWebBrowser webBrowser) {
    webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
      @Override
      public void titleChanged(WebBrowserEvent e) {
        for(int i=0; i<tabbedPane.getTabCount(); i++) {
          if(tabbedPane.getComponentAt(i) == webBrowser) {
            if(i == 0) {
              return;
            }
            tabbedPane.setTitleAt(i, webBrowser.getPageTitle());
            break;
          }
        }
      }
      @Override
      public void windowWillOpen(WebBrowserWindowWillOpenEvent e) {
        JWebBrowser newWebBrowser = new JWebBrowser();
        addWebBrowserListener(tabbedPane, newWebBrowser);
        tabbedPane.addTab("New Tab", newWebBrowser);
        e.setNewWebBrowser(newWebBrowser);
      }
      @Override
      public void windowOpening(WebBrowserWindowOpeningEvent e) {
        e.getWebBrowser().setMenuBarVisible(false);
      }
    });
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
