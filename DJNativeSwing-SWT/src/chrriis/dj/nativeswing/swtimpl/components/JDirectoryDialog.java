/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.Component;
import java.io.Serializable;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import chrriis.dj.nativeswing.swtimpl.ControlCommandMessage;
import chrriis.dj.nativeswing.swtimpl.components.ModalDialogUtils.NativeModalComponent;

/**
 * A native directory selection dialog.
 * @author Christopher Deckers
 */
public class JDirectoryDialog {

  private static class NativeDirectoryDialogContainer extends NativeModalComponent {

    private static class CMN_openDirectoryDialog extends ControlCommandMessage {
      @Override
      public Object run(Object[] args) {
        Data data = (Data)args[0];
        Control control = getControl();
        DirectoryDialog directoryDialog = new DirectoryDialog(control.getShell(), SWT.NONE);
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

    protected static Control createControl(Shell shell, Object[] parameters) {
      return new Composite(shell, SWT.NONE);
    }

    @Override
    protected Component createEmbeddableComponent() {
      return super.createEmbeddableComponent(new HashMap<Object, Object>());
    }

    public Data open(Data data) {
      return (Data)new CMN_openDirectoryDialog().syncExec(this, data);
    }

  }

  private static class Data implements Serializable {
    public String title;
    public String message;
    public String selectedDirectory;
  }

  private Data data = new Data();

  /**
   * Show the directory selection dialog, which is a blocking call until the user has made a choice.
   * @param component The parent component.
   */
  public void show(Component component) {
    final NativeDirectoryDialogContainer nativeComponent = new NativeDirectoryDialogContainer();
    ModalDialogUtils.showModalDialog(component, nativeComponent, new Runnable() {
      public void run() {
        data = nativeComponent.open(data);
      }
    });
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
