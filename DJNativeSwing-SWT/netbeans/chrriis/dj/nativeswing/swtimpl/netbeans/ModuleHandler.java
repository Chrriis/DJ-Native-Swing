/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.netbeans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.LifecycleManager;
import org.openide.modules.ModuleInfo;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * A helper class that allows activating/deactivating NetBeans modules programmatically.
 * @author Aljoscha Rittner
 * @author Christopher Deckers
 */
class ModuleHandler {

  private boolean isRestart = false;
  private OperationContainer<OperationSupport> oc;
  private Restarter restarter;
  private final boolean isDirectMode;

  public ModuleHandler() {
    this (false);
  }

  public ModuleHandler(boolean isDirectMode) {
    this.isDirectMode = isDirectMode;
  }

  public List<String> getModules(String startFilter, boolean includeDisabled) {
    List<String> activatedModules = new ArrayList<String>();
    Collection<? extends ModuleInfo> lookupAll = Lookup.getDefault().lookupAll(ModuleInfo.class);
    for (ModuleInfo moduleInfo : lookupAll) {
      if (includeDisabled || moduleInfo.isEnabled()) {
        if (startFilter == null || moduleInfo.getCodeNameBase().startsWith(startFilter)) {
          activatedModules.add(moduleInfo.getCodeNameBase());
        }
      }
    }
    Collections.sort(activatedModules);
    return activatedModules;
  }

  public void doRestart(boolean isForced) {
    if (isForced || isRestart) {
      if (oc != null && restarter != null) {
        try {
          oc.getSupport().doRestart(restarter, null);
        } catch (OperationException ex) {
          Exceptions.printStackTrace(ex);
        }
      } else {
        LifecycleManager.getDefault().markForRestart();
        LifecycleManager.getDefault().exit();
      }
    }
  }

  /**
   * Activate/deactivate a list of modules
   * @param codeNames The names of the modules.
   * @param isEnabled True to enable the modules, false otherwise.
   * @return true if a restart is mandatory.
   */
  public boolean setModulesState(Set<String> codeNames, boolean isEnabled) {
    boolean restartFlag;
    if (isEnabled) {
      restartFlag = setModulesEnabled(codeNames);
    } else {
      restartFlag = setModulesDisabled(codeNames);
    }
    return isRestart = isRestart || restartFlag;
  }

  private boolean setModulesDisabled(Set<String> codeNames) {
    Collection<UpdateElement> toDisable = new HashSet<UpdateElement>();
    List<UpdateUnit> allUpdateUnits =
      UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
    for (UpdateUnit unit : allUpdateUnits) {
      if (unit.getInstalled() != null) {
        UpdateElement el = unit.getInstalled();
        if (el.isEnabled()) {
          if (codeNames.contains(el.getCodeName())) {
            toDisable.add(el);
          }
        }
      }
    }
    if (!toDisable.isEmpty()) {
      oc = isDirectMode ? OperationContainer.createForDirectDisable() : OperationContainer.createForDisable();
      for (UpdateElement module : toDisable) {
        if (oc.canBeAdded(module.getUpdateUnit(), module)) {
          OperationInfo<OperationSupport> operationInfo = oc.add(module);
          if (operationInfo == null) {
            continue;
          }
          // get all module depending on this module
          Set<UpdateElement> requiredElements = operationInfo.getRequiredElements();
          // add all of them between modules for disable
          oc.add(requiredElements);
        }
      }
      try {
        // get operation support for complete the disable operation
        OperationSupport support = oc.getSupport();
        // If support is null, no element can be disabled.
        if ( support != null ) {
          restarter = support.doOperation(null);
        }
      } catch (OperationException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
    return restarter != null;
  }

  private boolean setModulesEnabled(Set<String> codeNames) {
    Collection<UpdateElement> toEnable = new HashSet<UpdateElement>();
    List<UpdateUnit> allUpdateUnits = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
    for (UpdateUnit unit : allUpdateUnits) {
      if (unit.getInstalled() != null) {
        UpdateElement el = unit.getInstalled();
        if (!el.isEnabled()) {
          if (codeNames.contains(el.getCodeName())) {
            toEnable.add(el);
          }
        }
      }
    }
    if (!toEnable.isEmpty()) {
      oc = OperationContainer.createForEnable();
      for (UpdateElement module : toEnable) {
        if (oc.canBeAdded(module.getUpdateUnit(), module)) {
          OperationInfo<OperationSupport> operationInfo = oc.add(module);
          if (operationInfo == null) {
            continue;
          }
          // get all module depending on this module
          Set<UpdateElement> requiredElements = operationInfo.getRequiredElements();
          // add all of them between modules for disable
          oc.add(requiredElements);
        }
      }
      try {
        // get operation support for complete the enable operation
        OperationSupport support = oc.getSupport();
        if (support != null) {
          restarter = support.doOperation(null);
        }
        return true;
      } catch (OperationException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
    return false;
  }

}
