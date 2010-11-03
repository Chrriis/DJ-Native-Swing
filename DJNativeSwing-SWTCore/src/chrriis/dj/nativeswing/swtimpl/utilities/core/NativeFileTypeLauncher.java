/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.utilities.core;

import javax.swing.ImageIcon;

import chrriis.dj.nativeswing.swtimpl.CommandMessage;
import chrriis.dj.nativeswing.swtimpl.utilities.internal.INativeFileTypeLauncher;



/**
 * A utility class to get the launchers of certain file types.
 * @author Christopher Deckers
 */
class NativeFileTypeLauncher implements INativeFileTypeLauncher {

  private NativeFileTypeLauncherStatic fileTypeLauncherStatic;
  private int id;

  NativeFileTypeLauncher(NativeFileTypeLauncherStatic fileTypeLauncherStatic, int id) {
    this.fileTypeLauncherStatic = fileTypeLauncherStatic;
    this.id = id;
  }

  private static class CMN_getRegisteredExtensions extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      return NativeFileTypeLauncherStatic.getFileTypeLauncherInfo((Integer)args[0]).getRegisteredExtensions();
    }
  }

  private String[] registeredExtensions;

  /**
   * Get the extensions of the files that can be launched.
   * Some global loading of data is performed the first time, if it is needed.
   * @return the file extensions.
   */
  public String[] getRegisteredExtensions() {
    if(registeredExtensions == null) {
      fileTypeLauncherStatic.initializeExtensions();
      registeredExtensions = (String[])new CMN_getRegisteredExtensions().syncExec(true, id);
    }
    return registeredExtensions;
  }

  private String name;

  private static class CMN_getName extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      return NativeFileTypeLauncherStatic.getFileTypeLauncherInfo((Integer)args[0]).getProgram().getName();
    }
  }

  /**
   * Get the name of the launcher.
   * @return the name.
   */
  public String getName() {
    if(name == null) {
      name = (String)new CMN_getName().syncExec(true, id);
    }
    return name;
  }

  private ImageIcon icon;
  private boolean isIconInitialized;

  private static class CMN_getIcon extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      return NativeFileTypeLauncherStatic.getFileTypeLauncherInfo((Integer)args[0]).getIcon();
    }
  }

  /**
   * Get the icon associated with this file type.
   * @return the icon, which could be the default icon.
   */
  public ImageIcon getIcon() {
    if(!isIconInitialized) {
      isIconInitialized = true;
      icon = (ImageIcon)new CMN_getIcon().syncExec(true, id);
    }
    return icon == null? fileTypeLauncherStatic.getDefaultIcon(): icon;
  }

  @Override
  public boolean equals(Object o) {
    return this == o;
  }

  private Integer hashCode;

  private static class CMN_hashCode extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      return NativeFileTypeLauncherStatic.getFileTypeLauncherInfo((Integer)args[0]).getProgram().hashCode();
    }
  }

  @Override
  public int hashCode() {
    if(hashCode == null) {
      hashCode = (Integer)new CMN_hashCode().syncExec(true, id);
    }
    return hashCode;
  }

  private static class CMN_launch extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      NativeFileTypeLauncherStatic.getFileTypeLauncherInfo((Integer)args[0]).getProgram().execute((String)args[1]);
      return null;
    }
  }

  /**
   * Launch a file using this launcher.
   * @param filePath the path to the file to launch.
   */
  public void launch(String filePath) {
    new CMN_launch().asyncExec(true, id, filePath);
  }

}
