/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.awt.AWTEvent;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.ui.NativeComponent;

/**
 * @author Christopher Deckers
 */
public class NativeInterfaceHandler {

  protected static volatile Thread displayThread;
  protected static volatile Display display;
  protected static volatile boolean isRunning;

  protected static Set<Window> windowSet;
  
  public static Window[] getWindows() {
    if(Utils.IS_JAVA_6_OR_GREATER) {
      return Window.getWindows();
    }
    return windowSet == null? new Window[0]: windowSet.toArray(new Window[0]);
  }
  
  public static void init() {
    if(isRunning) {
      return;
    }
    try {
      String systemLookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
      if(!"com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(systemLookAndFeelClassName)) {
        UIManager.setLookAndFeel(systemLookAndFeelClassName);
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
    // We set up a new security manager to track exit calls.
    // When this happens, we dispose native resources to avoid freezes.
    try {
      System.setSecurityManager(new SecurityManager() {
        protected SecurityManager securityManager = System.getSecurityManager();
        @Override
        public void checkExit(int status) {
          super.checkExit(status);
          for(StackTraceElement stackTraceElement: Thread.currentThread().getStackTrace()) {
            String className = stackTraceElement.getClassName();
            String methodName = stackTraceElement.getMethodName();
            if("java.lang.Runtime".equals(className) && ("exit".equals(methodName) || "halt".equals(methodName)) || "java.lang.System".equals(className) && "exit".equals(methodName)) {
              display.syncExec(new Runnable() {
                public void run() {
                  cleanUp();
                }
              });
              break;
            }
          }
        }
        @Override
        public void checkPermission(Permission perm) {
          if(securityManager != null) {
            securityManager.checkPermission(perm);
          }
        }
      });
    } catch(Exception e) {
      e.printStackTrace();
    }
    displayThread = Thread.currentThread();
    display = new Display();
    isRunning = true;
    // Specific Sun property to prevent heavyweight components from erasing their background.
    System.setProperty("sun.awt.noerasebackground", "true");
    // It seems on Linux this is required to get the component visible.
    System.setProperty("sun.awt.xembedserver", "true");
    // Remove all lightweight windows, to avoid wrong overlaps
    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    System.setProperty("JPopupMenu.defaultLWPopupEnabledKey", "false");
    // Disable components when they go out of visibility to avoid focus grabbing
    Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
      protected Set<Dialog> dialogSet = new HashSet<Dialog>();
      protected volatile Set<Window> blockedWindowSet = new HashSet<Window>();
//      protected volatile boolean isInvoking;
      protected void adjustNativeComponents() {
//        if(isInvoking) {
//          return;
//        }
//        isInvoking = true;
//        SwingUtilities.invokeLater(new Runnable() {
//          public void run() {
//            isInvoking = false;
//            adjustNativeComponents_();
//          }
//        });
//      }
//      protected void adjustNativeComponents_() {
        invokeSWT(new Runnable() {
          public void run() {
            for(int i=canvasList.size()-1; i>=0; i--) {
              Shell shell = shellList.get(i);
              final Canvas canvas = canvasList.get(i);
              Component c = canvas;
              if(canvas instanceof NativeComponent) {
                Component componentProxy = ((NativeComponent)canvas).getComponentProxy();
                if(componentProxy != null) {
                  c = componentProxy;
                }
              }
              Window embedderWindowAncestor = SwingUtilities.getWindowAncestor(c);
              boolean isBlocked = blockedWindowSet.contains(embedderWindowAncestor);
              final boolean isShowing = c.isShowing();
              if(!shell.isDisposed()) {
                shell.setEnabled(!isBlocked && isShowing);
                SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                    boolean hasFocus = canvas.hasFocus();
                    if(!isShowing && hasFocus) {
                      canvas.transferFocus();
                    }
                  }
                });
              }
            }
          }
        });
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
          if(windowSet == null) {
            windowSet = new HashSet<Window>();
          }
          switch(e.getID()) {
            case WindowEvent.WINDOW_OPENED:
            case ComponentEvent.COMPONENT_SHOWN:
              windowSet.add((Window)e.getSource());
              break;
            case WindowEvent.WINDOW_CLOSED:
            case ComponentEvent.COMPONENT_HIDDEN:
              windowSet.remove(e.getSource());
              break;
          }
        }
        if(e.getSource() instanceof Dialog) {
          switch(e.getID()) {
            case WindowEvent.WINDOW_OPENED:
            case ComponentEvent.COMPONENT_SHOWN:
              dialogSet.add((Dialog)e.getSource());
              break;
            case WindowEvent.WINDOW_CLOSED:
            case ComponentEvent.COMPONENT_HIDDEN:
              dialogSet.remove(e.getSource());
              break;
          }
          switch(e.getID()) {
            case WindowEvent.WINDOW_OPENED:
            case WindowEvent.WINDOW_CLOSED:
            case ComponentEvent.COMPONENT_SHOWN:
            case ComponentEvent.COMPONENT_HIDDEN:
              blockedWindowSet.clear();
              for(Dialog dialog: dialogSet) {
                // TODO: consider modal excluded and other modality types than simple parent blocking.
                if(dialog.isVisible() && dialog.isModal()) {
                  blockedWindowSet.add(dialog.getOwner());
                }
              }
              isAdjusting = true;
              break;
          }
        }
        if(isAdjusting) {
          adjustNativeComponents();
        }
      }
    }, WindowEvent.WINDOW_EVENT_MASK | ComponentEvent.COMPONENT_EVENT_MASK);
    Runtime.getRuntime().addShutdownHook(new Thread("DJNativeSwing Shutdown Hook") {
      @Override
      public void run() {
        display.asyncExec(new Runnable() {
          public void run() {
            cleanUp();
          }
        });
      }
    });
    Thread autoShutdownThread = new Thread("DJNativeSwing Auto-Shutdown") {
      protected Thread[] activeThreads = new Thread[1024];
      @Override
      public void run() {
        while(true) {
          try {
            Thread.sleep(500);
          } catch(Exception e) {
          }
          ThreadGroup group = Thread.currentThread().getThreadGroup();
          for(ThreadGroup parentGroup = group; (parentGroup = parentGroup.getParent()) != null; group = parentGroup);
          boolean isAlive = false;
          for(int i=group.enumerate(activeThreads, true)-1; i>=0; i--) {
            Thread t = activeThreads[i];
            if(t != displayThread && !t.isDaemon() && t.isAlive()) {
              isAlive = true;
              break;
            }
          }
          if(!isAlive) {
            isRunning = false;
            display.wake();
          }
        }
      }
    };
    autoShutdownThread.setDaemon(true);
    autoShutdownThread.start();
  }
  
  protected static void cleanUp() {
    for(Shell shell: shellList) {
      try {
        shell.dispose();
      } catch(Exception e) {
        // Swallow exceptions. These are de-activation of controls when the display is already disposed.
      }
    }
    shellList = new ArrayList<Shell>();
    canvasList = new ArrayList<Canvas>();
    // Note: display is not disposed because it makes the JVM to crash. Anyway, its resources are released on system exit.
//    display.dispose();
//    display = null;
  }
  
  public static void runEventPump() {
    Thread wakeUpThread = new Thread("DJNativeSwing SWT wake-up") {
      @Override
      public void run() {
        while(display != null) {
          try {
            sleep(50);
            if(display != null && !display.isDisposed()) {
              // for some reasons, need to wake up everytime it sleeps using a fake event
              display.asyncExec(new Runnable() {
                public void run() {
                }
              });
            }
          } catch(Exception e) {
            e.printStackTrace();
          }
        }
      }
    };
    wakeUpThread.setDaemon(true);
    wakeUpThread.start();
    while(isRunning) {
      dispatch();
    }
    cleanUp();
  }
  
  protected static void dispatch() {
    try {
      if(display.readAndDispatch()) {
        if(isRunning) {
          display.sleep();
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public static Display getDisplay() {
    checkPump();
    return display;
  }
  
  protected static final Object SWING_LOCK = new Object();
  protected static final Object SWT_LOCK = new Object();
  
  protected static List<Runnable> swingRunnableList = new ArrayList<Runnable>();
  protected static List<Runnable> swtRunnableList = new ArrayList<Runnable>();
  protected static volatile boolean hasSwingRun;
  protected static volatile boolean hasSWTRun;
  
  /**
   * Invoke some code in the Swing UI thread synchronously.
   */
  @SuppressWarnings("unchecked")
  public static void invokeSwing(final Runnable runnable) {
    checkPump();
    if(displayThread == Thread.currentThread()) {
      synchronized(SWING_LOCK) {
        swingRunnableList.add(runnable);
        if(swingRunnableList.size() == 1) {
          hasSwingRun = false;
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              while(true) {
                Runnable r;
                synchronized(SWING_LOCK) {
                  if(swingRunnableList.isEmpty()) {
                    hasSwingRun = true;
                    break;
                  }
                  r = swingRunnableList.remove(0);
                }
                r.run();
              }
            }
          });
        }
      }
      while(!hasSwingRun) {
        Runnable r = null;
        synchronized(SWT_LOCK) {
          if(swtRunnableList.isEmpty()) {
            hasSWTRun = true;
          } else {
            r = swtRunnableList.remove(0);
          }
        }
        if(r != null) {
          r.run();
        }
//        try {
//          Thread.sleep(50);
//        } catch(Exception e) {
//        }
        dispatch();
      }
    } else if(SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      try {
        SwingUtilities.invokeAndWait(runnable);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  public static void invokeSWT(final Runnable runnable) {
    checkPump();
    if(SwingUtilities.isEventDispatchThread()) {
      synchronized(SWT_LOCK) {
        swtRunnableList.add(runnable);
        if(swtRunnableList.size() == 1) {
          hasSWTRun = false;
          display.asyncExec(new Runnable() {
            public void run() {
              while(true) {
                Runnable r;
                synchronized(SWT_LOCK) {
                  if(swtRunnableList.isEmpty()) {
                    hasSWTRun = true;
                    break;
                  }
                  r = swtRunnableList.remove(0);
                }
                r.run();
              }
            }
          });
        }
      }
      while(!hasSWTRun) {
        Runnable r = null;
        synchronized(SWING_LOCK) {
          if(swingRunnableList.isEmpty()) {
            hasSwingRun = true;
          } else {
            r = swingRunnableList.remove(0);
          }
        }
        if(r != null) {
          r.run();
        }
      }
    } else if(displayThread == Thread.currentThread()) {
      runnable.run();
    } else {
      display.syncExec(runnable);
    }
  }
  
  protected static volatile List<Shell> shellList = new ArrayList<Shell>();
  protected static volatile List<Canvas> canvasList = new ArrayList<Canvas>();
  
  public static Shell createShell(Canvas canvas) {
    checkPump();
    Shell shell = SWT_AWT.new_Shell(getDisplay(), canvas);
    canvasList.add(canvas);
    shellList.add(shell);
    return shell;
  }
  
  public static void disposeShell(Shell shell) {
    int index = shellList.indexOf(shell);
    if(index >= 0) {
      canvasList.remove(index);
      shellList.remove(index);
      shell.dispose();
    }
  }
  
  public static Canvas[] getCanvas() {
    return canvasList.toArray(new Canvas[0]);
  }
  
  public static void main(String[] args) {
    init();
    String mainClass = System.getProperty("dj.nativeswing.mainclass");
    if(mainClass == null) {
      mainClass = args[0];
      String[] newArgs = new String[args.length - 1];
      System.arraycopy(args, 1, newArgs, 0, newArgs.length);
      args = newArgs;
    }
    try {
      Method method = Class.forName(mainClass).getDeclaredMethod("main", new Class[] {String[].class});
      method.invoke(null, new Object[] {args});
    } catch(Throwable t) {
      t.printStackTrace();
      System.exit(-1);
    }
    runEventPump();
  }

  protected static void checkPump() {
    if(displayThread == null) {
      throw new IllegalStateException("The native interface handler is not initialized! Please refer to the instructions to set it up properly.");
      // Following code works on Windows, but not on other platforms...
//      final Object LOCK = new Object();
//      synchronized(LOCK) {
//        new Thread("SWT-EventQueue") {
//          @Override
//          public void run() {
//            init();
//            synchronized(LOCK) {
//              LOCK.notifyAll();
//            }
//            runEventPump();
//          }
//        }.start();
//        while(displayThread == null) {
//          try {
//            LOCK.wait();
//          } catch(Exception e) {
//            e.printStackTrace();
//          }
//        }
//      }
    }
  }
  
}
