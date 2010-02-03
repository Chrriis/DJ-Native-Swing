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
public class HTMLEditorDirtyStateEvent extends HTMLEditorEvent {

  private boolean isDirty;

  /**
   * Construct an HTML editor dirty state event.
   * @param htmlEditor the editor.
   * @param isDirty The dirty state that is being notified.
   */
  public HTMLEditorDirtyStateEvent(JHTMLEditor htmlEditor, boolean isDirty) {
    super(htmlEditor);
    this.isDirty = isDirty;
  }

  public boolean isDirty() {
    return isDirty;
  }

}
