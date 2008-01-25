/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.layering;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
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
public class DesktopPaneComponentLayeringExample extends JPanel {

  public DesktopPaneComponentLayeringExample() {
    super(new BorderLayout(0, 0));
    NativeComponentEmbedder.setPreferredLayering(NativeComponentEmbedder.Layering.COMPONENT_LAYERING);
    JDesktopPane desktopPane = new JDesktopPane();
    // Web Browser 1 internal frame
    JInternalFrame webBrowser1InternalFrame = new JInternalFrame("Web Browser 1");
    webBrowser1InternalFrame.setBounds(10, 10, 400, 300);
    webBrowser1InternalFrame.setResizable(true);
    webBrowser1InternalFrame.setVisible(true);
    JWebBrowser webBrowser1 = new JWebBrowser();
    webBrowser1.setURL("http://www.google.com");
    webBrowser1InternalFrame.add(webBrowser1, BorderLayout.CENTER);
    desktopPane.add(webBrowser1InternalFrame);
    // Flash Player internal frame
    JInternalFrame flashPlayerInternalFrame = new JInternalFrame("Flash Player");
    flashPlayerInternalFrame.setBounds(110, 110, 400, 300);
    flashPlayerInternalFrame.setResizable(true);
    flashPlayerInternalFrame.setVisible(true);
    JFlashPlayer flashPlayer = new JFlashPlayer();
    flashPlayer.setControlBarVisible(false);
    String resourceURL = WebServer.getDefaultWebServer().getClassPathResourceURL(SimpleFlashExample.class.getName(), "resource/Movement-pointer_or_click.swf");
    flashPlayer.setURL(resourceURL);
    flashPlayerInternalFrame.add(flashPlayer, BorderLayout.CENTER);
    desktopPane.add(flashPlayerInternalFrame);
    // Web Browser 2 internal frame, with a button on top
    JInternalFrame webBrowser2InternalFrame = new JInternalFrame("Web Browser 2 with JButton on top");
    webBrowser2InternalFrame.setBounds(210, 210, 400, 300);
    webBrowser2InternalFrame.setResizable(true);
    webBrowser2InternalFrame.setVisible(true);
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints cons = new GridBagConstraints();
    cons.fill = GridBagConstraints.BOTH;
    cons.gridx = 0;
    cons.gridy = 0;
    JPanel webBrowser2ContentPane = new JPanel(gridBag) {
      @Override
      public boolean isOptimizedDrawingEnabled() {
        return false;
      }
    };
    JWebBrowser webBrowser2 = new JWebBrowser();
    webBrowser2.setURL("http://www.google.com");
    cons.weightx = 1;
    cons.weighty = 1;
    gridBag.setConstraints(webBrowser2, cons);
    webBrowser2ContentPane.add(webBrowser2);
    JButton webBrowser2Button = new JButton("A Swing button");
    cons.fill = GridBagConstraints.NONE;
    cons.weightx = 0;
    cons.weighty = 0;
    gridBag.setConstraints(webBrowser2Button, cons);
    webBrowser2ContentPane.add(webBrowser2Button);
    webBrowser2ContentPane.setComponentZOrder(webBrowser2Button, 0);
    webBrowser2InternalFrame.add(webBrowser2ContentPane, BorderLayout.CENTER);
    desktopPane.add(webBrowser2InternalFrame);
    add(desktopPane, BorderLayout.CENTER);
    NativeComponentEmbedder.setPreferredLayering(NativeComponentEmbedder.Layering.NO_LAYERING);
  }
  
}