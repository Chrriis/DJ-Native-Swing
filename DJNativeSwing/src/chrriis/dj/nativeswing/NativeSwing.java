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
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.SwingUtilities;

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
    if(Utils.IS_JAVA_6_OR_GREATER) {
      List<Window> windowList = new ArrayList<Window>();
      for(Window window: Window.getWindows()) {
        if(!(window instanceof HeavyweightForcerWindow)) {
          windowList.add(window);
        }
      }
      return windowList.toArray(new Window[0]);
    }
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
      Dialog applicationModalDialog = null;
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
            if(applicationModalDialog != null) {
              if(isDescendant(dialog, applicationModalDialog)) {
                applicationModalDialog = dialog;
              }
            } else {
              applicationModalDialog = dialog;
            }
          }
        }
      }
      if(applicationModalDialog != null) {
        for(Window window: windows) {
          if(window != applicationModalDialog && !isDescendant(window, applicationModalDialog)) {
            boolean isIncluded = true;
            if(Utils.IS_JAVA_6_OR_GREATER) {
              switch(window.getModalExclusionType()) {
                case APPLICATION_EXCLUDE:
                case TOOLKIT_EXCLUDE:
                  isIncluded = false;
                  break;
              }
            }
            if(isIncluded) {
              blockedWindowSet.add(window);
            }
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
      boolean isAdjusting = false;
      switch(e.getID()) {
        case ComponentEvent.COMPONENT_SHOWN:
        case ComponentEvent.COMPONENT_HIDDEN:
          isAdjusting = true;
          break;
      }
      if(!Utils.IS_JAVA_6_OR_GREATER && e.getSource() instanceof Window) {
        if(windowList == null) {
          windowList = new ArrayList<Window>();
        }
        switch(e.getID()) {
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
        switch(e.getID()) {
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
        switch(e.getID()) {
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
    System.setProperty("sun.awt.noerasebackground", "true");
    // It seems on Linux this is required to get the component visible.
    System.setProperty("sun.awt.xembedserver", "true");
    // We use our own HW forcing, so we disable the one from JNA
    System.setProperty("jna.force_hw_popups", "false");
    // We have to disable mixing until all bahaviors can be implemented using the JDK features.
    if(System.getProperty("sun.awt.disableMixing") == null) {
      System.setProperty("sun.awt.disableMixing", "true");
    }
    boolean isSunMixingEnabled = !"true".equals(System.getProperty("sun.awt.disableMixing")) && System.getProperty("java.version").compareTo("1.6.0_12") >= 0;
    isHeavyWeightForcerEnabled = isSunMixingEnabled;
    // Mac does not support shaping: we are not going to activate our algorithm.
    System.setProperty("nativeswing.integration.useDefaultClipping", String.valueOf(Utils.IS_MAC || isSunMixingEnabled));
    // Create window monitor
    Toolkit.getDefaultToolkit().addAWTEventListener(new NIAWTEventListener(), WindowEvent.WINDOW_EVENT_MASK | ComponentEvent.COMPONENT_EVENT_MASK);
    isInitialized = true;
  }

  private NativeSwing() {}

}
