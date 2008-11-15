/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.additionalfeatures;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.common.Utils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JFlashPlayer;
import chrriis.dj.nativeswing.swtimpl.demo.examples.flashplayer.SimpleFlashExample;

/**
 * @author Christopher Deckers
 */
public class ComponentLifeCycle extends JPanel {

  public ComponentLifeCycle() {
    super(new GridLayout(0, 1, 0, 0));
    addLifeCyclePane(false);
    addLifeCyclePane(true);
  }
  
  private static final String LS = Utils.LINE_SEPARATOR;
  
  private void addLifeCyclePane(final boolean isForcedInitializationType) {
    JPanel lifeCyclePane = new JPanel(new BorderLayout(5, 0));
    lifeCyclePane.setBorder(BorderFactory.createTitledBorder(isForcedInitializationType? "Forced initialization life cycle": "Default life cycle"));
    final JPanel componentPane = new JPanel(new BorderLayout()) {
      @Override
      public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
        preferredSize.width = 150;
        return preferredSize;
      }
    };
    JPanel buttonBar = new JPanel(new FlowLayout());
    JButton createButton = new JButton("Create");
    buttonBar.add(createButton);
    componentPane.add(buttonBar, BorderLayout.SOUTH);
    lifeCyclePane.add(componentPane, BorderLayout.WEST);
    final JTextArea logTextArea = new JTextArea();
    logTextArea.setEditable(false);
    lifeCyclePane.add(new JScrollPane(logTextArea));
    add(lifeCyclePane);
    // Listener
    createButton.addActionListener(new ActionListener() {
      private int count = 1;
      private JFlashPlayer flashPlayer;
      private void log(String s, boolean isWithCount) {
        logTextArea.append((logTextArea.getText().length() > 0? LS: "") + (isWithCount? count++ + ". ": "  ") + s);
        logTextArea.setCaretPosition(0);
      }
      public void actionPerformed(ActionEvent e) {
        if(flashPlayer != null) {
          componentPane.remove(flashPlayer);
          count = 1;
          logTextArea.setText("");
        }
        log("JFlashPlayer creation.", true);
        flashPlayer = new JFlashPlayer(JFlashPlayer.destroyOnFinalization());
        if(isForcedInitializationType) {
          log("JFlashPlayer addition to containment hierarchy.", true);
          log("(mandatory before forced initialization)", false);
          componentPane.add(flashPlayer, BorderLayout.CENTER);
          componentPane.revalidate();
          componentPane.repaint();
          log("Forced initialization of the native peer.", true);
          log("-> Calls are now synchronous.", false);
          flashPlayer.initializeNativePeer();
        } else {
          log("-> Calls are queued to be played after initialization.", false);
        }
        flashPlayer.setControlBarVisible(false);
        flashPlayer.runInSequence(new Runnable() {
          public void run() {
            log("JFlashPlayer is initialized.", true);
          }
        });
        log("Before JFlashPlayer.load() call.", true);
        flashPlayer.load(SimpleFlashExample.class, "resource/Movement-pointer_or_click.swf");
        flashPlayer.runInSequence(new Runnable() {
          public void run() {
            log("JFlashPlayer.load() has run.", true);
            log("-> runInSequence() used to display this message in sync with actual call.", false);
          }
        });
        log("After JFlashPlayer.load() call.", true);
        if(!isForcedInitializationType) {
          log("JFlashPlayer addition to containment hierarchy.", true);
          log("-> Initialization will soon happen automatically.", false);
          componentPane.add(flashPlayer, BorderLayout.CENTER);
          componentPane.revalidate();
          componentPane.repaint();
        }
      }
    });
  }
  
  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    UIUtils.setPreferredLookAndFeel();
    NativeInterface.open();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ComponentLifeCycle(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
    NativeInterface.runEventPump();
  }
  
}
