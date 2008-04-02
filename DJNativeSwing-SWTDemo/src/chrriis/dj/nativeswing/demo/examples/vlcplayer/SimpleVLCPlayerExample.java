/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.vlcplayer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import chrriis.dj.nativeswing.NativeInterface;
import chrriis.dj.nativeswing.components.JVLCPlayer;

/**
 * @author Christopher Deckers
 */
public class SimpleVLCPlayerExample extends JPanel {

  public SimpleVLCPlayerExample() {
    super(new BorderLayout(0, 0));
    final JCheckBox controlBarCheckBox = new JCheckBox("Control Bar");
    JPanel playerPanel = new JPanel(new BorderLayout(0, 0));
    playerPanel.setBorder(BorderFactory.createTitledBorder("VLC Player component"));
    final JVLCPlayer player = new JVLCPlayer();
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints cons = new GridBagConstraints();
    JPanel playerFilePanel = new JPanel(gridBag);
    JLabel playerFileLabel = new JLabel("File: ");
    cons.gridx = 0;
    cons.gridy = 0;
    cons.insets = new Insets(2, 2, 2, 0);
    cons.fill = GridBagConstraints.HORIZONTAL;
    gridBag.setConstraints(playerFileLabel, cons);
    playerFilePanel.add(playerFileLabel);
    final JTextField playerFileTextField = new JTextField();
    cons.gridx++;
    cons.weightx = 1;
    gridBag.setConstraints(playerFileTextField, cons);
    final Runnable loadPlayerFileRunnable = new Runnable() {
      public void run() {
        player.load(playerFileTextField.getText());
      }
    };
    playerFileTextField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        loadPlayerFileRunnable.run();
      }
    });
    playerFilePanel.add(playerFileTextField);
    JButton playerFileButton = new JButton("...");
    cons.gridx++;
    cons.insets = new Insets(2, 2, 2, 2);
    cons.weightx = 0;
    gridBag.setConstraints(playerFileButton, cons);
    playerFileButton.addActionListener(new ActionListener() {
      JFileChooser fileChooser;
      public void actionPerformed(ActionEvent e) {
        if(fileChooser == null) {
          fileChooser = new JFileChooser();
        }
        if(fileChooser.showOpenDialog(SimpleVLCPlayerExample.this) == JFileChooser.APPROVE_OPTION) {
          File selectedFile = fileChooser.getSelectedFile();
          playerFileTextField.setText(selectedFile.getAbsolutePath());
          loadPlayerFileRunnable.run();
        }
      }
    });
    playerFilePanel.add(playerFileButton);
    add(playerFilePanel, BorderLayout.NORTH);
    playerPanel.add(player, BorderLayout.CENTER);
    add(playerPanel, BorderLayout.CENTER);
    // Create the check boxes, to show/hide the various bars
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
    player.setControlBarVisible(false);
    controlBarCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        player.setControlBarVisible(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
    buttonPanel.add(controlBarCheckBox);
    add(buttonPanel, BorderLayout.SOUTH);
  }
  
  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e) {}
    NativeInterface.open();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new SimpleVLCPlayerExample(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
  }
  
}
