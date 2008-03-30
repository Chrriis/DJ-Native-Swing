/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components;

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

}
