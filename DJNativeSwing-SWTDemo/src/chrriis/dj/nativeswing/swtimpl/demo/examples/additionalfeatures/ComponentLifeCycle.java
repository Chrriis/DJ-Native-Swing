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
import javax.swing.JComponent;
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
public class ComponentLifeCycle {

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new GridLayout(0, 1, 0, 0));
    addLifeCyclePane(contentPane, false);
    addLifeCyclePane(contentPane, true);
    return contentPane;
  }

  private static void addLifeCyclePane(JPanel contentPane, final boolean isForcedInitializationType) {
    JPanel lifeCyclePane = new JPanel(new BorderLayout(5, 0));
    lifeCyclePane.setBorder(BorderFactory.createTitledBorder(isForcedInitializationType? "Forced initialization life cycle": "Default life cycle"));
    final JPanel componentPane = new JPanel(new BorderLayout());
    JPanel buttonBar = new JPanel(new FlowLayout());
    final JButton createButton = new JButton("Create JFlashPlayer");
    buttonBar.add(createButton);
    componentPane.add(buttonBar, BorderLayout.SOUTH);
    lifeCyclePane.add(componentPane, BorderLayout.WEST);
    final JTextArea logTextArea = new JTextArea();
    logTextArea.setEditable(false);
    lifeCyclePane.add(new JScrollPane(logTextArea));
    contentPane.add(lifeCyclePane);
    componentPane.setPreferredSize(new Dimension(Math.max(componentPane.getPreferredSize().width, 150), 0));
    // Add listener
    createButton.addActionListener(new ActionListener() {
      private JFlashPlayer flashPlayer;
      public void actionPerformed(ActionEvent e) {
        if(flashPlayer != null) {
          componentPane.remove(flashPlayer);
          logTextArea.setText("");
        }
        createButton.setEnabled(false);
        if(isForcedInitializationType) {
          flashPlayer = createPlayerWithForcedInitializationLyfeCycle(logTextArea, componentPane);
        } else {
          flashPlayer = createPlayerWithDefaultLyfeCycle(logTextArea, componentPane);
        }
        flashPlayer.runInSequence(new Runnable() {
          public void run() {
            createButton.setEnabled(true);
          }
        });
      }
    });
  }

  private static JFlashPlayer createPlayerWithDefaultLyfeCycle(final JTextArea logTextArea, JComponent componentPane) {
    log(logTextArea, "- JFlashPlayer creation.");
    JFlashPlayer flashPlayer = new JFlashPlayer(JFlashPlayer.destroyOnFinalization());
    flashPlayer.setControlBarVisible(false);
    log(logTextArea, "  -> Calls will be played after initialization.");
    flashPlayer.runInSequence(new Runnable() {
      public void run() {
        log(logTextArea, "- JFlashPlayer is initialized.");
        log(logTextArea, "  -> runInSequence() used to display this message.");
      }
    });
    log(logTextArea, "- Before JFlashPlayer.load() call.");
    flashPlayer.load(SimpleFlashExample.class, "resource/Movement-pointer_or_click.swf");
    flashPlayer.runInSequence(new Runnable() {
      public void run() {
        log(logTextArea, "- runInSequence(): JFlashPlayer.load() has run.");
      }
    });
    log(logTextArea, "- After JFlashPlayer.load() call.");
    log(logTextArea, "- JFlashPlayer addition to containment hierarchy.");
    log(logTextArea, "  -> Initialization will soon happen automatically.");
    componentPane.add(flashPlayer, BorderLayout.CENTER);
    componentPane.revalidate();
    componentPane.repaint();
    return flashPlayer;
  }

  private static JFlashPlayer createPlayerWithForcedInitializationLyfeCycle(final JTextArea logTextArea, JComponent componentPane) {
    log(logTextArea, "- JFlashPlayer creation.");
    JFlashPlayer flashPlayer = new JFlashPlayer(JFlashPlayer.destroyOnFinalization());
    flashPlayer.setControlBarVisible(false);
    log(logTextArea, "- JFlashPlayer addition to containment hierarchy.");
    log(logTextArea, "  (mandatory before forced initialization)");
    componentPane.add(flashPlayer, BorderLayout.CENTER);
    componentPane.revalidate();
    componentPane.repaint();
    log(logTextArea, "- Forced initialization of the native peer.");
    log(logTextArea, "  -> Calls are now synchronous.");
    flashPlayer.initializeNativePeer();
    flashPlayer.runInSequence(new Runnable() {
      public void run() {
        log(logTextArea, "- JFlashPlayer is initialized.");
        log(logTextArea, "  -> runInSequence() used to display this message.");
      }
    });
    log(logTextArea, "- Before JFlashPlayer.load() call.");
    flashPlayer.load(SimpleFlashExample.class, "resource/Movement-pointer_or_click.swf");
    flashPlayer.runInSequence(new Runnable() {
      public void run() {
        log(logTextArea, "- runInSequence(): JFlashPlayer.load() has run.");
      }
    });
    log(logTextArea, "- After JFlashPlayer.load() call.");
    return flashPlayer;
  }

  private static void log(JTextArea logTextArea, String s) {
    logTextArea.append((logTextArea.getText().length() > 0? Utils.LINE_SEPARATOR: "") + s);
    logTextArea.setCaretPosition(0);
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
