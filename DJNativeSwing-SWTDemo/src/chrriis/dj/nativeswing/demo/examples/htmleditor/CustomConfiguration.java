/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.htmleditor;

import java.awt.BorderLayout;

import javax.swing.JPanel;

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
  
}
