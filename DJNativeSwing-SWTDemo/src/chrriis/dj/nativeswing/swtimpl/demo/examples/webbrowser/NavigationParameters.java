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
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.common.WebServer.HTTPData;
import chrriis.common.WebServer.HTTPRequest;
import chrriis.common.WebServer.WebServerContent;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationParameters;

/**
 * @author Christopher Deckers
 */
public class NavigationParameters {

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    JPanel buttonPanel = new JPanel(new GridBagLayout());
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    buttonPanel.add(new JLabel("Custom Header:"), new GridBagConstraints(0, 0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 1, 0), 0, 0));
    final JTextField testHeaderKeyTextField = new JTextField("Custom-header", 10);
    buttonPanel.add(testHeaderKeyTextField, new GridBagConstraints(1, 0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 1, 0), 0, 0));
    final JTextField testHeaderValueTextField = new JTextField("My value", 10);
    buttonPanel.add(testHeaderValueTextField, new GridBagConstraints(2, 0, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 1, 0), 0, 0));
    buttonPanel.add(new JLabel("POST Data:"), new GridBagConstraints(0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 0, 0, 0), 0, 0));
    final JTextField testPostDataKeyTextField = new JTextField("Custom-POST", 10);
    buttonPanel.add(testPostDataKeyTextField, new GridBagConstraints(1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(1, 5, 0, 0), 0, 0));
    final JTextField testPostDataValueTextField = new JTextField("My value", 10);
    buttonPanel.add(testPostDataValueTextField, new GridBagConstraints(2, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(1, 5, 0, 0), 0, 0));
    JButton testHeaderButton = new JButton("Test");
    buttonPanel.add(testHeaderButton, new GridBagConstraints(3, 0, 1, 2, 0, GridBagConstraints.WEST, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));
    contentPane.add(buttonPanel, BorderLayout.NORTH);
    final JPanel webBrowserPanel = new JPanel(new BorderLayout());
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    contentPane.add(webBrowserPanel, BorderLayout.CENTER);
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
        Map<String, String> postDataMap = new HashMap<String, String>();
        postDataMap.put(testPostDataKeyTextField.getText(), testPostDataValueTextField.getText());
        parameters.setPostData(postDataMap);
        // Let's generate the page with the resulting HTTP headers dynamically.
        webBrowser.navigate(WebServer.getDefaultWebServer().getDynamicContentURL(NavigationParameters.class.getName(), "header-viewer.html"), parameters);
        webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
        webBrowserPanel.revalidate();
        webBrowserPanel.repaint();
      }
    });
    return contentPane;
  }

  protected static WebServerContent getWebServerContent(final HTTPRequest httpRequest) {
    // We dynamically generate the page using the embedded web server.
    if("header-viewer.html".equals(httpRequest.getResourcePath())) {
      return new WebServerContent() {
        @Override
        public InputStream getInputStream() {
          StringBuilder sb = new StringBuilder();
          sb.append("<html><body>");
          sb.append("<h1>HTTP Headers</h1><table border=\"1\">");
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
          sb.append("</table>");
          HTTPData[] httpPostDataArray = httpRequest.getHTTPPostDataArray();
          if(httpPostDataArray != null) {
            sb.append("<h1>HTTP POST Data</h1><table border=\"1\">");
            HTTPData httpPostData = httpPostDataArray[0];
            Map<String, String> postHeaderMap = httpPostData.getHeaderMap();
            String[] postKeys = postHeaderMap.keySet().toArray(new String[0]);
            Arrays.sort(postKeys, String.CASE_INSENSITIVE_ORDER);
            for(String key: postKeys) {
              sb.append("<tr><td>");
              sb.append(Utils.escapeXML(key));
              sb.append("</td><td>");
              sb.append(Utils.escapeXML(postHeaderMap.get(key)));
              sb.append("</td></tr>");
            }
            sb.append("</table>");
          }
          sb.append("</body></html>");
          return getInputStream(sb.toString());
        }
      };
    }
    return null;
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
