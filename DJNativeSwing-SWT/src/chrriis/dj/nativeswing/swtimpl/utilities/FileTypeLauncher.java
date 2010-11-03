/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.utilities;

import java.awt.Dimension;

import javax.swing.ImageIcon;

import chrriis.dj.nativeswing.swtimpl.internal.NativeCoreObjectFactory;
import chrriis.dj.nativeswing.swtimpl.utilities.internal.INativeFileTypeLauncher;
import chrriis.dj.nativeswing.swtimpl.utilities.internal.INativeFileTypeLauncherStatic;



/**
 * A utility class to get the launchers of certain file types.
 * @author Christopher Deckers
 */
public class FileTypeLauncher {

  private static INativeFileTypeLauncherStatic fileTypeLauncherStatic = NativeCoreObjectFactory.create(INativeFileTypeLauncherStatic.class, "chrriis.dj.nativeswing.swtimpl.utilities.core.NativeFileTypeLauncherStatic", new Class<?>[0], new Object[0]);
  private INativeFileTypeLauncher fileTypeLauncher;

  public FileTypeLauncher() {
    this(NativeCoreObjectFactory.create(INativeFileTypeLauncher.class, "chrriis.dj.nativeswing.swtimpl.utilities.core.NativeFileTypeLauncher", new Class<?>[0], new Object[0]));
  }

  FileTypeLauncher(INativeFileTypeLauncher fileTypeLauncher) {
    this.fileTypeLauncher = fileTypeLauncher;
  }

  /**
   * Explicitely load the data if it is not yet loaded, instead of letting the system perform the appropriate loading when needed.
   */
  public static void load() {
    fileTypeLauncherStatic.load();
  }

  /**
   * Get all the registered file extensions.
   * Some global loading of data is performed the first time, if it is needed.
   * @return all the registered file extensions.
   */
  public static String[] getAllRegisteredExtensions() {
    return fileTypeLauncherStatic.getAllRegisteredExtensions();
  }

  /**
   * Get the extensions of the files that can be launched.
   * Some global loading of data is performed the first time, if it is needed.
   * @return the file extensions.
   */
  public String[] getRegisteredExtensions() {
    return fileTypeLauncher.getRegisteredExtensions();
  }

  /**
   * Get the name of the launcher.
   * @return the name.
   */
  public String getName() {
    return fileTypeLauncher.getName();
  }

  /**
   * Get the icon associated with this file type.
   * @return the icon, which could be the default icon.
   */
  public ImageIcon getIcon() {
    return fileTypeLauncher.getIcon();
  }

  @Override
  public boolean equals(Object o) {
    return fileTypeLauncher.equals(((FileTypeLauncher)o).fileTypeLauncher);
  }

  @Override
  public int hashCode() {
    return fileTypeLauncher.hashCode();
  }

  /**
   * Launch a file using this launcher.
   * @param filePath the path to the file to launch.
   */
  public void launch(String filePath) {
    fileTypeLauncher.launch(filePath);
  }

  /**
   * Get the launcher for a given file name, which may or may not represent an existing file. The name can also simply be the extension of a file (including the '.').
   */
  public static FileTypeLauncher getLauncher(String fileName) {
    INativeFileTypeLauncher launcher = fileTypeLauncherStatic.getLauncher(fileName);
    return launcher == null? null: new FileTypeLauncher(launcher);
  }

  /**
   * Some global loading of data is performed the first time, if it is needed.
   */
  public static FileTypeLauncher[] getLaunchers() {
    INativeFileTypeLauncher[] nLaunchers = fileTypeLauncherStatic.getLaunchers();
    FileTypeLauncher[] launchers = new FileTypeLauncher[nLaunchers.length];
    for(int i=0; i<launchers.length; i++) {
      launchers[i] = new FileTypeLauncher(nLaunchers[i]);
    }
    return launchers;
  }

  /**
   * Get the default icon for files that don't have a custom icon.
   * @return the default icon.
   */
  public static ImageIcon getDefaultIcon() {
    return fileTypeLauncherStatic.getDefaultIcon();
  }

  /**
   * Get the size of the icons that can be obtained.
   * @return the size of the icons.
   */
  public static Dimension getIconSize() {
    return fileTypeLauncherStatic.getIconSize();
  }

}
