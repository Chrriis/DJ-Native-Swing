/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;


/**
 * @author Christopher Deckers
 */
public class HTMLEditorSaveEvent extends HTMLEditorEvent {

  private String text;

  /**
   * Construct an HTML editor save event.
   * @param htmlEditor the editor.
   * @param text The text that is asked to be saved.
   */
  public HTMLEditorSaveEvent(JHTMLEditor htmlEditor, String text) {
    super(htmlEditor);
    this.text = text;
  }

  /**
   * Get the text to save.
   */
  public String getText() {
    return text;
  }

}
