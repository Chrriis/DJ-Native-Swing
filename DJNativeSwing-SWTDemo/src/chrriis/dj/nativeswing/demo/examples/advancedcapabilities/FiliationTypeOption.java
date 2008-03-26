/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.advancedcapabilities;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.Disposable;
import chrriis.dj.nativeswing.NativeInterface;
import chrriis.dj.nativeswing.NativeInterface.NativeInterfaceInitOptions;
import chrriis.dj.nativeswing.demo.examples.flashplayer.SimpleFlashExample;
import chrriis.dj.nativeswing.ui.JFlashPlayer;
import chrriis.dj.nativeswing.ui.JWebBrowser;
import chrriis.dj.nativeswing.ui.NativeComponent;
import chrriis.dj.nativeswing.ui.NativeComponent.Options.DestructionTime;
import chrriis.dj.nativeswing.ui.NativeComponent.Options.FiliationType;

/**
 * @author Christopher Deckers
 */
public class FiliationTypeOption extends JPanel implements Disposable {

  private JWebBrowser webBrowser2;
  
  public FiliationTypeOption() {
    super(new BorderLayout(0, 0));
    JDesktopPane desktopPane = new JDesktopPane();
    // Web Browser 1 internal frame
    JInternalFrame webBrowser1InternalFrame = new JInternalFrame("Web Browser 1");
    webBrowser1InternalFrame.setBounds(10, 10, 400, 280);
    webBrowser1InternalFrame.setResizable(true);
    webBrowser1InternalFrame.setVisible(true);
    NativeComponent.getNextInstanceOptions().setFiliationType(FiliationType.COMPONENT_PROXYING);
    JWebBrowser webBrowser1 = new JWebBrowser();
    webBrowser1.setURL("http://djproject.sf.net");
    webBrowser1InternalFrame.add(webBrowser1, BorderLayout.CENTER);
    desktopPane.add(webBrowser1InternalFrame);
    // Flash Player internal frame
    JInternalFrame flashPlayerInternalFrame = new JInternalFrame("Flash Player");
    flashPlayerInternalFrame.setBounds(100, 100, 400, 280);
    flashPlayerInternalFrame.setResizable(true);
    flashPlayerInternalFrame.setVisible(true);
    NativeComponent.getNextInstanceOptions().setFiliationType(FiliationType.COMPONENT_PROXYING);
    JFlashPlayer flashPlayer = new JFlashPlayer();
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
    NativeComponent.getNextInstanceOptions().setFiliationType(FiliationType.COMPONENT_PROXYING);
    // When a frame is iconified, components are destroyed. To avoid this, we use the option to destroy on finalization.
    NativeComponent.getNextInstanceOptions().setDestructionTime(DestructionTime.ON_FINALIZATION);
    webBrowser2 = new JWebBrowser();
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
    webBrowser2InternalFrame.setIconifiable(true);
    desktopPane.add(webBrowser2InternalFrame);
    add(desktopPane, BorderLayout.CENTER);
  }
  
  public void dispose() {
    // webBrowser 2 is disposed on finalization.
    // Rather than waiting for garbage collection, release when the demo leaves this screen.
    webBrowser2.dispose();
  }
  
  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    NativeInterfaceInitOptions options = new NativeInterfaceInitOptions();
    options.setPreferredLookAndFeelApplied(true);
    NativeInterface.init(options);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new FiliationTypeOption(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
  }
  
}
