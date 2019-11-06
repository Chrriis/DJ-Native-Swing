/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.netbeans;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openide.modules.ModuleInstall;

import chrriis.dj.nativeswing.common.SystemProperty;
import chrriis.dj.nativeswing.common.Utils;

/**
 * The NetBeans integration object responsible for the activation of the appropriate platform core module.
 * @author Christopher Deckers
 */
public class NativeCoreInstaller extends ModuleInstall {

  @Override
  public void restored() {
    Map<String, String> modelMap = new HashMap<String, String>();
    modelMap.put("Windows.32", "chrriis.dj.nativeswing.swtcore.win32.win32.x86");
    modelMap.put("Windows.64", "chrriis.dj.nativeswing.swtcore.win32.win32.x86_64");
    modelMap.put("Linux.32", "chrriis.dj.nativeswing.swtcore.gtk.linux.x86");
    modelMap.put("Linux.64", "chrriis.dj.nativeswing.swtcore.gtk.linux.x86_64");
    modelMap.put("Mac.32", "chrriis.dj.nativeswing.swtcore.cocoa.macosx.x86");
    modelMap.put("Mac.64", "chrriis.dj.nativeswing.swtcore.cocoa.macosx.x86_64");
    String osArch;
    if (Utils.IS_64_BIT) {
      osArch = "64";
    } else {
      osArch = "32";
    }
    String osName = SystemProperty.OS_NAME.get();
    if (Utils.IS_WINDOWS) {
      osName = "Windows";
    } else if (Utils.IS_MAC) {
      osName = "Mac";
    } else {
      osName = "Linux";
    }
    Map<String, String> osNameMap = new HashMap<String, String>();
    osNameMap.put("Windows", "Windows");
    osNameMap.put("Linux", "Linux");
    osNameMap.put("Mac", "Mac");
    String toEnable = modelMap.get(osNameMap.get(osName) + "." + osArch);
    Set<String> toDisable = new HashSet<String>(modelMap.values());
    if (toEnable != null) {
      toDisable.remove(toEnable);
    }
    ModuleHandler disablerModuleHandler = new ModuleHandler(true);
    disablerModuleHandler.setModulesState(toDisable, false);
    ModuleHandler enablerModuleHandler = new ModuleHandler(true);
    enablerModuleHandler.setModulesState(Collections.singleton(toEnable), true);
  }

}