/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.flashplayer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.FlashPluginOptions;
import chrriis.dj.nativeswing.swtimpl.components.JFlashPlayer;

/**
 * @author Christopher Deckers
 */
public class VariablesAndFlow {

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    JPanel flashPlayerPanel = new JPanel(new BorderLayout());
    flashPlayerPanel.setBorder(BorderFactory.createTitledBorder("Native Flash Player component"));
    final JFlashPlayer flashPlayer = new JFlashPlayer();
    flashPlayer.setControlBarVisible(true);
    FlashPluginOptions flashLoadingOptions = new FlashPluginOptions();
    flashLoadingOptions.setVariables(new HashMap<String, String>() {{put("mytext", "My Text");}});
    flashPlayer.load(VariablesAndFlow.class, "resource/dyn_text_moving.swf", flashLoadingOptions);
    flashPlayerPanel.add(flashPlayer, BorderLayout.CENTER);
    contentPane.add(flashPlayerPanel, BorderLayout.CENTER);
    JPanel variablePanel = new JPanel(new BorderLayout());
    variablePanel.setBorder(BorderFactory.createTitledBorder("Get/Set Variables"));
    JPanel getSetNorthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
    getSetNorthPanel.add(new JLabel("Text:"));
    final JTextField setTextField = new JTextField(new PlainDocument() {
      @Override
      public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if(str != null && getLength() + str.length() > 7) {
          return;
        }
        super.insertString(offs, str, a);
      }
    }, "Set", 14);
    getSetNorthPanel.add(setTextField);
    JButton setButton = new JButton("Set");
    setButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        flashPlayer.setVariable("mytext", setTextField.getText());
      }
    });
    getSetNorthPanel.add(setButton);
    JButton getButton = new JButton("Get");
    getSetNorthPanel.add(getButton);
    variablePanel.add(getSetNorthPanel, BorderLayout.NORTH);
    JPanel getSetSouthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
    getSetSouthPanel.add(new JLabel("Last acquired text:"));
    final JLabel getLabel = new JLabel("-");
    getButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String value = (String)flashPlayer.getVariable("mytext");
        getLabel.setText(value == null || value.length() == 0? " ": value);
      }
    });
    getSetSouthPanel.add(getLabel);
    variablePanel.add(getSetSouthPanel, BorderLayout.SOUTH);
    contentPane.add(variablePanel, BorderLayout.SOUTH);
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
