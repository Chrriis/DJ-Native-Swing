/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.utilities.core;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.program.Program;

import chrriis.dj.nativeswing.swtimpl.CommandMessage;
import chrriis.dj.nativeswing.swtimpl.core.SWTUtils;
import chrriis.dj.nativeswing.swtimpl.utilities.internal.INativeFileTypeLauncher;
import chrriis.dj.nativeswing.swtimpl.utilities.internal.INativeFileTypeLauncherStatic;



/**
 * A utility class to get the launchers of certain file types.
 * @author Christopher Deckers
 */
class NativeFileTypeLauncherStatic implements INativeFileTypeLauncherStatic {

  static class FileTypeLauncherInfo {
    public static int nextID = 1;
    private int id = nextID++;
    private Program program;
    private List<String> registeredExtensionList;
    public FileTypeLauncherInfo(Program program) {
      this.program = program;
      idToFileTypeLauncherInfoMap.put(getID(), this);
    }
    private void addExtension(String extension) {
      if(registeredExtensionList == null) {
        registeredExtensionList = new ArrayList<String>(1);
      }
      if(!registeredExtensionList.contains(extension)) {
        registeredExtensionList.add(extension);
      }
    }
    public int getID() {
      return id;
    }
    public String[] getRegisteredExtensions() {
      return registeredExtensionList == null? new String[0]: registeredExtensionList.toArray(new String[0]);
    }
    public Program getProgram() {
      return program;
    }
    private boolean isIconInitialized;
    private ImageIcon icon;
    public ImageIcon getIcon() {
      if(!isIconInitialized) {
        isIconInitialized = true;
        ImageData imageData = program.getImageData();
        icon = imageData == null? null: new ImageIcon(SWTUtils.convertSWTImage(imageData));
      }
      return icon;
    }
  }

  private static boolean isProgramValid(Program program) {
    String name = program.getName();
    return name != null && name.length() > 0 /*&& program.getImageData() != null*/;
  }

  private static Map<Integer, FileTypeLauncherInfo> idToFileTypeLauncherInfoMap;
  private static Map<Program, FileTypeLauncherInfo> programToFileTypeLauncherInfoMap;

  private static boolean isNativeInitialized;

  private static void initNative() {
    if(isNativeInitialized) {
      return;
    }
    isNativeInitialized = true;
    programToFileTypeLauncherInfoMap = new HashMap<Program, FileTypeLauncherInfo>();
    idToFileTypeLauncherInfoMap = new HashMap<Integer, FileTypeLauncherInfo>();
  }

  /**
   * Explicitely load the data if it is not yet loaded, instead of letting the system perform the appropriate loading when needed.
   */
  public void load() {
    initializeExtensions();
    initializeLaunchers();
  }

  private static class CMN_initializeLaunchers extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      for(Program program: Program.getPrograms()) {
        if(!programToFileTypeLauncherInfoMap.containsKey(program) && isProgramValid(program) && program.getImageData() != null) {
          programToFileTypeLauncherInfoMap.put(program, new FileTypeLauncherInfo(program));
        }
      }
      return null;
    }
  }

  private static boolean hasInitializedLaunchers;

  private static void initializeLaunchers() {
    if(hasInitializedLaunchers) {
      return;
    }
    hasInitializedLaunchers = true;
    new CMN_initializeLaunchers().syncExec(true);
  }

  private static class CMN_initializeExtensions extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      for(String extension: Program.getExtensions()) {
        Program program = Program.findProgram(extension);
        if(program != null) {
          initNative();
          FileTypeLauncherInfo fileTypeLauncherInfo = programToFileTypeLauncherInfoMap.get(program);
          if(fileTypeLauncherInfo == null && isProgramValid(program)) {
            fileTypeLauncherInfo = new FileTypeLauncherInfo(program);
            programToFileTypeLauncherInfoMap.put(program, fileTypeLauncherInfo);
          }
          if(fileTypeLauncherInfo != null) {
            fileTypeLauncherInfo.addExtension(extension);
          }
        }
      }
      return null;
    }
  }

  private static boolean hasInitializedExtensions;

  void initializeExtensions() {
    if(hasInitializedExtensions) {
      return;
    }
    hasInitializedExtensions = true;
    new CMN_initializeExtensions().syncExec(true);
  }

  private static class CMN_getAllRegisteredExtensions extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      List<String> extensionList = new ArrayList<String>();
      for(FileTypeLauncherInfo launcherInfo: programToFileTypeLauncherInfoMap.values()) {
        for(String registeredExtension: launcherInfo.getRegisteredExtensions()) {
          extensionList.add(registeredExtension);
        }
      }
      return extensionList.toArray(new String[0]);
    }
  }

  /**
   * Get all the registered file extensions.
   * Some global loading of data is performed the first time, if it is needed.
   * @return all the registered file extensions.
   */
  public String[] getAllRegisteredExtensions() {
    initializeExtensions();
    return (String[])new CMN_getAllRegisteredExtensions().syncExec(true);
  }

  private static Map<Integer, NativeFileTypeLauncher> idToFileTypeLauncherMap;

  private static class CMN_getLauncherID extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      String extension = (String)args[0];
      Program program = Program.findProgram(extension);
      if(program == null) {
        return null;
      }
      initNative();
      FileTypeLauncherInfo fileTypeLauncher = programToFileTypeLauncherInfoMap.get(program);
      if(fileTypeLauncher == null && isProgramValid(program)) {
        fileTypeLauncher = new FileTypeLauncherInfo(program);
        programToFileTypeLauncherInfoMap.put(program, fileTypeLauncher);
      }
      if(fileTypeLauncher != null) {
        if(!hasInitializedExtensions) {
          fileTypeLauncher.addExtension(extension);
        }
        return fileTypeLauncher.getID();
      }
      return null;
    }
  }

  /**
   * Get the launcher for a given file name, which may or may not represent an existing file. The name can also simply be the extension of a file (including the '.').
   */
  public INativeFileTypeLauncher getLauncher(String fileName) {
    int index = fileName.lastIndexOf('.');
    if(index == -1) {
      return null;
    }
    final String extension = fileName.substring(index);
    Integer id = (Integer)new CMN_getLauncherID().syncExec(true, extension);
    if(id == null) {
      return null;
    }
    NativeFileTypeLauncher fileTypeLauncher = idToFileTypeLauncherMap.get(id);
    if(fileTypeLauncher == null) {
      fileTypeLauncher = new NativeFileTypeLauncher(this, id);
      idToFileTypeLauncherMap.put(id, fileTypeLauncher);
    }
    return fileTypeLauncher;
  }

  private static class CMN_getLauncherIDs extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      initNative();
      FileTypeLauncherInfo[] fileTypeLaunchers = programToFileTypeLauncherInfoMap.values().toArray(new FileTypeLauncherInfo[0]);
      Arrays.sort(fileTypeLaunchers, new Comparator<FileTypeLauncherInfo>() {
        public int compare(FileTypeLauncherInfo o1, FileTypeLauncherInfo o2) {
          return o1.getProgram().getName().toLowerCase().compareTo(o2.getProgram().getName().toLowerCase());
        }
      });
      int[] ids = new int[fileTypeLaunchers.length];
      for(int i=0; i<fileTypeLaunchers.length; i++) {
        ids[i] = fileTypeLaunchers[i].getID();
      }
      return ids;
    }
  }

  /**
   * Some global loading of data is performed the first time, if it is needed.
   */
  public INativeFileTypeLauncher[] getLaunchers() {
    load();
    int[] ids = (int[])new CMN_getLauncherIDs().syncExec(true);
    if(idToFileTypeLauncherMap == null) {
      idToFileTypeLauncherMap = new HashMap<Integer, NativeFileTypeLauncher>();
    }
    NativeFileTypeLauncher[] fileTypeLaunchers = new NativeFileTypeLauncher[ids.length];
    for(int i=0; i<ids.length; i++) {
      int id = ids[i];
      NativeFileTypeLauncher fileTypeLauncher = idToFileTypeLauncherMap.get(id);
      if(fileTypeLauncher == null) {
        fileTypeLauncher = new NativeFileTypeLauncher(this, id);
        idToFileTypeLauncherMap.put(id, fileTypeLauncher);
      }
      fileTypeLaunchers[i] = fileTypeLauncher;
    }
    return fileTypeLaunchers;
  }

  private static boolean isDefaultIconLoaded;
  private static ImageIcon defaultIcon;

  /**
   * Get the default icon for files that don't have a custom icon.
   * @return the default icon.
   */
  public ImageIcon getDefaultIcon() {
    if(!isDefaultIconLoaded) {
      isDefaultIconLoaded = true;
      Icon defaultIcon_;
      try {
        File tmpFile = File.createTempFile("~djn", "~.qwertyuiop");
        tmpFile.deleteOnExit();
        defaultIcon_ = FileSystemView.getFileSystemView().getSystemIcon(tmpFile);
        tmpFile.delete();
      } catch(Exception e) {
        defaultIcon_ = UIManager.getIcon("FileView.fileIcon");
      }
      if(!(defaultIcon_ instanceof ImageIcon)) {
        int width = defaultIcon_.getIconWidth();
        int height = defaultIcon_.getIconHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics gc = image.getGraphics();
//        gc.setColor(Color.WHITE);
//        gc.fillRect(0, 0, width, height);
        defaultIcon_.paintIcon(null, gc, 0, 0);
        gc.dispose();
        defaultIcon_ = new ImageIcon(image);
      }
      defaultIcon = (ImageIcon)defaultIcon_;
    }
    return defaultIcon;
  }

  /**
   * Get the size of the icons that can be obtained.
   * @return the size of the icons.
   */
  public Dimension getIconSize() {
    ImageIcon defaultIcon = getDefaultIcon();
    // TODO: check if a null default icon can happen, and if yes find alternate code
    return defaultIcon == null? new Dimension(16, 16): new Dimension(defaultIcon.getIconWidth(), defaultIcon.getIconHeight());
  }

  static FileTypeLauncherInfo getFileTypeLauncherInfo(Integer id) {
    return idToFileTypeLauncherInfoMap.get(id);
  }

}
