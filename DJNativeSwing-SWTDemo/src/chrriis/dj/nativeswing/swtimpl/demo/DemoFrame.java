/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;

/**
 * @author Christopher Deckers
 */
public class DemoFrame {

  public static void main(String[] args) {
    UIUtils.setPreferredLookAndFeel();
    NativeInterface.open();
    Toolkit.getDefaultToolkit().setDynamicLayout(true);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame demoFrame = new JFrame("The DJ Project - NativeSwing (SWT)");
        Class<DemoFrame> clazz = DemoFrame.class;
        if(System.getProperty("java.version").compareTo("1.6") >= 0) {
          demoFrame.setIconImages(Arrays.asList(new Image[] {
              new ImageIcon(clazz.getResource("resource/DJIcon16x16.png")).getImage(),
              new ImageIcon(clazz.getResource("resource/DJIcon24x24.png")).getImage(),
              new ImageIcon(clazz.getResource("resource/DJIcon32x32.png")).getImage(),
              new ImageIcon(clazz.getResource("resource/DJIcon48x48.png")).getImage(),
              new ImageIcon(clazz.getResource("resource/DJIcon256x256.png")).getImage(),
          }));
        } else {
          demoFrame.setIconImage(new ImageIcon(clazz.getResource("resource/DJIcon32x32Plain.png")).getImage());
        }
        demoFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        demoFrame.getContentPane().add(new DemoPane());
        demoFrame.setSize(800, 600);
        demoFrame.setLocationByPlatform(true);
        demoFrame.setVisible(true);
      }
    });
    NativeInterface.runEventPump();
  }

}
