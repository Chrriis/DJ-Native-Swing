/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.flashplayer;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import chrriis.dj.nativeswing.NativeInterface;
import chrriis.dj.nativeswing.components.JFlashPlayer;

/**
 * @author Christopher Deckers
 */
public class SimpleFlashExample extends JPanel {

  public SimpleFlashExample() {
    super(new BorderLayout(0, 0));
    JFlashPlayer flashPlayer = new JFlashPlayer();
    flashPlayer.load(getClass(), "resource/Movement-pointer_or_click.swf");
    add(flashPlayer, BorderLayout.CENTER);
  }
  
  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e) {}
    NativeInterface.start();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new SimpleFlashExample(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
  }
  
}
