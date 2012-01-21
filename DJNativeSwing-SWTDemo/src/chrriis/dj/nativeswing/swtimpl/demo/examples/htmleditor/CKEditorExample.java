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
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.HTMLEditorAdapter;
import chrriis.dj.nativeswing.swtimpl.components.HTMLEditorSaveEvent;
import chrriis.dj.nativeswing.swtimpl.components.JHTMLEditor;

/**
 * @author Christopher Deckers
 */
public class CKEditorExample {

  protected static final String LS = System.getProperty("line.separator");

  public static JComponent createContent() {
    final JPanel contentPane = new JPanel(new BorderLayout());
    Map<String, String> optionMap = new HashMap<String, String>();
    optionMap.put("toolbar", "[" +
        "  ['Source','-','Save','NewPage','Preview','-','Templates']," +
        "  ['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print', 'SpellChecker', 'Scayt']," +
        "  ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat']," +
        "  ['Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField']," +
        "  '/'," +
        "  ['Bold','Italic','Underline','Strike','-','Subscript','Superscript']," +
        "  ['NumberedList','BulletedList','-','Outdent','Indent','Blockquote']," +
        "  ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock']," +
        "  ['Link','Unlink','Anchor']," +
        "  ['Image','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak']," +
        "  '/'," +
        "  ['Styles','Format','Font','FontSize']," +
        "  ['TextColor','BGColor']," +
        "  ['Maximize', 'ShowBlocks','-','About']" +
    "]");
//    optionMap.put("uiColor", "'#9AB8F3'");
//    optionMap.put("toolbarCanCollapse", "false");
    final JHTMLEditor htmlEditor = new JHTMLEditor(JHTMLEditor.HTMLEditorImplementation.CKEditor,
        JHTMLEditor.CKEditorOptions.setOptions(optionMap)
    );
    htmlEditor.addHTMLEditorListener(new HTMLEditorAdapter() {
      @Override
      public void saveHTML(HTMLEditorSaveEvent e) {
        JOptionPane.showMessageDialog(contentPane, "The data of the HTML editor could be saved anywhere...");
      }
    });
    contentPane.add(htmlEditor, BorderLayout.CENTER);
    JPanel southPanel = new JPanel(new BorderLayout());
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
