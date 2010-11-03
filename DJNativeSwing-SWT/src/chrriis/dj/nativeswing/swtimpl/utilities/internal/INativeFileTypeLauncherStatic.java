/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.utilities.internal;

import java.awt.Dimension;

import javax.swing.ImageIcon;


public interface INativeFileTypeLauncherStatic {

  public void load();

  public String[] getAllRegisteredExtensions();

  public INativeFileTypeLauncher getLauncher(String fileName);

  public INativeFileTypeLauncher[] getLaunchers();

  public ImageIcon getDefaultIcon();

  public Dimension getIconSize();

}
