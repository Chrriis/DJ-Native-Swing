/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo.examples.htmleditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.HTMLEditorAdapter;
import chrriis.dj.nativeswing.swtimpl.components.HTMLEditorDirtyStateEvent;
import chrriis.dj.nativeswing.swtimpl.components.JHTMLEditor;
import chrriis.dj.nativeswing.swtimpl.components.JHTMLEditor.HTMLEditorImplementation;

/**
 * @author Christopher Deckers
 */
public class EditorDirtyExample {

  protected static final String LS = System.getProperty("line.separator");

  public static JComponent createContent() {
    JPanel contentPane = new JPanel(new BorderLayout());
    final JHTMLEditor htmlEditor = new JHTMLEditor(HTMLEditorImplementation.TinyMCE);
    contentPane.add(htmlEditor, BorderLayout.CENTER);
    JPanel southPanel = new JPanel(new BorderLayout());
    JPanel dirtyPanel = new JPanel();
    dirtyPanel.setBorder(BorderFactory.createTitledBorder("Dirty State"));
    final JLabel dirtyLabel = new JLabel("Dirty: false");
    dirtyPanel.add(dirtyLabel);
    htmlEditor.addHTMLEditorListener(new HTMLEditorAdapter() {
      @Override
      public void notifyDirtyStateChanged(HTMLEditorDirtyStateEvent e) {
        dirtyLabel.setText("Dirty: " + e.isDirty());
      }
    });
    JButton markAsCleanButton = new JButton("Mark as clean");
    markAsCleanButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        htmlEditor.clearDirtyState();
      }
    });
    dirtyPanel.add(markAsCleanButton);
    southPanel.add(dirtyPanel, BorderLayout.NORTH);
    JPanel controlsPanel = new JPanel(new BorderLayout());
    controlsPanel.setBorder(BorderFactory.createTitledBorder("Custom Controls"));
    JPanel middlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton setHTMLButton = new JButton("Set HTML");
    middlePanel.add(setHTMLButton);
    JButton getHTMLButton = new JButton("Get HTML");
    middlePanel.add(getHTMLButton);
    controlsPanel.add(middlePanel, BorderLayout.NORTH);
    final JTextArea htmlTextArea = new JTextArea();
    htmlTextArea.setText(
        "<p style=\"text-align: center\">This is an <b>HTML editor</b>, in a <u><i>Swing</i></u> application.<br />" + LS +
        "<img alt=\"DJ Project Logo\" src=\"http://djproject.sourceforge.net/common/logo.png\" /><br />" + LS +
        "<a href=\"http://djproject.sourceforge.net/ns/\">DJ Project - Native Swing</a></p>"
    );
    htmlTextArea.setCaretPosition(0);
    JScrollPane scrollPane = new JScrollPane(htmlTextArea);
    scrollPane.setPreferredSize(new Dimension(0, 100));
    controlsPanel.add(scrollPane, BorderLayout.CENTER);
    southPanel.add(controlsPanel, BorderLayout.SOUTH);
    contentPane.add(southPanel, BorderLayout.SOUTH);
    getHTMLButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        htmlTextArea.setText(htmlEditor.getHTMLContent());
        htmlTextArea.setCaretPosition(0);
      }
    });
    setHTMLButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        htmlEditor.setHTMLContent(htmlTextArea.getText());
      }
    });
    htmlEditor.setHTMLContent(htmlTextArea.getText());
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
