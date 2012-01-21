/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.nativedialogs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JDirectoryDialog;
import chrriis.dj.nativeswing.swtimpl.components.JFileDialog;
import chrriis.dj.nativeswing.swtimpl.components.JFileDialog.DialogType;
import chrriis.dj.nativeswing.swtimpl.components.JFileDialog.SelectionMode;

/**
 * @author Christopher Deckers
 */
public class NativeDialogs {

  public static JComponent createContent() {
    final JPanel contentPane = new JPanel(new BorderLayout());
    JPanel buttonPanel = new JPanel(new GridBagLayout());
    ButtonGroup buttonGroup = new ButtonGroup();
    GridBagConstraints cons = new GridBagConstraints();
    cons.fill = GridBagConstraints.HORIZONTAL;
    cons.gridy = 0;
    final JRadioButton basicRadioButton = new JRadioButton("Basic \"Open File\" dialog.", true);
    buttonGroup.add(basicRadioButton);
    buttonPanel.add(basicRadioButton, cons);
    cons.anchor = GridBagConstraints.WEST;
    cons.gridy++;
    final JRadioButton multiSelectionRadioButton = new JRadioButton("\"Open File\" dialog with multi selection.");
    buttonGroup.add(multiSelectionRadioButton);
    buttonPanel.add(multiSelectionRadioButton, cons);
    cons.gridy++;
    final JRadioButton filtersRadioButton = new JRadioButton("\"Save File\" dialog with extension filters.");
    buttonGroup.add(filtersRadioButton);
    buttonPanel.add(filtersRadioButton, cons);
    cons.gridy++;
    final JRadioButton directoryRadioButton = new JRadioButton("Basic Directory dialog.");
    buttonGroup.add(directoryRadioButton);
    buttonPanel.add(directoryRadioButton, cons);
    cons.gridy++;
    final JRadioButton customizedDirectoryRadioButton = new JRadioButton("Customized Directory dialog.");
    buttonGroup.add(customizedDirectoryRadioButton);
    buttonPanel.add(customizedDirectoryRadioButton, cons);
    cons.fill = GridBagConstraints.NONE;
    cons.anchor = GridBagConstraints.CENTER;
    cons.gridy++;
    final JButton showDialogButton = new JButton("Show Dialog");
    showDialogButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(basicRadioButton.isSelected()) {
          JFileDialog fileDialog = new JFileDialog();
          fileDialog.show(contentPane);
          JOptionPane.showMessageDialog(contentPane, "Selected file: " + fileDialog.getSelectedFileName());
          return;
        }
        if(multiSelectionRadioButton.isSelected()) {
          JFileDialog fileDialog = new JFileDialog();
          fileDialog.setSelectionMode(SelectionMode.MULTIPLE_SELECTION);
          fileDialog.show(contentPane);
          String fileNames = Arrays.toString(fileDialog.getSelectedFileNames());
          if(fileNames.length() > 100) {
            fileNames = fileNames.substring(0, 100) + "...";
          }
          JOptionPane.showMessageDialog(contentPane, "Selected files: " + fileNames);
          return;
        }
        if(filtersRadioButton.isSelected()) {
          JFileDialog fileDialog = new JFileDialog();
          fileDialog.setDialogType(DialogType.SAVE_DIALOG_TYPE);
          fileDialog.setExtensionFilters(new String[] {"*.*", "*.mp3;*.avi", "*.txt;*.doc"}, new String[] {"All files", "Multimedia file (*.mp3, *.avi)", "Text document (*.txt, *.doc)"}, 1);
          fileDialog.setConfirmedOverwrite(true);
          fileDialog.show(contentPane);
          JOptionPane.showMessageDialog(contentPane, "Selected file: " + fileDialog.getSelectedFileName());
          return;
        }
        if(directoryRadioButton.isSelected()) {
          JDirectoryDialog directoryDialog = new JDirectoryDialog();
          directoryDialog.show(contentPane);
          JOptionPane.showMessageDialog(contentPane, "Selected directory: " + directoryDialog.getSelectedDirectory());
          return;
        }
        if(customizedDirectoryRadioButton.isSelected()) {
          JDirectoryDialog directoryDialog = new JDirectoryDialog();
          directoryDialog.setTitle("This is a GREAT dialog!");
          directoryDialog.setMessage("Choose a directory NOW!");
          directoryDialog.show(contentPane);
          JOptionPane.showMessageDialog(contentPane, "Selected directory: " + directoryDialog.getSelectedDirectory());
          return;
        }
      }
    });
    buttonPanel.add(showDialogButton, cons);
    contentPane.add(buttonPanel, BorderLayout.CENTER);
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
