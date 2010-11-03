/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.Component;

import chrriis.dj.nativeswing.swtimpl.components.internal.INativeDirectoryDialog;
import chrriis.dj.nativeswing.swtimpl.internal.NativeCoreObjectFactory;

/**
 * A native directory selection dialog.
 * @author Christopher Deckers
 */
public class JDirectoryDialog {

  private INativeDirectoryDialog nativeDirectoryDialog = NativeCoreObjectFactory.create(INativeDirectoryDialog.class, "chrriis.dj.nativeswing.swtimpl.components.core.NativeDirectoryDialog", new Class<?>[0], new Object[0]);

  /**
   * Show the directory selection dialog, which is a blocking call until the user has made a choice.
   * @param component The parent component.
   */
  public void show(Component component) {
    nativeDirectoryDialog.show(component);
  }

  /**
   * Get the directory that was selected or that should be selected when the dialog is shown.
   * @return the selected directory.
   */
  public String getSelectedDirectory() {
    return nativeDirectoryDialog.getSelectedDirectory();
  }

  /**
   * Set the directory that should be selected when the dialog is shown.
   * @param selectedDirectory the directory that should be selected.
   */
  public void setSelectedDirectory(String selectedDirectory) {
    nativeDirectoryDialog.setSelectedDirectory(selectedDirectory);
  }

  /**
   * Set the title of the file dialog.
   * @param title the title to set or null to use the default title.
   */
  public void setTitle(String title) {
    nativeDirectoryDialog.setTitle(title);
  }

  /**
   * Get the title of the file dialog.
   * @return the title or null if it is not set.
   */
  public String getTitle() {
    return nativeDirectoryDialog.getTitle();
  }

  /**
   * Set the message that is shown to the user to describe the purpose of this directory selection.
   * @param message the message to show.
   */
  public void setMessage(String message) {
    nativeDirectoryDialog.setMessage(message);
  }

  /**
   * Get the message that is shown to the user to describe the purpose of this directory selection.
   * @return the message or null if it is not set.
   */
  public String getMessage() {
    return nativeDirectoryDialog.getMessage();
  }

}
