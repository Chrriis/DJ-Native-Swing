/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.common.Utils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

/**
 * @author Christopher Deckers
 */
public class ConfirmedDisposal {

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    final JPanel webBrowserPanel = new JPanel(new BorderLayout());
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    final AtomicReference<JWebBrowser> webBrowserRef = new AtomicReference<JWebBrowser>();
    webBrowserRef.set(createWebBrowser(webBrowserPanel));
    contentPane.add(webBrowserPanel, BorderLayout.CENTER);
    // Create an additional bar allowing to dispose the web browser cleanly.
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
    final JButton cleanDisposeButton = new JButton("Dispose With Confirmation");
    final JButton createWebBrowserButton = new JButton("Create New Web Browser");
    cleanDisposeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(webBrowserRef.get().disposeNativePeer(true)) {
          webBrowserPanel.removeAll();
          webBrowserPanel.add(new JLabel("The web browser was disposed."), BorderLayout.CENTER);
          webBrowserPanel.revalidate();
          webBrowserPanel.repaint();
          cleanDisposeButton.setVisible(false);
          createWebBrowserButton.setVisible(true);
        }
      }
    });
    buttonPanel.add(cleanDisposeButton);
    createWebBrowserButton.setVisible(false);
    createWebBrowserButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        webBrowserRef.set(createWebBrowser(webBrowserPanel));
        cleanDisposeButton.setVisible(true);
        createWebBrowserButton.setVisible(false);
      }
    });
    buttonPanel.add(createWebBrowserButton);
    contentPane.add(buttonPanel, BorderLayout.SOUTH);
    return contentPane;
  }

  private static final String LS = Utils.LINE_SEPARATOR;

  private static JWebBrowser createWebBrowser(JPanel webBrowserPanel) {
    webBrowserPanel.removeAll();
    JWebBrowser webBrowser = new JWebBrowser();
    webBrowser.setBarsVisible(false);
    webBrowser.setHTMLContent(
        "<html>" + LS +
        "  <head>" + LS +
        "    <script>" + LS +
        "      window.onbeforeunload = function() {" + LS +
        "        return 'Here, a normal page would tell you there is some unsaved data.';" + LS +
        "      };" + LS +
        "    </script>" + LS +
        "  </head>" + LS +
        "  <body>" + LS +
        "    <textarea rows=\"5\" cols=\"25\">A bogus editor...</textarea>" + LS +
        "  </body>" + LS +
        "</html>");
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    webBrowserPanel.revalidate();
    webBrowserPanel.repaint();
    return webBrowser;
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
