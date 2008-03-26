/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.advancedcapabilities;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.Disposable;
import chrriis.dj.nativeswing.NativeComponent;
import chrriis.dj.nativeswing.NativeInterface;
import chrriis.dj.nativeswing.NativeComponent.Options.DestructionTime;
import chrriis.dj.nativeswing.NativeInterface.NativeInterfaceInitOptions;
import chrriis.dj.nativeswing.components.JFlashPlayer;
import chrriis.dj.nativeswing.demo.examples.flashplayer.SimpleFlashExample;

/**
 * @author Christopher Deckers
 */
public class DestructionTimeOption extends JPanel implements Disposable {

  protected JFlashPlayer flashPlayer;
  
  public DestructionTimeOption() {
    super(new BorderLayout(0, 0));
    NativeComponent.getNextInstanceOptions().setDestructionTime(DestructionTime.ON_FINALIZATION);
    flashPlayer = new JFlashPlayer();
    flashPlayer.setControlBarVisible(false);
    flashPlayer.load(SimpleFlashExample.class, "resource/Movement-pointer_or_click.swf");
    add(flashPlayer, BorderLayout.CENTER);
    JButton addRemoveButton = new JButton("Add/Remove component");
    addRemoveButton.addActionListener(new ActionListener() {
      protected boolean isAdded = true;
      public void actionPerformed(ActionEvent e) {
        if(isAdded) {
          remove(flashPlayer);
        } else {
          add(flashPlayer, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
        isAdded = !isAdded;
      }
    });
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    buttonPanel.add(addRemoveButton);
    add(buttonPanel, BorderLayout.SOUTH);
  }
  
  public void dispose() {
    flashPlayer.disposeNativePeer();
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
        frame.getContentPane().add(new DestructionTimeOption(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
  }
  
}
