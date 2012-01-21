/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.additionalfeatures;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JFlashPlayer;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.demo.examples.flashplayer.SimpleFlashExample;

/**
 * @author Christopher Deckers
 */
public class HierarchyProxying {

  public static JComponent createContent() {
    JDesktopPane desktopPane = new JDesktopPane();
    // Web Browser 1 internal frame
    JInternalFrame webBrowser1InternalFrame = new JInternalFrame("Web Browser 1");
    webBrowser1InternalFrame.setBounds(10, 10, 400, 280);
    webBrowser1InternalFrame.setResizable(true);
    webBrowser1InternalFrame.setVisible(true);
    JWebBrowser webBrowser1 = new JWebBrowser(JWebBrowser.proxyComponentHierarchy());
    webBrowser1.navigate("http://djproject.sf.net");
    webBrowser1InternalFrame.add(webBrowser1, BorderLayout.CENTER);
    desktopPane.add(webBrowser1InternalFrame);
    // Flash Player internal frame
    JInternalFrame flashPlayerInternalFrame = new JInternalFrame("Flash Player");
    flashPlayerInternalFrame.setBounds(100, 100, 400, 280);
    flashPlayerInternalFrame.setResizable(true);
    flashPlayerInternalFrame.setVisible(true);
    JFlashPlayer flashPlayer = new JFlashPlayer(JFlashPlayer.proxyComponentHierarchy());
    flashPlayer.setControlBarVisible(false);
    flashPlayer.load(SimpleFlashExample.class, "resource/Movement-pointer_or_click.swf");
    flashPlayerInternalFrame.add(flashPlayer, BorderLayout.CENTER);
    desktopPane.add(flashPlayerInternalFrame);
    // Web Browser 2 internal frame, with a button on top
    JInternalFrame webBrowser2InternalFrame = new JInternalFrame("Web Browser 2 with a JButton on top");
    webBrowser2InternalFrame.setBounds(190, 190, 400, 280);
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
    // When a frame is iconified, components are destroyed. To avoid this, we use the option to destroy on finalization.
    final JWebBrowser webBrowser2 = new JWebBrowser(JWebBrowser.proxyComponentHierarchy(), JWebBrowser.destroyOnFinalization());
    webBrowser2.navigate("http://www.google.com");
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
    webBrowser2InternalFrame.setIconifiable(true);
    desktopPane.add(webBrowser2InternalFrame);
    JPanel contentPane = new JPanel(new BorderLayout()) {
      @Override
      public void removeNotify() {
        super.removeNotify();
        // webBrowser2 is destroyed on finalization.
        // Rather than wait for garbage collection, release when the component is removed from its parent.
        webBrowser2.disposeNativePeer();
      }
    };
    contentPane.add(desktopPane, BorderLayout.CENTER);
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
