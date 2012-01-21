/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.webbrowser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

/**
 * @author Christopher Deckers
 */
public class SettingContent {

  private static final String LS = System.getProperty("line.separator");

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    JPanel webBrowserPanel = new JPanel(new BorderLayout());
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    final JWebBrowser webBrowser = new JWebBrowser();
    webBrowser.setBarsVisible(false);
    webBrowser.setStatusBarVisible(true);
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    contentPane.add(webBrowserPanel, BorderLayout.CENTER);
    JPanel configurationPanel = new JPanel(new BorderLayout());
    configurationPanel.setBorder(BorderFactory.createTitledBorder("Configuration"));
    final JTextArea configurationTextArea = new JTextArea(
        "<html>" + LS +
        "  <body>" + LS +
        "    <h1>Some header</h1>" + LS +
        "    <p>A paragraph with a <a href=\"http://www.google.com\">link</a>.</p>" + LS +
        "  </body>" + LS +
        "</html>");
    JScrollPane scrollPane = new JScrollPane(configurationTextArea);
    Dimension preferredSize = scrollPane.getPreferredSize();
    preferredSize.height += 20;
    scrollPane.setPreferredSize(preferredSize);
    configurationPanel.add(scrollPane, BorderLayout.CENTER);
    JPanel configurationButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    JButton setContentButton = new JButton("Set Content");
    setContentButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        webBrowser.setHTMLContent(configurationTextArea.getText());
      }
    });
    configurationButtonPanel.add(setContentButton);
    configurationPanel.add(configurationButtonPanel, BorderLayout.SOUTH);
    contentPane.add(configurationPanel, BorderLayout.NORTH);
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
