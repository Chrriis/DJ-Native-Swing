/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.NativeInterfaceHandler;
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
  }

  protected NativeComponentProxy componentProxy;
  
  protected void setComponentProxy(NativeComponentProxy componentProxy) {
    this.componentProxy = componentProxy;
  }
  
  public Component getComponentProxy() {
    return componentProxy;
  }
  
  protected int buttonPressedCount;
  protected Point lastLocation = new Point(-1, -1);
  
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
      throw new IllegalStateException("A native component cannot be re-created after having been disposed.");
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
  
  protected boolean isDisposed;
  
  @Override
  public void removeNotify() {
    if(shell != null) {
      isDisposed = true;
      // Postponing removal seems a bit more stable on Linux, mainly when disposing straight after creation.
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          NativeInterfaceHandler.invokeSWT(new Runnable() {
            public void run() {
              NativeInterfaceHandler.disposeShell(shell);
              shell = null;
              control = null;
            }
          });
        }
      });
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
    T[] result = listenerList.getListeners(listenerType);;
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
  
  public static class Preferences implements Cloneable {
    
    public static enum Layering {
      NO_LAYERING,
      COMPONENT_LAYERING,
      WINDOW_LAYERING,
    }
    
    protected Layering layering = Layering.NO_LAYERING;
    
    public void setLayering(Layering layering) {
      if(layering == null) {
        layering = Layering.NO_LAYERING;
      }
      switch(layering) {
        case COMPONENT_LAYERING:
        case WINDOW_LAYERING:
          try {
            Class.forName("com.sun.jna.examples.WindowUtils");
          } catch(Exception e) {
            throw new IllegalStateException("The JNA library is required in the classpath to use the layering mode \"" + layering + "\"!");
          }
      }
      this.layering = layering;
    }

    public Layering getLayering() {
      return layering;
    }
    
    protected boolean isDestroyOnFinalize;
    
    public void setDestroyOnFinalize(boolean isDestroyOnFinalize) {
      this.isDestroyOnFinalize = isDestroyOnFinalize;
    }
    
    public boolean isDestroyOnFinalize() {
      return isDestroyOnFinalize;
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
  
  protected static Preferences defaultPreferences;
  
  public static Preferences getDefaultPreferences() {
    if(defaultPreferences == null) {
      defaultPreferences = new Preferences();
    }
    return defaultPreferences;
  }
  
  public static void setDefaultPreferences(Preferences defaultPreferences) {
    NativeComponent.defaultPreferences = defaultPreferences;
  }
  
  protected static Preferences nextInstancePreferences;
  
  public static Preferences getNextInstancePreferences() {
    if(nextInstancePreferences == null) {
      nextInstancePreferences = (Preferences)getDefaultPreferences().clone();
    }
    return nextInstancePreferences;
  }
  
  public static void setNextInstancePreferences(Preferences nextInstancePreferences) {
    NativeComponent.nextInstancePreferences = nextInstancePreferences;
  }
  
  static {
    System.setProperty("jna.force_hw_popups", "false");
  }
  
  protected Component createEmbeddableComponent() {
    try {
      switch(getNextInstancePreferences().getLayering()) {
        case COMPONENT_LAYERING:
          return new NativeComponentProxyPanel(this);
        case WINDOW_LAYERING:
          return new NativeComponentProxyWindow(this);
        default:
          return this;
      }
    } finally {
      nextInstancePreferences = null;
    }
  }
  
}
