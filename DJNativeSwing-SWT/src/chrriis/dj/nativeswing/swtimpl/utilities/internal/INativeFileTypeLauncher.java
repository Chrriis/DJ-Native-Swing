/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.utilities.internal;

import javax.swing.ImageIcon;


public interface INativeFileTypeLauncher {

  public String[] getRegisteredExtensions();

  public String getName();

  public ImageIcon getIcon();

  public void launch(String filePath);

}
