package utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        Map<String, String> modelMap = new HashMap<String, String>();
        modelMap.put("Windows.32", "chrriis.dj.nativeswing.swtcore.win32.win32.x86");
        modelMap.put("Windows.64", "chrriis.dj.nativeswing.swtcore.win32.win32.x86_64");
        modelMap.put("Linux.32", "chrriis.dj.nativeswing.swtcore.gtk.linux.x86");
        modelMap.put("Linux.64", "chrriis.dj.nativeswing.swtcore.gtk.linux.x86_64");
        modelMap.put("Mac.32", "chrriis.dj.nativeswing.swtcore.cocoa.macosx.x86");
        modelMap.put("Mac.64", "chrriis.dj.nativeswing.swtcore.cocoa.macosx.x86_64");
        String osArch = System.getProperty("os.arch");
        if ("amd64".equals(osArch)) {
            osArch = "64";
        } else {
            osArch = "32";
        }
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            osName = "Windows";
        } else if (osName.startsWith("Mac")) {
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
        ModuleHandler disabler = new ModuleHandler(true);
        disabler.setModulesState(false, toDisable);
        ModuleHandler enabler = new ModuleHandler(true);
        enabler.setModulesState(true, Collections.singleton(toEnable));
    }

}