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
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
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

/**
 * @author Christopher Deckers
 */
public class NativeInterfaceHandler {

  protected static volatile Thread displayThread;
  protected static volatile Display display;

  public static void init() {
    if(display != null) {
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
    displayThread = Thread.currentThread();
    display = new Display();
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
      protected void adjustNativeComponents() {
        invokeSWT(new Runnable() {
          public void run() {
            for(int i=canvasList.size()-1; i>=0; i--) {
              Shell shell = shellList.get(i);
              final Canvas canvas = canvasList.get(i);
              boolean isBlocked = blockedWindowSet.contains(SwingUtilities.getWindowAncestor(canvas));
              final boolean isShowing = canvas.isShowing();
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
  }
  
  public static void runEventPump() {
    new Thread("SWT wake-up thread") {
      @Override
      public void run() {
        while(true) {
          try {
            sleep(50);
            // for some reasons, need to wake up everytime it sleeps using a fake event 
            display.asyncExec(new Runnable() {
              public void run() {
              }
            });
          } catch(Exception e) {
            e.printStackTrace();
          }
        }
      }
    }.start();
    while(true) {
      dispatch();
    }
    // TODO: add shutdown hooks for special cleanup?
  }
  
  public static void dispatch() {
    if(display.readAndDispatch()) {
      display.sleep();
    }
  }
  
  public static Display getDisplay() {
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
    if(displayThread == Thread.currentThread()) {
      synchronized(SWING_LOCK) {
        swingRunnableList.add(runnable);
        if(swingRunnableList.size() == 1) {
          hasSwingRun = false;
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              while(true) {
                Runnable r;
                synchronized (SWING_LOCK) {
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
        synchronized (SWT_LOCK) {
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
    if(SwingUtilities.isEventDispatchThread()) {
      synchronized(SWT_LOCK) {
        swtRunnableList.add(runnable);
        if(swtRunnableList.size() == 1) {
          hasSWTRun = false;
          display.asyncExec(new Runnable() {
            public void run() {
              while(true) {
                Runnable r;
                synchronized (SWT_LOCK) {
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
        synchronized (SWING_LOCK) {
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
  
  public static void main(final String[] args) {
    init();
    try {
      Method method = Class.forName(args[0]).getDeclaredMethod("main", new Class[] {String[].class});
      String[] newArgs = new String[args.length - 1];
      System.arraycopy(args, 1, newArgs, 0, newArgs.length);
      method.invoke(null, new Object[] {newArgs});
    } catch(Throwable t) {
      t.printStackTrace();
      System.exit(-1);
    }
    runEventPump();
  }

}
