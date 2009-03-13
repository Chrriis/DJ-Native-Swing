/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.util.EventObject;


/**
 * @author Christopher Deckers
 */
public class HTMLEditorEvent extends EventObject{

  private JHTMLEditor htmlEditor;

  /**
   * Construct an HTML editor event.
   * @param htmlEditor the HTML editor.
   */
  public HTMLEditorEvent(JHTMLEditor htmlEditor) {
    super(htmlEditor);
    this.htmlEditor = htmlEditor;
  }

  /**
   * Get the HTML editor.
   * @return the HTML editor.
   */
  public JHTMLEditor getHTMLEditor() {
    return htmlEditor;
  }

}
