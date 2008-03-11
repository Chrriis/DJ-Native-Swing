/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.htmleditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import chrriis.dj.nativeswing.ui.JHTMLEditor;
import chrriis.dj.nativeswing.ui.event.HTMLEditorListener;
import chrriis.dj.nativeswing.ui.event.HTMLEditorSaveEvent;

/**
 * @author Christopher Deckers
 */
public class SimpleHTMLEditorExample extends JPanel {

  protected static final String LS = System.getProperty("line.separator");

  public SimpleHTMLEditorExample() {
    super(new BorderLayout(0, 0));
    final JHTMLEditor htmlEditor = new JHTMLEditor();
    htmlEditor.addHTMLEditorListener(new HTMLEditorListener() {
      public void saveHTML(HTMLEditorSaveEvent e) {
        JOptionPane.showMessageDialog(SimpleHTMLEditorExample.this, "The data of the HTML editor could be saved anywhere...");
      }
    });
    add(htmlEditor, BorderLayout.CENTER);
    JPanel southPanel = new JPanel(new BorderLayout(0, 0));
    southPanel.setBorder(BorderFactory.createTitledBorder("Custom Controls"));
    JPanel middlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton setHTMLButton = new JButton("Set HTML");
    middlePanel.add(setHTMLButton);
    JButton getHTMLButton = new JButton("Get HTML");
    middlePanel.add(getHTMLButton);
    southPanel.add(middlePanel, BorderLayout.NORTH);
    final JTextArea htmlTextArea = new JTextArea();
    htmlTextArea.setText(
        "<p style=\"text-align: center\">This is an <b>HTML editor</b>, in a <u><i>Swing</i></u> application.<br />" + LS +
        "<img alt=\"DJ Project Logo\" src=\"http://djproject.sourceforge.net/common/logo.png\" /><br />" + LS +
        "<a href=\"http://djproject.sourceforge.net/ns/\">DJ Project - Native Swing</a></p>"
    );
    htmlTextArea.setCaretPosition(0);
    JScrollPane scrollPane = new JScrollPane(htmlTextArea);
    scrollPane.setPreferredSize(new Dimension(0, 100));
    southPanel.add(scrollPane, BorderLayout.CENTER);
    add(southPanel, BorderLayout.SOUTH);
    getHTMLButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        htmlTextArea.setText(htmlEditor.getHTML());
        htmlTextArea.setCaretPosition(0);
      }
    });
    setHTMLButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        htmlEditor.setHTML(htmlTextArea.getText());
      }
    });
    htmlEditor.setHTML(htmlTextArea.getText());
  }
  
}
