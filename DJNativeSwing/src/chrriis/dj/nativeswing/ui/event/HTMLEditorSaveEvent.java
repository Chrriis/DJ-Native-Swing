/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui.event;

import chrriis.dj.nativeswing.ui.JHTMLEditor;

/**
 * @author Christopher Deckers
 */
public class HTMLEditorSaveEvent extends HTMLEditorEvent {

  protected String text;

  public HTMLEditorSaveEvent(JHTMLEditor htmlEditor, String text) {
    super(htmlEditor);
    this.text = text;
  }
  
  public String getText() {
    return text;
  }
  
}
