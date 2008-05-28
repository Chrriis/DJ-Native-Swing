/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.htmleditor;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.NativeInterface;
import chrriis.dj.nativeswing.components.JHTMLEditor;

/**
 * @author Christopher Deckers
 */
public class CustomConfiguration extends JPanel {

  public CustomConfiguration() {
    super(new BorderLayout(0, 0));
    String configurationScript =
      "FCKConfig.ToolbarSets[\"Default\"] = [\n" +
      "['Source','DocProps','-','Save','NewPage','Preview','-','Templates']\n" +
      "];\n" +
      "FCKConfig.ToolbarCanCollapse = false;\n";
    JHTMLEditor htmlEditor = new JHTMLEditor(JHTMLEditor.setCustomJavascriptConfiguration(configurationScript));
    htmlEditor.setHTMLContent("<p>The toolbar was modified using custom configuration.</p>");
    add(htmlEditor, BorderLayout.CENTER);
  }
  
  /* Standard main method to try that test as a standalone application. */
  public static void main(String[] args) {
    UIUtils.setPreferredLookAndFeel();
    NativeInterface.open();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame("DJ Native Swing Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new CustomConfiguration(), BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
      }
    });
  }
  
}
