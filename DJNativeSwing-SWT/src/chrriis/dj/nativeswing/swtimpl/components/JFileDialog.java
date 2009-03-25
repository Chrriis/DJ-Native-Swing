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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import chrriis.dj.nativeswing.swtimpl.ControlCommandMessage;
import chrriis.dj.nativeswing.swtimpl.ModalDialogUtils;
import chrriis.dj.nativeswing.swtimpl.NativeComponent;

/**
 * @author Christopher Deckers
 */
public class JFileDialog {

  private static class NativeFileDialogContainer extends NativeComponent {

    private static class CMN_openFileDialog extends ControlCommandMessage {
      @Override
      public Object run(Object[] args) {
        Data data = (Data)args[0];
        Control control = getControl();
        int style = 0;
        if(data.isSave) {
          style |= SWT.SAVE;
        }
        if(data.isMulti) {
          style |= SWT.MULTI;
        }
        FileDialog fileDialog = new FileDialog(control.getShell(), style);
        if(data.title != null) {
          fileDialog.setText(data.title);
        }
        if(data.initialDirectory != null) {
          fileDialog.setFilterPath(data.initialDirectory);
        }
        if(data.selectedFileNames != null) {
          fileDialog.setFileName(data.selectedFileNames[0]);
        }
        if(data.filterExtensions != null) {
          fileDialog.setFilterExtensions(data.filterExtensions);
          fileDialog.setFilterNames(data.filterDescriptions);
          fileDialog.setFilterIndex(data.selectedFilterIndex);
        }
        fileDialog.open();
        data.selectedFileNames = fileDialog.getFileNames();
        data.selectedFilterIndex = fileDialog.getFilterIndex();
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
      return (Data)new CMN_openFileDialog().syncExec(this, data);
    }

  }

  private static class Data implements Serializable {
    public String title;
    public boolean isSave;
    public boolean isMulti;
    public String[] selectedFileNames;
    public String[] filterDescriptions;
    public String[] filterExtensions;
    public int selectedFilterIndex;
    public String initialDirectory;
  }

  private Data data = new Data();

  public void show(Window window) {
    final NativeFileDialogContainer nativeComponent = new NativeFileDialogContainer();
    ModalDialogUtils.showModalDialog(window, nativeComponent, new Runnable() {
      public void run() {
        data = nativeComponent.open(data);
      }
    });
  }

  public String getSelectedFileName() {
    String[] selectedFileNames = getSelectedFileNames();
    return selectedFileNames.length >= 1? selectedFileNames[0]: null;
  }

  public String[] getSelectedFileNames() {
    if(data.selectedFileNames == null) {
      return new String[0];
    }
    String[] selectedFileNames = new String[data.selectedFileNames.length];
    System.arraycopy(data.selectedFileNames, 0, selectedFileNames, 0, selectedFileNames.length);
    return selectedFileNames;
  }

  public void setSelectedFileName(String selectedFileName) {
    data.selectedFileNames = new String[] {selectedFileName};
  }

  public static enum SelectionMode {
    SINGLE_SELECTION,
    MULTIPLE_SELECTION,
  }

  public SelectionMode getSelectionMode() {
    return data.isMulti? SelectionMode.MULTIPLE_SELECTION: SelectionMode.SINGLE_SELECTION;
  }

  public void setSelectionMode(SelectionMode selectionMode) {
    data.isMulti = selectionMode == SelectionMode.MULTIPLE_SELECTION;
  }

  public static enum DialogType {
    OPEN_DIALOG_TYPE,
    SAVE_DIALOG_TYPE,
  }

  public DialogType getDialogType() {
    return data.isSave? DialogType.SAVE_DIALOG_TYPE: DialogType.OPEN_DIALOG_TYPE;
  }

  public void setDialogType(DialogType dialogType) {
    data.isSave = dialogType == DialogType.SAVE_DIALOG_TYPE;
  }

  public String getInitialDirectory() {
    return data.initialDirectory;
  }

  public void setInitialDirectory(String initialiDirectory) {
    data.initialDirectory = initialiDirectory;
  }

  public void setExtensionFilters(String[] filterExtensions, String[] filterDescriptions, int selectedFilterIndex) {
    data.filterExtensions = filterExtensions;
    data.filterDescriptions = filterDescriptions;
    data.selectedFilterIndex = selectedFilterIndex;
  }

  public String[] getFilterExtensions() {
    return data.filterExtensions;
  }

  public String[] getFilterDescriptions() {
    return data.filterDescriptions;
  }

  public int getSelectedFilterIndex() {
    return data.selectedFilterIndex;
  }

  public void setTitle(String title) {
    data.title = title;
  }

  public String getTitle() {
    return data.title;
  }

}
