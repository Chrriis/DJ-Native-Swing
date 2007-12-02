/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.directorydialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import chrriis.dj.nativeswing.ui.JDirectoryChooser;

/**
 * @author Christopher Deckers
 */
public class DirectorySelection extends JPanel {

  protected String NO_DIRECTORY_MESSAGE = "No directory is selected.";
  
  public DirectorySelection() {
    setLayout(new GridBagLayout());
    JPanel contentPane = new JPanel(new BorderLayout(0, 0));
    final JLabel selectedDirectoryLabel = new JLabel(NO_DIRECTORY_MESSAGE);
    selectedDirectoryLabel.setHorizontalAlignment(JLabel.CENTER);
    contentPane.add(selectedDirectoryLabel, BorderLayout.SOUTH);
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    JButton directoryButton = new JButton("Select a directory");
    directoryButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JDirectoryChooser directoryChooser = new JDirectoryChooser();
        if(directoryChooser.showDialog(DirectorySelection.this) == JDirectoryChooser.APPROVE_OPTION) {
          selectedDirectoryLabel.setText(directoryChooser.getDirectory().getAbsolutePath());
        } else {
          selectedDirectoryLabel.setText(NO_DIRECTORY_MESSAGE);
        }
      }
    });
    buttonPanel.add(directoryButton);
    contentPane.add(buttonPanel, BorderLayout.NORTH);
    add(contentPane);
  }
  
}
