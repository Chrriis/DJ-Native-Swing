/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.core;

import java.awt.Component;
import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

import chrriis.dj.nativeswing.swtimpl.components.internal.INativeDirectoryDialog;
import chrriis.dj.nativeswing.swtimpl.core.ControlCommandMessage;
import chrriis.dj.nativeswing.swtimpl.core.NativeModalDialogHandler;

/**
 * @author Christopher Deckers
 */
class NativeDirectoryDialog implements INativeDirectoryDialog {

  private static class Data implements Serializable {
    public String title;
    public String message;
    public String selectedDirectory;
  }

  private Data data = new Data();

  private static class CMN_openDirectoryDialog extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      Data data = (Data)args[0];
      Control control = getControl();
      if(control.isDisposed()) {
        return data;
      }
      org.eclipse.swt.widgets.DirectoryDialog directoryDialog = new org.eclipse.swt.widgets.DirectoryDialog(control.getShell(), SWT.NONE);
      if(data.title != null) {
        directoryDialog.setText(data.title);
      }
      if(data.selectedDirectory != null) {
        directoryDialog.setFilterPath(data.selectedDirectory);
      }
      if(data.message != null) {
        directoryDialog.setMessage(data.message);
      }
      data.selectedDirectory = directoryDialog.open();
      return data;
    }
  }

  public void show(Component component) {
    new NativeModalDialogHandler() {
      @Override
      protected void processResult(Object result) {
        data = (Data)result;
      }
    }.showModalDialog(component, new CMN_openDirectoryDialog(), data);
  }

  /**
   * Get the directory that was selected or that should be selected when the dialog is shown.
   * @return the selected directory.
   */
  public String getSelectedDirectory() {
    return data.selectedDirectory;
  }

  /**
   * Set the directory that should be selected when the dialog is shown.
   * @param selectedDirectory the directory that should be selected.
   */
  public void setSelectedDirectory(String selectedDirectory) {
    data.selectedDirectory = selectedDirectory;
  }

  /**
   * Set the title of the file dialog.
   * @param title the title to set or null to use the default title.
   */
  public void setTitle(String title) {
    data.title = title;
  }

  /**
   * Get the title of the file dialog.
   * @return the title or null if it is not set.
   */
  public String getTitle() {
    return data.title;
  }

  /**
   * Set the message that is shown to the user to describe the purpose of this directory selection.
   * @param message the message to show.
   */
  public void setMessage(String message) {
    data.message = message;
  }

  /**
   * Get the message that is shown to the user to describe the purpose of this directory selection.
   * @return the message or null if it is not set.
   */
  public String getMessage() {
    return data.message;
  }

}
