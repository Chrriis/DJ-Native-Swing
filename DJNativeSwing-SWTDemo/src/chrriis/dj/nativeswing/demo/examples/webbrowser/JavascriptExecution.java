/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.webbrowser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.NativeInterfaceHandler;
import chrriis.dj.nativeswing.NativeInterfaceHandler.NativeInterfaceInitOptions;
import chrriis.dj.nativeswing.ui.JWebBrowser;

/**
 * @author Christopher Deckers
 */
public class JavascriptExecution extends JPanel {

  protected static final String LS = System.getProperty("line.separator");

  public JavascriptExecution() {
    super(new BorderLayout(0, 0));
    JPanel webBrowserPanel = new JPanel(new BorderLayout(0, 0));
    webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
    final JWebBrowser webBrowser = new JWebBrowser();
    webBrowser.setBarsVisible(false);
    webBrowser.setStatusBarVisible(true);
    webBrowser.setText(
        "<html>" + LS +
        "  <body>" + LS +
        "    <h1>Some header</h1>" + LS +
        "    <p>A paragraph with a <a href=\"http://www.google.com\">link</a>.</p>" + LS +
        "  </body>" + LS +
        "</html>");
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    add(webBrowserPanel, BorderLayout.CENTER);
    JPanel configurationPanel = new JPanel(new BorderLayout(0, 0));
    configurationPanel.setBorder(BorderFactory.createTitledBorder("Configuration"));
    final JTextArea configurationTextArea = new JTextArea(
        "document.bgColor = '#FFFF00';" + LS +
        "//window.open('http://www.google.com');" + LS);
    JScrollPane scrollPane = new JScrollPane(configurationTextArea);
    Dimension preferredSize = scrollPane.getPreferredSize();
    preferredSize.height += 20;
    scrollPane.setPreferredSize(preferredSize);
    configurationPanel.add(scrollPane, BorderLayout.CENTER);
    JPanel configurationButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    JButton executeJavascriptButton = new JButton("Execute Javascript");
    executeJavascriptButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        webBrowser.execute(configurationTextArea.getText());
      }
    });
    configurationButtonPanel.add(executeJavascriptButton);
    configurationPanel.add(configurationButtonPanel, BorderLayout.SOUTH);
    add(configurationPanel, BorderLayout.NORTH);
  }
  
  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    NativeInterfaceInitOptions options = new NativeInterfaceInitOptions();
    options.setPreferredLookAndFeelApplied(true);
    NativeInterfaceHandler.init(options);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JavascriptExecution(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
  }
  
}
