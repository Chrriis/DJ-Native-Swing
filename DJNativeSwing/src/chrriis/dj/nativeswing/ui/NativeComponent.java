/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.NativeInterfaceHandler;
import chrriis.dj.nativeswing.ui.NativeComponent.Options.DestructionTime;
import chrriis.dj.nativeswing.ui.NativeComponent.Options.FiliationType;
import chrriis.dj.nativeswing.ui.NativeComponent.Options.VisibilityConstraint;
import chrriis.dj.nativeswing.ui.event.InitializationEvent;
import chrriis.dj.nativeswing.ui.event.InitializationListener;

/**
 * @author Christopher Deckers
 */
public abstract class NativeComponent extends Canvas {

  private Shell shell;
  private volatile Control control;
  private volatile List<Runnable> initializationRunnableList = new ArrayList<Runnable>();

  public NativeComponent() {
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        if(control != null) {
          NativeInterfaceHandler.invokeSWT(new Runnable() {
            public void run() {
              if(control != null && !control.isDisposed()) {
                control.traverse(SWT.TRAVERSE_TAB_NEXT);
              }
            }
          });
        }
      }
    });
    // Setting the width/height to Integer.MAX_VALUE is not enough.
    // The following code sets the size one pixel bigger after the first resize and revalidates.
    // This fixes wrong computations of native scrollbars in components like the web browser.
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        if(control != null) {
          int width = getWidth();
          int height = getHeight();
          if(width != 0 && height != 0) {
            removeComponentListener(this);
            control.getDisplay().asyncExec(new Runnable() {
              public void run() {
                if(!control.isDisposed()) {
                  control.setSize(getWidth() + 1, getHeight() + 1);
                  SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                      doLayout();
                      repaint();
                    }
                  });
                }
              }
            });
          }
        }
      }
    });
    setFocusable(true);
    addHierarchyListener(new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
        if((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
          Container parent = getParent();
          if(parent != null && !(parent instanceof NativeComponentHolder)) {
            throw new IllegalStateException("The native component cannot be added directly! Use the createEmbeddableComponent() method to get a component that can be added.");
          }
        }
      }
    });
  }

  static interface NativeComponentHolder {}
  
  private NativeComponentProxy componentProxy;
  
  void setComponentProxy(NativeComponentProxy componentProxy) {
    this.componentProxy = componentProxy;
  }
  
  public Component getComponentProxy() {
    return componentProxy;
  }
  
  private int buttonPressedCount;
  private Point lastLocation = new Point(-1, -1);
  
  protected void dispatchMouseEvent(org.eclipse.swt.events.MouseEvent e, int type) {
    if(!isShowing()) {
      return;
    }
    switch(type) {
      case MouseEvent.MOUSE_PRESSED:
        buttonPressedCount++;
        break;
      case MouseEvent.MOUSE_RELEASED:
        buttonPressedCount--;
        break;
      case MouseEvent.MOUSE_DRAGGED:
      case MouseEvent.MOUSE_MOVED:
        Point newLocation = new Point(e.x, e.y);
        if(newLocation.equals(lastLocation)) {
          return;
        }
        lastLocation = newLocation;
        break;
    }
    int button = UIUtils.translateMouseButton(e.button);
    if(button == 0) {
      switch(type) {
        case MouseEvent.MOUSE_PRESSED:
        case MouseEvent.MOUSE_RELEASED:
        case MouseEvent.MOUSE_CLICKED:
          return;
      }
    }
    if(buttonPressedCount != 0 && type == MouseEvent.MOUSE_MOVED) {
      type = MouseEvent.MOUSE_DRAGGED;
    }
    final MouseEvent me;
    if(Utils.IS_JAVA_6_OR_GREATER) {
      // Not specifying the absX and Y in Java 6 results in a deadlock when pressing alt+F4 while moving the mouse over a native control
      Point cursorLocation = e.display.getCursorLocation();
      if(type == MouseEvent.MOUSE_WHEEL) {
        me = new MouseWheelEvent(this, type, System.currentTimeMillis(), UIUtils.translateModifiers(e.stateMask), e.x, e.y, cursorLocation.x, cursorLocation.y, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, e.count, 1);
      } else {
        me = new MouseEvent(this, type, System.currentTimeMillis(), UIUtils.translateModifiers(e.stateMask), e.x, e.y, cursorLocation.x, cursorLocation.y, e.count, false, button);
      }
    } else {
      if(type == MouseEvent.MOUSE_WHEEL) {
        me = new MouseWheelEvent(this, type, System.currentTimeMillis(), UIUtils.translateModifiers(e.stateMask), e.x, e.y, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, e.count, 1);
      } else {
        me = new MouseEvent(this, type, System.currentTimeMillis(), UIUtils.translateModifiers(e.stateMask), e.x, e.y, e.count, false, button);
      }
    }
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        dispatchEvent(me);
      }
    });
  }
  
  protected void dispatchKeyEvent(org.eclipse.swt.events.KeyEvent e, int type) {
    if(!isShowing()) {
      return;
    }
    char character = e.character;
    int keyCode;
    if(type == KeyEvent.KEY_TYPED) {
      if(character == '\0') {
        return;
      }
      keyCode = KeyEvent.VK_UNDEFINED;
    } else {
      keyCode = UIUtils.translateKeyCode(e.keyCode);
    }
    final KeyEvent ke = new KeyEvent(this, type, System.currentTimeMillis(), UIUtils.translateModifiers(e.stateMask), keyCode, character);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        dispatchEvent(ke);
      }
    });
  }

  @Override
  public void addNotify() {
    super.addNotify();
    if(initializationRunnableList == null) {
      throw new IllegalStateException("A native component cannot be re-created after having been disposed! To achieve re-parenting, set the options to use a proxied filiation and a finalization-time destruction.");
    }
    NativeInterfaceHandler.invokeSWT(new Runnable() {
      public void run() {
        shell = NativeInterfaceHandler.createShell(NativeComponent.this);
        shell.setLayout (new FillLayout());
        if(!NativeComponent.this.isShowing()) {
          shell.setEnabled(false);
        }
        control = createControl(shell);
        if(control != null) {
          // Setting a big size rather than the default (0, 0) helps having proper scrollbars
          control.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
          control.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
              dispatchMouseEvent(e, MouseEvent.MOUSE_PRESSED);
            }
            @Override
            public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
              dispatchMouseEvent(e, MouseEvent.MOUSE_RELEASED);
            }
          });
          control.addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(org.eclipse.swt.events.MouseEvent e) {
              dispatchMouseEvent(e, MouseEvent.MOUSE_MOVED);
            }
          });
          control.addMouseWheelListener(new MouseWheelListener() {
            public void mouseScrolled(org.eclipse.swt.events.MouseEvent e) {
              dispatchMouseEvent(e, MouseEvent.MOUSE_WHEEL);
            }
          });
          control.addKeyListener(new KeyListener() {
            public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
              if((e.stateMask & SWT.CONTROL) != 0 && e.keyCode == SWT.TAB) {
                final boolean isBackward = (e.stateMask & SWT.SHIFT) != 0;
                SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                    if(isBackward) {
                      NativeComponent.this.transferFocusBackward();
                    } else {
                      NativeComponent.this.transferFocus();
                    }
                  }
                });
                e.doit = false;
              }
              dispatchKeyEvent(e, KeyEvent.KEY_PRESSED);
            }
            public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
              dispatchKeyEvent(e, KeyEvent.KEY_RELEASED);
              // Maybe innacurate: swing may issue pressed events when a key is stuck. verify this behavior some day.
              dispatchKeyEvent(e, KeyEvent.KEY_TYPED);
            }
          });
        } else {
          Label label = new Label(shell, SWT.NONE);
          label.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
          label.setText("Failed to create object " + NativeComponent.this.getClass().getName() + "[" + NativeComponent.this.hashCode() + "]");
        }
        List<Runnable> initializationRunnableList_ = initializationRunnableList;
        initializationRunnableList = null;
        for(Runnable initRunnable: initializationRunnableList_) {
          NativeComponent.this.run(initRunnable);
        }
        NativeInterfaceHandler.invokeSwing(new Runnable() {
          public void run() {
            Object[] listeners = listenerList.getListenerList();
            InitializationEvent e = null;
            for(int i=listeners.length-2; i>=0; i-=2) {
              if(listeners[i] == InitializationEvent.class) {
                if(e == null) {
                  e = new InitializationEvent(NativeComponent.this);
                }
                ((InitializationListener)listeners[i + 1]).componentInitialized(e);
              }
            }
          }
        });
      }
    });
  }
  
  /**
   * Run the given command if the control is created, or store it to play it when the creation occurs.
   * If the component is disposed before the command has a chance to run, it is ignored silently.
   */
  protected void run(Runnable runnable) {
    if(isDisposed) {
      return;
    }
    if(initializationRunnableList != null) {
      initializationRunnableList.add(runnable);
    } else if(isValidControl()) {
      NativeInterfaceHandler.invokeSWT(runnable);
    } else {
      System.err.println(getClass().getName() + "[" + hashCode() + "]: invocation failed: " + runnable.getClass().getName());
    }
  }
  
  protected abstract Control createControl(Shell shell);
  
  /**
   * @return true if the control was initialized. If the initialization failed, this would return true but isValidControl would return false.
   */
  public boolean isInitialized() {
    return initializationRunnableList == null;
  }
  
  /**
   * @return true if the component is initialized and is properly created.
   */
  public boolean isValidControl() {
    return control != null && initializationRunnableList == null;
  }
  
  private boolean isDisposed;
  
  public boolean isDisposed() {
    return isDisposed;
  }
  
  @Override
  public void removeNotify() {
    if(shell != null) {
      isDisposed = true;
      final Shell shell_ = shell;
      NativeInterfaceHandler.invokeSWT(new Runnable() {
        public void run() {
          shell.setEnabled(false);
          control.setEnabled(false);
        }
      });
      // We have to defer, because on Linux with the web browser, there are often some asynchronous calls that fail...
      Thread disposeThread = new Thread() {
        @Override
        public void run() {
          try {
            sleep(500);
          } catch(Exception e) {
          }
          Display display = NativeInterfaceHandler.getDisplay();
          if(display != null && !display.isDisposed()) {
            display.asyncExec(new Runnable() {
              public void run() {
                NativeInterfaceHandler.disposeShell(shell_);
              }
            });
          }
        }
      };
      disposeThread.setDaemon(true);
      disposeThread.start();
      shell = null;
      control = null;
    }
    super.removeNotify();
  }

  @Override
  public boolean hasFocus() {
    final boolean[] result = new boolean[1];
    if(control != null) {
      NativeInterfaceHandler.invokeSWT(new Runnable() {
        public void run() {
          if(control != null && !control.isDisposed()) {
            result[0] = control.isFocusControl();
          }
        }
      });
    }
    return result[0] || super.hasFocus();
  }
  
  public Control getControl() {
    return control;
  }
  
  @Override
  public Dimension getPreferredSize() {
    final Dimension result = super.getPreferredSize();
    if(control != null) {
      NativeInterfaceHandler.invokeSWT(new Runnable() {
        public void run() {
          if(control != null && !control.isDisposed()) {
            Point cSize = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            result.width = cSize.x;
            result.height = cSize.y;
          }
        }
      });
    }
    return result;
  }
  
  @Override
  public Dimension getMinimumSize() {
    return new Dimension(0, 0);
  }
  
  protected EventListenerList listenerList = new EventListenerList();
  
  public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
    T[] result = listenerList.getListeners(listenerType);
    if(result.length == 0) { 
      return super.getListeners(listenerType); 
    }
    return result; 
  }
  
  public void addInitializationListener(InitializationListener listener) {
    listenerList.add(InitializationListener.class, listener);
  }
  
  public void removeWebBrowserListener(InitializationListener listener) {
    listenerList.remove(InitializationListener.class, listener);
  }
  
  public InitializationListener[] getInitializationListeners() {
    return listenerList.getListeners(InitializationListener.class);
  }
  
  private static Method redrawMethod;
  private static Method updateMethod;
  
  static {
    System.setProperty("jna.force_hw_popups", "false");
    try {
      redrawMethod = Control.class.getDeclaredMethod("redraw", new Class[] {boolean.class});
      redrawMethod.setAccessible(true);
      updateMethod = Control.class.getDeclaredMethod("update", new Class[] {boolean.class});
      updateMethod.setAccessible(true);
    } catch(Exception e) {
      redrawMethod = null;
      updateMethod = null;
      // Swallow. Not a big deal if we can't have it working.
    }
  }
  
  /**
   * Attempt to force a redraw of the native control. This is useful when a native control shows rendering problems. 
   */
  protected void repaintNativeControl() {
    if(control == null || control.isDisposed()) {
      return;
    }
    if(redrawMethod == null) {
      return;
    }
    control.getDisplay().asyncExec(new Runnable() {
      public void run() {
        try {
          if(control == null || control.isDisposed()) {
            return;
          }
          redrawMethod.invoke(control, Boolean.TRUE);
          updateMethod.invoke(control, Boolean.TRUE);
        } catch(Exception e) {
          // Swallow. Not a big deal if we can't have it working.
        }
      }
    });
  }
  
  private Options options;
  
  private void setOptions(Options options) {
    this.options = options;
  }
  
  public Options getOptions() {
    return options;
  }
  
  public static class Options implements Cloneable {
    
    public static enum FiliationType {
      AUTO,
      DIRECT,
      COMPONENT_PROXYING,
      WINDOW_PROXYING,
    }
    
    private FiliationType filiationType = FiliationType.AUTO;
    
    /**
     * Proxied filiation allows re-parenting and change of component Z-order.
     */
    public void setFiliationType(FiliationType filiationType) {
      if(filiationType == null) {
        filiationType = FiliationType.AUTO;
      }
      this.filiationType = filiationType;
    }

    public FiliationType getFiliationType() {
      return filiationType;
    }
    
    public static enum DestructionTime {
      AUTO,
      ON_REMOVAL,
      ON_FINALIZATION,
    }
    
    private DestructionTime destructionTime = DestructionTime.AUTO;
    
    /**
     * Destruction on finalization allows removal and later re-addition to the user interface. It requires a proxied filiation, and will select one automatically if it is set to default. It is also possible to explicitely dispose the component rather than waiting until finalization.
     */
    public void setDestructionTime(DestructionTime destructionTime) {
      if(destructionTime == null) {
        destructionTime = DestructionTime.AUTO;
      }
      this.destructionTime = destructionTime;
    }
    
    public DestructionTime getDestructionTime() {
      return destructionTime;
    }
    
    public static enum VisibilityConstraint {
      AUTO,
      NONE,
      FULL_COMPONENT_TREE,
    }
    
    private VisibilityConstraint visibilityConstraint = VisibilityConstraint.AUTO;
    
    /**
     * Visibility constraints allow to superimpose native components and Swing components.
     */
    public void setVisibilityConstraint(VisibilityConstraint visibilityConstraint) {
      if(visibilityConstraint == null) {
        visibilityConstraint = VisibilityConstraint.AUTO;
      }
      this.visibilityConstraint = visibilityConstraint;
    }
    
    public VisibilityConstraint getVisibilityConstraint() {
      return visibilityConstraint;
    }
    
    @Override
    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        e.printStackTrace();
        return null;
      }
    }
    
  }
  
  private static Options defaultOptions;
  
  public static Options getDefaultOptions() {
    if(defaultOptions == null) {
      defaultOptions = new Options();
    }
    return defaultOptions;
  }
  
  public static void setDefaultOptions(Options defaultOptions) {
    NativeComponent.defaultOptions = defaultOptions;
  }
  
  private static Options nextInstanceOptions;
  
  /**
   * The next instance options are a copy of the default options from the moment this method is called the first time before a new instance is created.
   */
  public static Options getNextInstanceOptions() {
    if(nextInstanceOptions == null) {
      nextInstanceOptions = (Options)getDefaultOptions().clone();
    }
    return nextInstanceOptions;
  }
  
  public static void setNextInstanceOptions(Options nextInstanceOptions) {
    NativeComponent.nextInstanceOptions = nextInstanceOptions;
  }
  
  static class SimpleNativeComponentHolder extends JPanel implements NativeComponentHolder {
    
    public SimpleNativeComponentHolder(NativeComponent nativeComponent) {
      this();
      add(nativeComponent);
    }
    
    public SimpleNativeComponentHolder() {
      super(new BorderLayout(0, 0));
    }
    
  }
  
  private static boolean isJNAPresent() {
    try {
      Class.forName("com.sun.jna.examples.WindowUtils");
      Class.forName("com.sun.jna.Platform");
      return true;
    } catch(Exception e) {
    }
    return false;
  }
  
  protected Component createEmbeddableComponent() {
    Options nextInstanceOptions = getNextInstanceOptions();
    FiliationType filiationType = nextInstanceOptions.getFiliationType();
    DestructionTime destructionTime = nextInstanceOptions.getDestructionTime();
    if(destructionTime == DestructionTime.AUTO) {
      destructionTime = DestructionTime.ON_REMOVAL;
    }
    VisibilityConstraint visibilityConstraint = nextInstanceOptions.getVisibilityConstraint();
    boolean isJNAPresent = isJNAPresent();
    if(visibilityConstraint == VisibilityConstraint.AUTO) {
      if(!isJNAPresent) {
        visibilityConstraint = VisibilityConstraint.NONE;
      } else {
        switch(filiationType) {
          case COMPONENT_PROXYING:
          case WINDOW_PROXYING:
            visibilityConstraint = VisibilityConstraint.FULL_COMPONENT_TREE;
            break;
          default:
            visibilityConstraint = VisibilityConstraint.NONE;
          break;
        }
      }
    }
    if(visibilityConstraint != VisibilityConstraint.NONE && !isJNAPresent) {
      throw new IllegalStateException("The JNA libraries are required to use the visibility constraints!");
    }
    if(destructionTime == DestructionTime.ON_FINALIZATION && filiationType == FiliationType.AUTO) {
      filiationType = FiliationType.COMPONENT_PROXYING;
    }
    Options options = (Options)nextInstanceOptions.clone();
    options.setDestructionTime(destructionTime);
    options.setFiliationType(filiationType);
    options.setVisibilityConstraint(visibilityConstraint);
    setOptions(options);
    nextInstanceOptions = null;
    switch(filiationType) {
      case COMPONENT_PROXYING:
        return new NativeComponentProxyPanel(this);
      case WINDOW_PROXYING:
        return new NativeComponentProxyWindow(this);
      default:
        switch(destructionTime) {
          case ON_REMOVAL:
            break;
          default:
            throw new IllegalStateException("Finalization-time destruction cannot be used without a proxied filiation!");
        }
        switch(visibilityConstraint) {
          case NONE:
            return new SimpleNativeComponentHolder(this);
          default:
            return new NativeComponentProxyPanel(this);
        }
    }
  }
  
}
