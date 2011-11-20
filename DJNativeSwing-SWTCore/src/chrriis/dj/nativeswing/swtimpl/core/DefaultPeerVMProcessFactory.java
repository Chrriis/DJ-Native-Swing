/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import chrriis.common.SystemProperty;
import chrriis.common.Utils;
import chrriis.dj.nativeswing.swtimpl.NSSystemPropertySWT;
import chrriis.dj.nativeswing.swtimpl.PeerVMProcessFactory;

/**
 * @author Christopher Deckers
 */
public class DefaultPeerVMProcessFactory implements PeerVMProcessFactory {

  public Process createProcess(String[] classpathItems, Map<String, String> systemPropertiesMap, String[] vmParams, String mainClass, String[] mainClassParameters) {
    String pathSeparator = SystemProperty.PATH_SEPARATOR.get();
    String[] candidateBinaries = new String[] {
        new File(SystemProperty.JAVA_HOME.get(), "bin/java").getAbsolutePath(),
        new File("/usr/lib/java").getAbsolutePath(),
        "java",
    };
    boolean isTryingAppletCompatibility = true;
    for(String peerVMParam: vmParams) {
      if(peerVMParam.startsWith("-Xbootclasspath/a:")) {
        isTryingAppletCompatibility = false;
        break;
      }
    }
    String javaVersion = SystemProperty.JAVA_VERSION.get();
    String vmParamsWithAppletCompatibility = null;
    // Try compatibility with Java applets on update 10.
    if(isTryingAppletCompatibility && javaVersion != null && javaVersion.compareTo("1.6.0_10") >= 0 && "Sun Microsystems Inc.".equals(SystemProperty.JAVA_VENDOR.get())) {
      String javaHome = SystemProperty.JAVA_HOME.get();
      File[] deploymentFiles = new File[] {
          new File(javaHome, "lib/deploy.jar"),
          new File(javaHome, "lib/plugin.jar"),
          new File(javaHome, "lib/javaws.jar"),
      };
      StringBuilder sbX = new StringBuilder();
      for(int i=0; i<deploymentFiles.length; i++) {
        if(i != 0) {
          sbX.append(pathSeparator);
        }
        File deploymentFile = deploymentFiles[i];
        if(deploymentFile.exists()) {
          sbX.append(deploymentFile.getAbsolutePath());
        }
      }
      if(sbX.indexOf(" ") != -1) {
        // TODO: check what to do when there are spaces in paths on non-windows machines
        vmParamsWithAppletCompatibility = "\"-Xbootclasspath/a:" + sbX + "\"";
      } else {
        vmParamsWithAppletCompatibility = "-Xbootclasspath/a:" + sbX;
      }
    } else {
      isTryingAppletCompatibility = false;
    }
    for(int mode=isTryingAppletCompatibility? 1: 0; mode>=0; mode--) {
      List<String> argList = new ArrayList<String>();
      for(String candidateBinary: candidateBinaries) {
        // Java binary
        argList.add(candidateBinary);
        if(mode == 1) {
          // Special boot class path when we try applet mode.
          argList.add(vmParamsWithAppletCompatibility);
        }
        // VM parameters
        for(String vmParam: vmParams) {
          argList.add(vmParam);
        }
        // System properties
        for(Map.Entry<String, String> propertyEntry: systemPropertiesMap.entrySet()) {
          String value = propertyEntry.getValue();
          if(Utils.IS_WINDOWS) {
      	    // On Windows, double quotes cut the property so we have to add a \ before.
            // But! if there is a sequence like \\", then the property is cut too...
            // Because such sequence happens in end of paths, too bad, we drop the last ending \
            // This is not a perfect escaping, but there does not seem to be one. Try to show with quotes: "c:\temp", "c:\temp\", and "c:\temp\\"...
            value = value.replace("\\\"", "\"").replace("\"", "\\\"");
          }
          argList.add("-D" + propertyEntry.getKey() + "=" + value);
        }
        // Class path.
        argList.add("-classpath");
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<classpathItems.length; i++) {
          if(i > 0) {
            sb.append(pathSeparator);
          }
          sb.append(classpathItems[i]);
        }
        argList.add(sb.toString());
        // Main class
        argList.add(mainClass);
        // Application parameters
        for(String mainClassParameter: mainClassParameters) {
          argList.add(mainClassParameter);
        }
        if(Boolean.parseBoolean(NSSystemPropertySWT.PEERVM_DEBUG_PRINTCOMMANDLINE.get())) {
          System.err.println("Native Command: " + Arrays.toString(argList.toArray()));
        }
        try {
          return new ProcessBuilder(argList).start();
        } catch(IOException e) {
          throw new IllegalStateException(e);
        }
      }
    }
    return null;
  }

}
