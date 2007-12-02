/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

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

import chrriis.dj.nativeswing.ui.Utils;

/**
 * @author Christopher Deckers
 */
public class FileTypeLauncher {

  protected Program program;
  protected List<String> registeredExtensionList;
  protected int hashCode;
  
  protected FileTypeLauncher(Program program) {
    this.program = program;
    hashCode = program.hashCode();
  }
  
  protected void addExtension(String extension) {
    if(registeredExtensionList == null) {
      registeredExtensionList = new ArrayList<String>(1);
    }
    if(!registeredExtensionList.contains(extension)) {
      registeredExtensionList.add(extension);
    }
  }
  
  /**
   * Some global loading of data is performed the first time, if it is needed.
   */
  public static FileTypeLauncher[] getLaunchers() {
    load();
    FileTypeLauncher[] fileTypeLaunchers = programToFileTypeLauncherMap.values().toArray(new FileTypeLauncher[0]);
    Arrays.sort(fileTypeLaunchers, new Comparator<FileTypeLauncher>() {
      public int compare(FileTypeLauncher o1, FileTypeLauncher o2) {
        return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
      }
    });
    return fileTypeLaunchers;
  }

  /**
   * Explicitely load the data if it is not yet loaded, instead of letting the system perform the appropriate loading when needed.
   */
  public static void load() {
    initializeExtensions();
    initializeLaunchers();
  }
  
  protected static boolean hasInitializedExtensions;
  
  protected static void initializeExtensions() {
    if(hasInitializedExtensions) {
      return;
    }
    hasInitializedExtensions = true;
    NativeInterfaceHandler.invokeSWT(new Runnable() {
      public void run() {
        for(String extension: Program.getExtensions()) {
          Program program = Program.findProgram(extension);
          if(program != null) {
            FileTypeLauncher fileTypeLauncher = programToFileTypeLauncherMap.get(program);
            if(fileTypeLauncher == null && isProgramValid(program)) {
              fileTypeLauncher = new FileTypeLauncher(program);
              programToFileTypeLauncherMap.put(program, fileTypeLauncher);
            }
            if(fileTypeLauncher != null) {
              fileTypeLauncher.addExtension(extension);
            }
          }
        }
      }
    });
  }
  
  protected static boolean hasInitializedLaunchers;
  
  protected static void initializeLaunchers() {
    if(hasInitializedLaunchers) {
      return;
    }
    hasInitializedLaunchers = true;
    NativeInterfaceHandler.invokeSWT(new Runnable() {
      public void run() {
        for(Program program: Program.getPrograms()) {
          if(!programToFileTypeLauncherMap.containsKey(program) && isProgramValid(program) && program.getImageData() != null) {
            programToFileTypeLauncherMap.put(program, new FileTypeLauncher(program));
          }
        }
      }
    });
  }
  
  protected static boolean isProgramValid(Program program) {
    String name = program.getName();
    return name != null && name.length() > 0 /*&& program.getImageData() != null*/;
  }
  
  protected static Map<Program, FileTypeLauncher> programToFileTypeLauncherMap = new HashMap<Program, FileTypeLauncher>();
  
  /**
   * Get the launcher for a given file name, which may or may not represent an existing file. The name can also simply be the extension of a file (including the '.').
   */
  public static FileTypeLauncher getLauncher(String fileName) {
    int index = fileName.lastIndexOf('.');
    if(index == -1) {
      return null;
    }
    final String extension = fileName.substring(index);
    final FileTypeLauncher[] fileTypeLauncherArray = new FileTypeLauncher[1];
    NativeInterfaceHandler.invokeSWT(new Runnable() {
      public void run() {
        Program program = Program.findProgram(extension);
        if(program == null) {
          return;
        }
        FileTypeLauncher fileTypeLauncher = programToFileTypeLauncherMap.get(program);
        if(fileTypeLauncher == null && isProgramValid(program)) {
          fileTypeLauncher = new FileTypeLauncher(program);
          programToFileTypeLauncherMap.put(program, fileTypeLauncher);
        }
        if(fileTypeLauncher != null) {
          if(!hasInitializedExtensions) {
            fileTypeLauncher.addExtension(extension);
          }
          fileTypeLauncherArray[0] = fileTypeLauncher;
        }
      }
    });
    return fileTypeLauncherArray[0];
  }
  
  /**
   * Some global loading of data is performed the first time, if it is needed.
   */
  public static String[] getAllRegisteredExtensions() {
    initializeExtensions();
    List<String> extensionList = new ArrayList<String>();
    for(FileTypeLauncher launcher: programToFileTypeLauncherMap.values()) {
      for(String registeredExtension: launcher.getRegisteredExtensions()) {
        extensionList.add(registeredExtension);
      }
    }
    return extensionList.toArray(new String[0]);
  }
  
  @Override
  public int hashCode() {
    return hashCode;
  }
  
  @Override
  public boolean equals(Object o) {
    return this == o;
  }
  
  public String getName() {
    final String[] result = new String[1];
    NativeInterfaceHandler.invokeSWT(new Runnable() {
      public void run() {
        result[0] = FileTypeLauncher.this.program.getName();
      }
    });
    return result[0];
  }
  
  protected static final ImageIcon DEFAULT_ICON;
  
  static {
    Icon defaultIcon;
    try {
      File tmpFile = File.createTempFile("~djn", "~.qwertyuiop");
      tmpFile.deleteOnExit();
      defaultIcon = FileSystemView.getFileSystemView().getSystemIcon(tmpFile);
      tmpFile.delete();
    } catch(Exception e) {
      defaultIcon = UIManager.getIcon("FileView.fileIcon");
    }
    if(!(defaultIcon instanceof ImageIcon)) {
      int width = defaultIcon.getIconWidth();
      int height = defaultIcon.getIconHeight();
      BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      Graphics gc = image.getGraphics();
//      gc.setColor(Color.WHITE);
//      gc.fillRect(0, 0, width, height);
      defaultIcon.paintIcon(null, gc, 0, 0);
      gc.dispose();
      defaultIcon = new ImageIcon(image);
    }
      DEFAULT_ICON = (ImageIcon)defaultIcon;
  }
  
  public static ImageIcon getDefaultIcon() {
    return DEFAULT_ICON;
  }
  
  protected ImageIcon icon;
  
  public ImageIcon getIcon() {
    if(icon == null) {
      final ImageIcon[] result = new ImageIcon[1];
      NativeInterfaceHandler.invokeSWT(new Runnable() {
        public void run() {
          ImageData imageData = FileTypeLauncher.this.program.getImageData();
          result[0] = imageData == null? DEFAULT_ICON: new ImageIcon(Utils.convertImage(imageData));
        }
      });
      icon = result[0];
    }
    return icon;
  }
  
  /**
   * Some global loading of data is performed the first time, if it is needed.
   */
  public String[] getRegisteredExtensions() {
    initializeExtensions();
    return registeredExtensionList == null? new String[0]: registeredExtensionList.toArray(new String[0]);
  }
  
  public void launch(final String fileName) {
    NativeInterfaceHandler.invokeSWT(new Runnable() {
      public void run() {
        program.execute(fileName);
      }
    });
  }
  
  public static Dimension getIconSize() {
    // TODO: check if a null default icon can happen, and if yes find alternate code
    return DEFAULT_ICON == null? new Dimension(16, 16): new Dimension(DEFAULT_ICON.getIconWidth(), DEFAULT_ICON.getIconHeight());
  }
  
}
