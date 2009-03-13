/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.additionalfeatures;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JFlashPlayer;
import chrriis.dj.nativeswing.swtimpl.demo.examples.flashplayer.SimpleFlashExample;

/**
 * @author Christopher Deckers
 */
public class DeferredDestruction extends JPanel {

  protected JFlashPlayer flashPlayer;

  public DeferredDestruction() {
    super(new BorderLayout());
    flashPlayer = new JFlashPlayer(JFlashPlayer.destroyOnFinalization());
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

  @Override
  public void removeNotify() {
    super.removeNotify();
    // flashPlayer is destroyed on finalization.
    // Rather than wait for garbage collection, release when the component is removed from its parent.
    flashPlayer.disposeNativePeer();
  }

  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    UIUtils.setPreferredLookAndFeel();
    NativeInterface.open();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new DeferredDestruction(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
    NativeInterface.runEventPump();
  }

}
