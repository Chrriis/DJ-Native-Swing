/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui.event;

import java.util.EventObject;

import chrriis.dj.nativeswing.ui.JHTMLEditor;

/**
 * @author Christopher Deckers
 */
public class HTMLEditorEvent extends EventObject{

  protected JHTMLEditor htmlEditor;
  
  public HTMLEditorEvent(JHTMLEditor htmlEditor) {
    super(htmlEditor);
    this.htmlEditor = htmlEditor;
  }
  
  public JHTMLEditor getHTMLEditor() {
    return htmlEditor;
  }
  
}
