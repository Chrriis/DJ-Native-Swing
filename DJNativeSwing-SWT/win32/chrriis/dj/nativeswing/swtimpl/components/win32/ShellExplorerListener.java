/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.win32;

import java.util.EventListener;

/**
 * @author Christopher Deckers
 */
public interface ShellExplorerListener extends EventListener {

  public void documentComplete(ShellExplorerDocumentCompleteEvent e);

}
