package utils;

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
* Der ModuleHandler ist eine Hilfsklasse zum programatischen (de)aktivieren
* von Modulen und der Analyse von installierten aktiven Modulen.
* @author rittner
*/
public class ModuleHandler {

  private boolean restart = false;
  private OperationContainer<OperationSupport> oc;
  private Restarter restarter;
  private final boolean directMode;


  public ModuleHandler() {
    this (false);
  }
  public ModuleHandler(boolean directMode) {
    this.directMode = directMode;
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


  /**
   * F�hrt einen Neustart der Anwendung durch, wenn der vorherige setModulesState
   * ein Flag daf�r gesetzt hat. mit force, kann der Restart erzwungen werden.
   * <p>
   * Man sollte nicht davon ausgehen, dass nach dem Aufruf der Methode
   * zur�ckgekehrt wird.
   * @param force
   */
  public void doRestart(boolean force) {
    if (force || restart) {
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
   * Aktiviert oder deaktivert die Liste der Module
   * @param enable
   * @param codeNames
   * @return true, wenn ein Neustart zwingend erforderlich ist
   */
  public boolean setModulesState (boolean enable, Set<String> codeNames) {
    boolean restartFlag;
    if (enable) {
      restartFlag = setModulesEnabled(codeNames);
    } else {
      restartFlag = setModulesDisabled(codeNames);
    }
    return restart = restart || restartFlag;
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
      oc = directMode ? OperationContainer.createForDirectDisable() : OperationContainer.createForDisable();
      for (UpdateElement module : toDisable) {
        if (oc.canBeAdded(module.getUpdateUnit(), module)) {
          OperationInfo operationInfo = oc.add(module);
          if (operationInfo == null) {
            continue;
          }
          // get all module depending on this module
          @SuppressWarnings("unchecked")
          Set<UpdateElement> requiredElements = (Set<UpdateElement>)operationInfo.getRequiredElements();
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
    List<UpdateUnit> allUpdateUnits =
            UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
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
          OperationInfo operationInfo = oc.add(module);
          if (operationInfo == null) {
            continue;
          }
          // get all module depending on this module
          @SuppressWarnings("unchecked")
          Set<UpdateElement> requiredElements = (Set<UpdateElement>)operationInfo.getRequiredElements();
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
