/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.util.EventListener;

/**
 * @author Christopher Deckers
 */
public interface HTMLEditorListener extends EventListener {

  /**
   * Invoked when the save action was triggered on an HTML editor.
   * @param e the save event.
   */
  public void saveHTML(HTMLEditorSaveEvent e);

  /**
   * Invoked when dirty state is changed. The editor is dirty when its content has changed.
   * @param e the editor event.
   * @param isDirty indicate whether it is dirty.
   */
  public void notifyDirtyStateChanged(HTMLEditorEvent e, boolean isDirty);

}
