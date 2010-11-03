/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.Component;

import chrriis.dj.nativeswing.swtimpl.components.internal.INativeFileDialog;
import chrriis.dj.nativeswing.swtimpl.internal.NativeCoreObjectFactory;

/**
 * A native file selection dialog.
 * @author Christopher Deckers
 */
public class JFileDialog {

  private INativeFileDialog nativeFileDialog = NativeCoreObjectFactory.create(INativeFileDialog.class, "chrriis.dj.nativeswing.swtimpl.components.core.NativeFileDialog", new Class<?>[0], new Object[0]);

  /**
   * Show the file selection dialog, which is a blocking call until the user has made a choice.
   * @param component The parent component.
   */
  public void show(Component component) {
    nativeFileDialog.show(component);
  }

  /**
   * Get the file name of the first file that was selected relative to the parent directory.
   * @return the file name or null if no file was selected.
   */
  public String getSelectedFileName() {
    return nativeFileDialog.getSelectedFileName();
  }

  /**
   * Get the file names of the files that was selected relative to the parent directory.
   * @return the file names or null if no file was selected.
   */
  public String[] getSelectedFileNames() {
    return nativeFileDialog.getSelectedFileNames();
  }

  /**
   * Set the file name that the dialog will select by default.
   * @param selectedFileName the name of the file to select or null if no file is to be selected.
   */
  public void setSelectedFileName(String selectedFileName) {
    nativeFileDialog.setSelectedFileName(selectedFileName);
  }

  /**
   * A representation of the selection modes that a file dialog supports.
   * @author Christopher Deckers
   */
  public static enum SelectionMode {
    SINGLE_SELECTION,
    MULTIPLE_SELECTION,
  }

  /**
   * Get the selection mode.
   * @return the selection mode.
   */
  public SelectionMode getSelectionMode() {
    return nativeFileDialog.getSelectionMode();
  }

  /**
   * Set the selection mode.
   * @param selectionMode the selection mode to set.
   */
  public void setSelectionMode(SelectionMode selectionMode) {
    nativeFileDialog.setSelectionMode(selectionMode);
  }

  /**
   * The types that a file dialog supports.
   * @author Christopher Deckers
   */
  public static enum DialogType {
    OPEN_DIALOG_TYPE,
    SAVE_DIALOG_TYPE,
  }

  /**
   * Get the dialog type.
   * @return the dialog type.
   */
  public DialogType getDialogType() {
    return nativeFileDialog.getDialogType();
  }

  /**
   * Set the dialog type.
   * @param dialogType the type of dialog.
   */
  public void setDialogType(DialogType dialogType) {
    nativeFileDialog.setDialogType(dialogType);
  }

  /**
   * For save dialogs, indicates whether the selection of a file should confirm the selection of an existing file.
   * @param isConfirmedOverwrite indicate whether selecting an existing file should confirm overwriting it.
   */
  public void setConfirmedOverwrite(boolean isConfirmedOverwrite) {
    nativeFileDialog.setConfirmedOverwrite(isConfirmedOverwrite);
  }

  /**
   * Indicate whether save dialogs should ask for confirmation when the file that is selected already exists.
   * @return true when the dialog should ask for confirmation, false otherwise.
   */
  public boolean isConfirmedOverwrite() {
    return nativeFileDialog.isConfirmedOverwrite();
  }

  /**
   * Get the parent directory of the files that will be/were shown.
   * @return The parent directory or null if it is not set.
   */
  public String getParentDirectory() {
    return nativeFileDialog.getParentDirectory();
  }

  /**
   * Set the parent directory of the files that will be shown.
   * @param parentDirectory The parent directory or null to use the default.
   */
  public void setParentDirectory(String parentDirectory) {
    nativeFileDialog.setParentDirectory(parentDirectory);
  }

  /**
   * Set the extension filters, with optional descriptions, and set which index is selected by default.
   * To clear the list, set both arrays to null.
   * @param extensionFilters an array of extensions typically in the form "*.extension" or null. A filter with multiple extension uses semicolon as a separator, e.g. "*.jpg;*.png".
   * @param extensionFiltersNames the array of names corresponding to each extension filter, or null.
   * @param selectedExtensionFilterIndex the index that is selected by default.
   */
  public void setExtensionFilters(String[] extensionFilters, String[] extensionFiltersNames, int selectedExtensionFilterIndex) {
    nativeFileDialog.setExtensionFilters(extensionFilters, extensionFiltersNames, selectedExtensionFilterIndex);
  }

  /**
   * Get the extension filters.
   * @return the extension filters, or null if not set.
   */
  public String[] getExtensionFilters() {
    return nativeFileDialog.getExtensionFilters();
  }

  /**
   * Get the names corresponding to the extension filters.
   * @return the extension filters names, or null if not set.
   */
  public String[] getExtensionFiltersNames() {
    return nativeFileDialog.getExtensionFiltersNames();
  }

  /**
   * Get the index of the selected extension filter.
   * @return the selected extension filter index.
   */
  public int getSelectedExtensionFilterIndex() {
    return nativeFileDialog.getSelectedExtensionFilterIndex();
  }

  /**
   * Set the title of the file dialog.
   * @param title the title to set or null to use the default title.
   */
  public void setTitle(String title) {
    nativeFileDialog.setTitle(title);
  }

  /**
   * Get the title of the file dialog.
   * @return the title or null if it is not set.
   */
  public String getTitle() {
    return nativeFileDialog.getTitle();
  }

}
