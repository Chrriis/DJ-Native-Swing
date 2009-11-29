/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.common.WebServer.HTTPRequest;
import chrriis.common.WebServer.WebServerContent;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationParameters;

/**
 * @author Christopher Deckers
 */
public class NavigationParameters extends JPanel {

  public NavigationParameters() {
    super(new BorderLayout());
    JPanel buttonPanel = new JPanel(new GridBagLayout());
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    buttonPanel.add(new JLabel("Custom Header:"), new GridBagConstraints(0, 0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    final JTextField testHeaderKeyTextField = new JTextField("Custom-header", 10);
    buttonPanel.add(testHeaderKeyTextField, new GridBagConstraints(1, 0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
    final JTextField testHeaderValueTextField = new JTextField("My value", 10);
    buttonPanel.add(testHeaderValueTextField, new GridBagConstraints(2, 0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
    JButton testHeaderButton = new JButton("Test");
    buttonPanel.add(testHeaderButton, new GridBagConstraints(3, 0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
    buttonPanel.add(new JLabel("BugZilla Search:"), new GridBagConstraints(0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    final JTextField testPostDataTextField = new JTextField("Browser", 20);
    buttonPanel.add(testPostDataTextField, new GridBagConstraints(1, 1, 2, 1, 0, GridBagConstraints.WEST, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
    JButton testPostDataButton = new JButton("Go!");
    buttonPanel.add(testPostDataButton, new GridBagConstraints(3, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
    add(buttonPanel, BorderLayout.NORTH);
    final JPanel webBrowserPanel = new JPanel(new BorderLayout());
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    add(webBrowserPanel, BorderLayout.CENTER);
    // Add the listeners
    testHeaderButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        webBrowserPanel.removeAll();
        JWebBrowser webBrowser = new JWebBrowser();
        webBrowser.setBarsVisible(false);
        WebBrowserNavigationParameters parameters = new WebBrowserNavigationParameters();
        Map<String, String> headersMap = new HashMap<String, String>();
        headersMap.put("User-agent", "Native Swing Browser");
        headersMap.put(testHeaderKeyTextField.getText(), testHeaderValueTextField.getText());
        parameters.setHeaders(headersMap);
        // Let's generate the page with the resulting HTTP headers dynamically.
        webBrowser.navigate(WebServer.getDefaultWebServer().getDynamicContentURL(NavigationParameters.this.getClass().getName(), "header-viewer.html"), parameters);
        webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
        webBrowserPanel.revalidate();
        webBrowserPanel.repaint();
      }
    });
    testPostDataButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        webBrowserPanel.removeAll();
        JWebBrowser webBrowser = new JWebBrowser();
        webBrowser.setBarsVisible(false);
        WebBrowserNavigationParameters parameters = new WebBrowserNavigationParameters();
        Map<String, String> postDataMap = new HashMap<String, String>();
        postDataMap.put("short_desc_type", "allwordssubstr");
        postDataMap.put("short_desc", testPostDataTextField.getText());
        postDataMap.put("bug_status", "NEW");
        postDataMap.put("product", "Platform");
        postDataMap.put("component", "SWT");
        parameters.setPostData(postDataMap);
        webBrowser.navigate("https://bugs.eclipse.org/bugs/buglist.cgi", parameters);
        webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
        webBrowserPanel.revalidate();
        webBrowserPanel.repaint();
      }
    });
  }

  protected static WebServerContent getWebServerContent(final HTTPRequest httpRequest) {
    // We dynamically generate the page using the embedded web server.
    if("header-viewer.html".equals(httpRequest.getResourcePath())) {
      return new WebServerContent() {
        @Override
        public InputStream getInputStream() {
          StringBuilder sb = new StringBuilder();
          sb.append("<html><body><h1>HTTP Headers</h1><table border=\"1\">");
          Map<String, String> headerMap = httpRequest.getHeaderMap();
          String[] keys = headerMap.keySet().toArray(new String[0]);
          Arrays.sort(keys, String.CASE_INSENSITIVE_ORDER);
          for(String key: keys) {
            sb.append("<tr><td>");
            sb.append(Utils.escapeXML(key));
            sb.append("</td><td>");
            sb.append(Utils.escapeXML(headerMap.get(key)));
            sb.append("</td></tr>");
          }
          sb.append("</table></body></html>");
          return getInputStream(sb.toString());
        }
      };
    }
    return null;
  }

  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    UIUtils.setPreferredLookAndFeel();
    NativeInterface.open();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new NavigationParameters(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
    NativeInterface.runEventPump();
  }

}
