/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.flashplayer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import chrriis.dj.nativeswing.ui.JFlashPlayer;
import chrriis.dj.nativeswing.ui.JFlashPlayer.FlashLoadingOptions;

/**
 * @author Christopher Deckers
 */
public class Interactions extends JPanel {

  public Interactions() {
    super(new BorderLayout(0, 0));
    JPanel flashPlayerPanel = new JPanel(new BorderLayout(0, 0));
    flashPlayerPanel.setBorder(BorderFactory.createTitledBorder("Native Flash Player component"));
    final JFlashPlayer flashPlayer = new JFlashPlayer();
    flashPlayer.setControlBarVisible(true);
    FlashLoadingOptions flashLoadingOptions = new FlashLoadingOptions();
    flashLoadingOptions.setVariables(new HashMap<String, String>() {{put("mytext", "My Text");}});
    flashPlayer.load(getClass(), "resource/dyn_text_moving.swf", flashLoadingOptions);
    flashPlayerPanel.add(flashPlayer, BorderLayout.CENTER);
    add(flashPlayerPanel, BorderLayout.CENTER);
    JPanel variablePanel = new JPanel(new BorderLayout(0, 0));
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
        String value = flashPlayer.getVariable("mytext");
        getLabel.setText(value == null || value.length() == 0? " ": value);
      }
    });
    getSetSouthPanel.add(getLabel);
    variablePanel.add(getSetSouthPanel, BorderLayout.SOUTH);
    add(variablePanel, BorderLayout.NORTH);
  }
  
}
