/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.Component;
import java.awt.Window;
import java.io.Serializable;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import chrriis.dj.nativeswing.swtimpl.ControlCommandMessage;
import chrriis.dj.nativeswing.swtimpl.ModalDialogUtils;
import chrriis.dj.nativeswing.swtimpl.NativeComponent;

/**
 * @author Christopher Deckers
 */
public class JDirectoryDialog {

  private static class NativeDirectoryDialogContainer extends NativeComponent {

    private static class CMN_openDirectoryDialog extends ControlCommandMessage {
      @Override
      public Object run(Object[] args) {
        Data data = (Data)args[0];
        Control control = getControl();
        DirectoryDialog directoryDialog = new DirectoryDialog(control.getShell(), SWT.NONE);
        if(data.title != null) {
          directoryDialog.setText(data.title);
        }
        if(data.initialDirectory != null) {
          directoryDialog.setFilterPath(data.initialDirectory);
        }
        if(data.message != null) {
          directoryDialog.setMessage(data.message);
        }
        data.selectedDirectoryName = directoryDialog.open();
        return data;
      }
    }

    protected static Control createControl(Shell shell, Object[] parameters) {
      return new Composite(shell, SWT.NONE);
    }

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
    public String selectedDirectoryName;
    public String initialDirectory;
  }

  private Data data = new Data();

  public void show(Window window) {
    final NativeDirectoryDialogContainer nativeComponent = new NativeDirectoryDialogContainer();
    ModalDialogUtils.showModalDialog(window, nativeComponent, new Runnable() {
      public void run() {
        data = nativeComponent.open(data);
      }
    });
  }

  public String getSelectedDirectoryName() {
    return data.selectedDirectoryName;
  }

  public String getInitialDirectory() {
    return data.initialDirectory;
  }

  public void setInitialDirectory(String initialiDirectory) {
    data.initialDirectory = initialiDirectory;
  }

  public void setTitle(String title) {
    data.title = title;
  }

  public String getTitle() {
    return data.title;
  }

  public void setMessage(String title) {
    data.message = title;
  }

  public String getMessage() {
    return data.message;
  }

}
