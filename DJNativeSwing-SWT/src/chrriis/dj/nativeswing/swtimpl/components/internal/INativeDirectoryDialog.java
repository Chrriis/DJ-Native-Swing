/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.internal;

import java.awt.Component;

/**
 * @author Christopher Deckers
 */
public interface INativeDirectoryDialog {

  public void show(Component component);

  public String getSelectedDirectory();

  public void setSelectedDirectory(String selectedDirectory);

  public void setTitle(String title);

  public String getTitle();

  public void setMessage(String message);

  public String getMessage();

}
