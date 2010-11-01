/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.NativeInterfaceAppletHandler;

/**
 * @author Christopher Deckers
 */
public class DemoApplet extends JApplet {

  static {
    // This is a mandatory call to activate applet support.
    NativeInterfaceAppletHandler.activateAppletMode();
    // For this applet, I decided to force the look and feel to the preferred one.
    UIUtils.setPreferredLookAndFeel();
  }

  @Override
  public void init() {
    // This is a mandatory call to activate applet support.
    NativeInterfaceAppletHandler.init(this);
    // Rest of your init().
  }

  @Override
  public void start() {
    // This is a mandatory call to activate applet support.
    NativeInterfaceAppletHandler.start(this);
    // Rest of your start().
    // For this applet, I decided to open the interface in the start method.
    NativeInterface.open();
    // For this applet, I decided to add all content.
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        getContentPane().add(new DemoPane(), BorderLayout.CENTER);
      }
    });
  }

  @Override
  public void stop() {
    // This is a mandatory call to activate applet support.
    NativeInterfaceAppletHandler.stop(this);
    // Rest of your stop().
    // For this applet, I decided to remove all content.
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Container contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.invalidate();
        contentPane.validate();
      }
    });
    // For this applet, I decided to close the interface here.
    NativeInterface.close();
  }

  @Override
  public void destroy() {
    // This is a mandatory call to activate applet support.
    NativeInterfaceAppletHandler.destroy(this);
    // Rest of your destroy().
  }

}
