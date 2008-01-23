/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.layering;

import java.awt.BorderLayout;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import chrriis.common.WebServer;
import chrriis.dj.nativeswing.demo.examples.flashplayer.SimpleFlashExample;
import chrriis.dj.nativeswing.ui.JFlashPlayer;
import chrriis.dj.nativeswing.ui.JWebBrowser;
import chrriis.dj.nativeswing.ui.NativeComponentEmbedder;

/**
 * @author Christopher Deckers
 */
public class DesktopPaneExample extends JPanel {

  public DesktopPaneExample() {
    super(new BorderLayout(0, 0));
    NativeComponentEmbedder.setLayeringPreferred(true);
    JDesktopPane desktopPane = new JDesktopPane();
    // Web Browser 1 internal frame
    JInternalFrame webBrowser1InternalFrame = new JInternalFrame("Web Browser 1");
    desktopPane.add(webBrowser1InternalFrame);
    webBrowser1InternalFrame.setBounds(10, 10, 400, 300);
    webBrowser1InternalFrame.setResizable(true);
    webBrowser1InternalFrame.setVisible(true);
    JWebBrowser webBrowser1 = new JWebBrowser();
    webBrowser1.setURL("http://www.google.com");
    webBrowser1InternalFrame.add(webBrowser1, BorderLayout.CENTER);
    // Flash Player internal frame
    JInternalFrame flashPlayerInternalFrame = new JInternalFrame("Flash Player");
    desktopPane.add(flashPlayerInternalFrame);
    flashPlayerInternalFrame.setBounds(110, 110, 400, 300);
    flashPlayerInternalFrame.setResizable(true);
    flashPlayerInternalFrame.setVisible(true);
    JFlashPlayer flashPlayer = new JFlashPlayer();
    flashPlayer.setControlBarVisible(false);
    String resourceURL = WebServer.getDefaultWebServer().getClassPathResourceURL(SimpleFlashExample.class.getName(), "resource/Movement-pointer_or_click.swf");
    flashPlayer.setURL(resourceURL);
    flashPlayerInternalFrame.add(flashPlayer, BorderLayout.CENTER);
    // Web Browser 2 internal frame
    JInternalFrame webBrowser2InternalFrame = new JInternalFrame("Web Browser 2");
    desktopPane.add(webBrowser2InternalFrame);
    webBrowser2InternalFrame.setBounds(210, 210, 400, 300);
    webBrowser2InternalFrame.setResizable(true);
    webBrowser2InternalFrame.setVisible(true);
    JWebBrowser webBrowser2 = new JWebBrowser();
    webBrowser2.setURL("http://www.google.com");
    webBrowser2InternalFrame.add(webBrowser2, BorderLayout.CENTER);
    add(desktopPane, BorderLayout.CENTER);
    NativeComponentEmbedder.setLayeringPreferred(false);
  }
  
}
