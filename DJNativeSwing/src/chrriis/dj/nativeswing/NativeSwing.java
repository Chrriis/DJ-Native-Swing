/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.SwingUtilities;

import chrriis.common.SystemProperty;
import chrriis.common.Utils;

/**
 * The Native Swing class responsible for the initialization of the framework.
 * @author Christopher Deckers
 */
public class NativeSwing {

  private static class HeavyweightForcerWindow extends Window {

    private boolean isPacked;

    public HeavyweightForcerWindow(Window parent) {
      super(parent);
      pack();
      isPacked = true;
    }

    @Override
    public boolean isVisible() {
      return isPacked;
    }

    @Override
    public Rectangle getBounds() {
      Window owner = getOwner();
      return owner == null? super.getBounds(): owner.getBounds();
    }

    private int count;

    public void setCount(int count) {
      this.count = count;
    }

    public int getCount() {
      return count;
    }

  }

  private static class HeavyweightForcer implements HierarchyListener {

    private Component component;
    private HeavyweightForcerWindow forcer;

    private HeavyweightForcer(Component component) {
      this.component = component;
      if(component.isShowing()) {
        createForcer();
      }
    }

    public static void activate(Component component) {
      component.addHierarchyListener(new HeavyweightForcer(component));
    }

    private void destroyForcer() {
      if(!SwingUtilities.isEventDispatchThread()) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            destroyForcer();
          }
        });
        return;
      }
      if(forcer == null) {
        return;
      }
      int count = forcer.getCount() - 1;
      forcer.setCount(count);
      if(count == 0) {
        forcer.dispose();
      }
      forcer = null;
    }

    private void createForcer() {
      if(!SwingUtilities.isEventDispatchThread()) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            createForcer();
          }
        });
        return;
      }
      Window windowAncestor = SwingUtilities.getWindowAncestor(component);
      if(windowAncestor == null) {
        return;
      }
      for(Window window: windowAncestor.getOwnedWindows()) {
        if(window instanceof HeavyweightForcerWindow) {
          forcer = (HeavyweightForcerWindow)window;
          break;
        }
      }
      if(forcer == null) {
        forcer = new HeavyweightForcerWindow(windowAncestor);
      }
      forcer.setCount(forcer.getCount() + 1);
    }

    public void hierarchyChanged(HierarchyEvent e) {
      long changeFlags = e.getChangeFlags();
      if((changeFlags & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
        if(!component.isDisplayable()) {
          component.removeHierarchyListener(this);
          destroyForcer();
        }
      } else if((changeFlags & HierarchyEvent.SHOWING_CHANGED) != 0) {
        if(component.isShowing()) {
          createForcer();
        } else {
          destroyForcer();
        }
      }
    }

  }

  private static volatile List<NativeComponentWrapper> nativeComponentWrapperList;

  static NativeComponentWrapper[] getNativeComponentWrappers() {
    if(nativeComponentWrapperList == null) {
      return new NativeComponentWrapper[0];
    }
    return nativeComponentWrapperList.toArray(new NativeComponentWrapper[0]);
  }

  static void addNativeComponentWrapper(NativeComponentWrapper nativeComponentWrapper) {
    checkInitialized();
    if(nativeComponentWrapperList == null) {
      nativeComponentWrapperList = new ArrayList<NativeComponentWrapper>();
    }
    nativeComponentWrapperList.add(nativeComponentWrapper);
    if(!isHeavyWeightForcerEnabled) {
      HeavyweightForcer.activate(nativeComponentWrapper.getNativeComponent());
    }
  }

  static boolean removeNativeComponentWrapper(NativeComponentWrapper nativeComponentWrapper) {
    if(nativeComponentWrapperList == null) {
      return false;
    }
    return nativeComponentWrapperList.remove(nativeComponentWrapper);
  }

  private static List<Window> windowList;

  static Window[] getWindows() {
    return windowList == null? new Window[0]: windowList.toArray(new Window[0]);
  }

  private static volatile boolean isInitialized;

  private static boolean isInitialized() {
    return isInitialized;
  }

  private static void checkInitialized() {
    if(!isInitialized()) {
      throw new IllegalStateException("The Native Swing framework is not initialized! Please refer to the instructions to set it up properly.");
    }
  }

  private static void loadClipboardDebuggingProperties() {
    try {
      Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      if(!systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
        return;
      }
      BufferedReader reader = new BufferedReader(new StringReader((String)systemClipboard.getData(DataFlavor.stringFlavor)));
      if("[nativeswing debug]".equals(reader.readLine().trim().toLowerCase(Locale.ENGLISH))) {
        for(String line; ((line = reader.readLine()) != null); ) {
          if(line.length() != 0) {
            int index = line.indexOf('=');
            if(index <= 0) {
              break;
            }
            String propertyName = line.substring(0, index).trim();
            String propertyValue = line.substring(index + 1).trim();
            if(propertyName.startsWith("nativeswing.")) {
              System.setProperty(propertyName, propertyValue);
            }
          }
        }
      }
      reader.close();
    } catch(Exception e) {
    }
  }

  private static class NIAWTEventListener implements AWTEventListener {

    private List<Dialog> dialogList = new ArrayList<Dialog>();
    private volatile Set<Window> blockedWindowSet = new HashSet<Window>();

    private static boolean isDescendant(Window window, Window ancestorWindow) {
      for(; window != null; window=window.getOwner()) {
        if(window == ancestorWindow) {
          return true;
        }
      }
      return false;
    }

    private void computeBlockedDialogs() {
      blockedWindowSet.clear();
      Window[] windows = getWindows();
      List<Dialog> applicationModalDialogList = new ArrayList<Dialog>();
      for(Dialog dialog: dialogList) {
        if(dialog.isVisible()) {
          boolean isApplicationModal = false;
          if(Utils.IS_JAVA_6_OR_GREATER) {
            switch(dialog.getModalityType()) {
              case APPLICATION_MODAL:
              case TOOLKIT_MODAL:
                isApplicationModal = true;
                break;
            }
          } else if(dialog.isModal()) {
            isApplicationModal = true;
          }
          if(isApplicationModal) {
            applicationModalDialogList.add(dialog);
          }
        }
      }
      if(!applicationModalDialogList.isEmpty()) {
        for(int i=0; i<windows.length; i++) {
          Window window = windows[i];
          boolean isIncluded = false;
          for(Dialog applicationModalDialog: applicationModalDialogList) {
            if(window != applicationModalDialog && !isDescendant(window, applicationModalDialog)) {
              isIncluded = true;
              // if the blocking dialog was opened before that one, it does not block.
              for(int j=0; j<i; j++) {
                if(windows[j] == applicationModalDialog) {
                  isIncluded = false;
                  break;
                }
              }
              if(isIncluded && Utils.IS_JAVA_6_OR_GREATER) {
                switch(window.getModalExclusionType()) {
                  case APPLICATION_EXCLUDE:
                  case TOOLKIT_EXCLUDE:
                    isIncluded = false;
                    break;
                }
              }
            }
            if(isIncluded) {
              break;
            }
          }
          if(isIncluded) {
            blockedWindowSet.add(window);
          }
        }
      }
      if(Utils.IS_JAVA_6_OR_GREATER) {
        // Consider that last in the list block previous ones.
        for(int i=dialogList.size()-1; i>=0; i--) {
          Dialog dialog = dialogList.get(i);
          if(dialog.isVisible() && !blockedWindowSet.contains(dialog)) {
            switch(dialog.getModalityType()) {
              case DOCUMENT_MODAL:
                // Block all windows that have same owner but are not a descendant of this dialog.
                Window hierarchyOwnerWindow = dialog.getOwner();
                for(Window owner = hierarchyOwnerWindow; owner != null; owner = owner.getOwner()) {
                  hierarchyOwnerWindow = owner;
                }
                if(hierarchyOwnerWindow != null) {
                  for(Window window: windows) {
                    if(window != dialog && !isDescendant(window, dialog) && (window == hierarchyOwnerWindow || isDescendant(window, hierarchyOwnerWindow))) {
                      blockedWindowSet.add(window);
                    }
                  }
                }
                break;
            }
          }
        }
      }
    }

    private void adjustNativeComponents() {
      if(nativeComponentWrapperList == null) {
        return;
      }
      for(int i=nativeComponentWrapperList.size()-1; i>=0; i--) {
        final NativeComponentWrapper nativeComponentWrapper = nativeComponentWrapperList.get(i);
        Component component = nativeComponentWrapper.getNativeComponent();
        Component c = component;
        Component componentProxy = nativeComponentWrapper.getNativeComponentProxy();
        if(componentProxy != null) {
          c = componentProxy;
        }
        Window embedderWindowAncestor = SwingUtilities.getWindowAncestor(c);
        boolean isBlocked = blockedWindowSet.contains(embedderWindowAncestor);
        final boolean isShowing = c.isShowing();
        nativeComponentWrapper.setNativeComponentEnabled(!isBlocked && isShowing);
        if(!Utils.IS_MAC) {
          // This causes serious freezes with Mac.
          if(!isShowing && component.hasFocus()) {
            component.transferFocus();
          }
        }
      }
    }

    public void eventDispatched(AWTEvent e) {
      int eventID = e.getID();
      if(Utils.IS_JAVA_7_OR_GREATER) {
        switch(eventID) {
          case MouseEvent.MOUSE_PRESSED:
          case FocusEvent.FOCUS_GAINED:
            if(nativeComponentWrapperList == null) {
              return;
            }
            // In Java 7, components cannot gain focus if a native component is shown.
            // The fix is to temporary disable the native component hierarchy.
            for(int i=nativeComponentWrapperList.size()-1; i>=0; i--) {
              final NativeComponentWrapper nativeComponentWrapper = nativeComponentWrapperList.get(i);
              if(nativeComponentWrapper.isNativeComponentEnabled()) {
                nativeComponentWrapper.setNativeComponentEnabled(false);
                nativeComponentWrapper.setNativeComponentEnabled(true);
              }
            }
            return;
        }
      }
      boolean isAdjusting = false;
      switch(eventID) {
        case ComponentEvent.COMPONENT_SHOWN:
        case ComponentEvent.COMPONENT_HIDDEN:
          isAdjusting = true;
          break;
      }
      if(e.getSource() instanceof Window) {
        if(windowList == null) {
          windowList = new ArrayList<Window>();
        }
        switch(eventID) {
          case WindowEvent.WINDOW_OPENED:
          case ComponentEvent.COMPONENT_SHOWN:
            Window w = (Window)e.getSource();
            windowList.remove(w);
            windowList.add(w);
            break;
          case WindowEvent.WINDOW_CLOSED:
          case ComponentEvent.COMPONENT_HIDDEN:
            windowList.remove(e.getSource());
            break;
        }
      }
      if(e.getSource() instanceof Dialog) {
        switch(eventID) {
          case WindowEvent.WINDOW_OPENED:
          case ComponentEvent.COMPONENT_SHOWN:
            Dialog d = (Dialog)e.getSource();
            dialogList.remove(d);
            dialogList.add(d);
            break;
          case WindowEvent.WINDOW_CLOSED:
          case ComponentEvent.COMPONENT_HIDDEN:
            dialogList.remove(e.getSource());
            break;
        }
        switch(eventID) {
          case WindowEvent.WINDOW_OPENED:
          case WindowEvent.WINDOW_CLOSED:
          case ComponentEvent.COMPONENT_SHOWN:
          case ComponentEvent.COMPONENT_HIDDEN:
            computeBlockedDialogs();
            isAdjusting = true;
            break;
        }
      }
      if(isAdjusting) {
        adjustNativeComponents();
      }
      // Dialogs mess the focus: when the default focusable component is not the native component,
      // clicking the native component has the effect of losing the focus of the dialog, then
      // regaining it but on the default component. Hence it is very hard to actually give focus to
      // the native component.
      // The fix is to prevent the window from being focusable for a short amount of time when it
      // loses focus. The effect is that the native component can gain focus and retargetting does
      // not happen.
      switch(eventID) {
        case WindowEvent.WINDOW_LOST_FOCUS:
          if(e.getSource() instanceof Dialog) {
            final Dialog d = (Dialog)e.getSource();
            if(d.getFocusableWindowState()) {
              d.setFocusableWindowState(false);
              Thread t = new Thread("Dialog focus fixer") {
                @Override
                public void run() {
                  try {
                    sleep(125);
                  } catch (InterruptedException e) {
                  }
                  SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                      d.setFocusableWindowState(true);
                    }
                  });
                }
              };
              t.setDaemon(true);
              t.start();
            }
            break;
        }
      }
    }

  }

  private static volatile boolean isHeavyWeightForcerEnabled;

  /**
   * Initialize the Native Swing framework. This method sets some properties and registers a few listeners to keep track of certain states.<br/>
   * It should be called early in the program, the best place being as the first call in the main method.
   */
  public static void initialize() {
    if(isInitialized()) {
      return;
    }
    loadClipboardDebuggingProperties();
    // Specific Sun property to prevent heavyweight components from erasing their background.
    SystemProperty.SUN_AWT_NOERASEBACKGROUND.set("true");
    // It seems on Linux this is required to get the component visible.
    SystemProperty.SUN_AWT_XEMBEDSERVER.set("true");
    // We use our own HW forcing, so we disable the one from JNA
    NSSystemProperty.JNA_FORCE_HW_POPUP.set("false");
    // We have to disable mixing until all bahaviors can be implemented using the JDK features.
    if(SystemProperty.JAVAWEBSTART_VERSION.get() != null && SystemProperty.JAVA_VERSION.get().compareTo("1.6.0_18") >= 0) {
      if(SystemProperty.SUN_AWT_DISABLEMIXING.get() == null) {
        System.err.println("Under WebStart on Java >= 1.6.0_18, the value of the \"" + SystemProperty.SUN_AWT_DISABLEMIXING.getName() + "\" system property needs to be defined in the JNLP descriptor with value \"true\" (or \"false\" if you really want the default behavior). When not set to \"true\", the content of the native components may not be displayed.");
        SystemProperty.SUN_AWT_DISABLEMIXING.set("false");
      }
    } else {
      if(SystemProperty.SUN_AWT_DISABLEMIXING.get() == null) {
        SystemProperty.SUN_AWT_DISABLEMIXING.set("true");
      }
    }
    boolean isSunMixingEnabled = !"true".equals(SystemProperty.SUN_AWT_DISABLEMIXING.get()) && SystemProperty.JAVA_VERSION.get().compareTo("1.6.0_12") >= 0;
    isHeavyWeightForcerEnabled = isSunMixingEnabled;
    NSSystemProperty.INTEGRATION_USEDEFAULTCLIPPING.set(String.valueOf(isSunMixingEnabled));
    // Create window monitor
    long flags = WindowEvent.WINDOW_EVENT_MASK | ComponentEvent.COMPONENT_EVENT_MASK;
    if(Utils.IS_JAVA_7_OR_GREATER) {
      flags |= ComponentEvent.FOCUS_EVENT_MASK | ComponentEvent.MOUSE_EVENT_MASK;
    }
    Toolkit.getDefaultToolkit().addAWTEventListener(new NIAWTEventListener(), flags);
    isInitialized = true;
  }

  private NativeSwing() {}

}
