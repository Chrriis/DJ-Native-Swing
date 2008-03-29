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
import javax.swing.SwingUtilities;

import chrriis.common.WebServer;
import chrriis.dj.nativeswing.NativeInterface;
import chrriis.dj.nativeswing.NativeInterfaceOptions;
import chrriis.dj.nativeswing.components.JWebBrowser;

/**
 * @author Christopher Deckers
 */
public class ClasspathPages extends JPanel {

  public ClasspathPages() {
    super(new BorderLayout(0, 0));
    JWebBrowser webBrowser = new JWebBrowser();
    webBrowser.setURL(WebServer.getDefaultWebServer().getClassPathResourceURL(getClass().getName(), "resource/page1.html"));
    webBrowser.setBarsVisible(false);
    add(webBrowser, BorderLayout.CENTER);
  }
  
  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    NativeInterfaceOptions options = new NativeInterfaceOptions();
    options.setPreferredLookAndFeelApplied(true);
    NativeInterface.initialize(options);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ClasspathPages(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
  }
  
}
