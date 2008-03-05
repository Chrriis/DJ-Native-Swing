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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
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
import org.eclipse.swt.widgets.Shell;

import chrriis.common.Registry;
import chrriis.common.Utils;
import chrriis.dj.nativeswing.CommandMessage;
import chrriis.dj.nativeswing.LocalMessage;
import chrriis.dj.nativeswing.Message;
import chrriis.dj.nativeswing.NativeInterfaceHandler;
import chrriis.dj.nativeswing.ui.NativeComponent.Options.DestructionTime;
import chrriis.dj.nativeswing.ui.NativeComponent.Options.FiliationType;
import chrriis.dj.nativeswing.ui.NativeComponent.Options.VisibilityConstraint;
import chrriis.dj.nativeswing.ui.event.InitializationEvent;
import chrriis.dj.nativeswing.ui.event.InitializationListener;

import com.sun.jna.Native;

/**
 * @author Christopher Deckers
 */
public abstract class NativeComponent extends Canvas {

  static Object syncExec(Control control, CommandMessage commandMessage, Object... args) {
    NativeInterfaceHandler.checkUIThread();
    if(commandMessage instanceof ControlCommandMessage) {
      ((ControlCommandMessage)commandMessage).setControl(control);
    }
    return commandMessage.syncExecArgs(args);
  }
  
  static void asyncExec(Control control, CommandMessage commandMessage, Object... args) {
    NativeInterfaceHandler.checkUIThread();
    if(commandMessage instanceof ControlCommandMessage) {
      ((ControlCommandMessage)commandMessage).setControl(control);
    }
    commandMessage.asyncExecArgs(args);
  }
  
  public Object syncExec(CommandMessage commandMessage, Object... args) {
    NativeInterfaceHandler.checkUIThread();
    if(commandMessage instanceof ControlCommandMessage) {
      ((ControlCommandMessage)commandMessage).setNativeComponent(this);
    }
    return commandMessage.syncExecArgs(args);
  }
  
  public void asyncExec(CommandMessage commandMessage, Object... args) {
    NativeInterfaceHandler.checkUIThread();
    if(commandMessage instanceof ControlCommandMessage) {
      ((ControlCommandMessage)commandMessage).setNativeComponent(this);
    }
    commandMessage.asyncExecArgs(args);
  }
  
  private class CMLocal_execRunnable extends LocalMessage {
    @Override
    public Object run() {
      ((Runnable)args[0]).run();
      return null;
    }
  }

  public void asyncExec(Runnable runnable) {
    runAsync(new CMLocal_execRunnable(), runnable);
  }
  
  private volatile List<CommandMessage> initializationCommandMessageList = new ArrayList<CommandMessage>();

  /**
   * Run the given command if the control is created, or store it to play it when the creation occurs.
   * If the component is disposed before the command has a chance to run, it is ignored silently.
   */
  protected Object runSync(CommandMessage commandMessage, Object... args) {
    NativeInterfaceHandler.checkUIThread();
    if(initializationCommandMessageList != null) {
      commandMessage.setArgs(args);
      if(commandMessage instanceof ControlCommandMessage) {
        ((ControlCommandMessage)commandMessage).setNativeComponent(this);
      }
      initializationCommandMessageList.add(commandMessage);
      return null;
    }
    if(!isValidControl()) {
      commandMessage.setArgs(args);
      printFailedInvocation(commandMessage);
      return null;
    }
    return syncExec(commandMessage, args);
  }
  
  /**
   * Run the given command if the control is created, or store it to play it when the creation occurs.
   * If the component is disposed before the command has a chance to run, it is ignored silently.
   */
  protected void runAsync(CommandMessage commandMessage, Object... args) {
    NativeInterfaceHandler.checkUIThread();
    if(initializationCommandMessageList != null) {
      commandMessage.setArgs(args);
      if(commandMessage instanceof ControlCommandMessage) {
        ((ControlCommandMessage)commandMessage).setNativeComponent(this);
      }
      initializationCommandMessageList.add(commandMessage);
    } else if(!isValidControl()) {
      commandMessage.setArgs(args);
      printFailedInvocation(commandMessage);
    } else {
      asyncExec(commandMessage, args);
    }
  }
  
  private void printFailedInvocation(Message message) {
    System.err.println("Invalid " + getClass().getName() + "[" + hashCode() + "]: " + message);
  }
  
  private static Registry registry = new Registry();
  
  protected static Registry getRegistry() {
    return registry;
  }
  
  protected abstract static class ControlCommandMessage extends CommandMessage {
    private int componentID;
    public int getComponentID() {
      return componentID;
    }
    public void setControl(Control control) {
      this.componentID = (Integer)control.getData("NS_ID");
    }
    public void setNativeComponent(NativeComponent nativeComponent) {
      this.componentID = nativeComponent.componentID;
    }
    public Control getControl() {
      return (Control)NativeComponent.registry.get(componentID);
    }
    public NativeComponent getComponent() {
      return (NativeComponent)NativeComponent.registry.get(componentID);
    }
    @Override
    public boolean isValid() {
      if(NativeInterfaceHandler.isNativeSide()) {
        return getControl() != null;
      }
      return getComponent() != null;
    }
  }
  
  private static class CMN_reshape extends ControlCommandMessage {
    @Override
    public Object run() throws Exception {
      getControl().getShell().setSize((Integer)args[0], (Integer)args[1]);
      return null;
    }
  }

  private int componentID;
  
  protected int getComponentID() {
    return componentID;
  }

  private static class CMN_transferFocus extends ControlCommandMessage {
    @Override
    public Object run() throws Exception {
      getControl().traverse(SWT.TRAVERSE_TAB_NEXT);
      return null;
    }
  }

  public NativeComponent() {
    componentID = NativeComponent.registry.add(this);
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        if(isValidControl() && !isDisposed()) {
          runSync(new CMN_transferFocus());
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
  
  protected Thread resizeThread;
  
  @SuppressWarnings("deprecation")
  @Override
  public void reshape(int x, int y, int width, int height) {
    if(resizeThread == null && width != getWidth() || height != getHeight()) {
      resizeThread = new Thread("NativeSwing Resize") {
        @Override
        public void run() {
          try {
            sleep(50);
          } catch(Exception e) {
          }
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              if(isValidControl()) {
                resizeThread = null;
                asyncExec(new CMN_reshape(), getWidth(), getHeight());
              }
            }
          });
        }
      };
      resizeThread.start();
    }
    super.reshape(x, y, width, height);
  }
  
  private static class CMJ_dispatchMouseEvent extends ControlCommandMessage {
    private static int buttonPressedCount;
    private static Point lastLocation = new Point(-1, -1);
    @Override
    public void setArgs(Object... args) {
      org.eclipse.swt.events.MouseEvent e = (org.eclipse.swt.events.MouseEvent)args[0];
      super.setArgs(args[1], e.x, e.y, e.button, e.count, e.stateMask, e.display.getCursorLocation());
    }
    @Override
    public Object run() {
      NativeComponent nativeComponent = getComponent();
      if(!nativeComponent.isShowing()) {
        return null;
      }
      int type = (Integer)args[0];
      int e_x = (Integer)args[1];
      int e_y = (Integer)args[2];
      int e_button = (Integer)args[3];
      int e_count = (Integer)args[4];
      int e_stateMask = (Integer)args[5];
      Point e_cursorLocation = (Point)args[6];
      switch(type) {
        case MouseEvent.MOUSE_PRESSED:
          buttonPressedCount++;
          break;
        case MouseEvent.MOUSE_RELEASED:
          buttonPressedCount--;
          break;
        case MouseEvent.MOUSE_DRAGGED:
        case MouseEvent.MOUSE_MOVED:
          Point newLocation = new Point(e_x, e_y);
          if(newLocation.equals(lastLocation)) {
            return null;
          }
          lastLocation = newLocation;
          break;
      }
      int button = UIUtils.translateMouseButton(e_button);
      if(button == 0) {
        switch(type) {
          case MouseEvent.MOUSE_PRESSED:
          case MouseEvent.MOUSE_RELEASED:
          case MouseEvent.MOUSE_CLICKED:
            return null;
        }
      }
      if(buttonPressedCount != 0 && type == MouseEvent.MOUSE_MOVED) {
        type = MouseEvent.MOUSE_DRAGGED;
      }
      final MouseEvent me;
      if(Utils.IS_JAVA_6_OR_GREATER) {
        // Not specifying the absX and Y in Java 6 results in a deadlock when pressing alt+F4 while moving the mouse over a native control
        if(type == MouseEvent.MOUSE_WHEEL) {
          me = new MouseWheelEvent(nativeComponent, type, System.currentTimeMillis(), UIUtils.translateModifiers(e_stateMask), e_x, e_y, e_cursorLocation.x, e_cursorLocation.y, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, e_count, 1);
        } else {
          me = new MouseEvent(nativeComponent, type, System.currentTimeMillis(), UIUtils.translateModifiers(e_stateMask), e_x, e_y, e_cursorLocation.x, e_cursorLocation.y, e_count, false, button);
        }
      } else {
        if(type == MouseEvent.MOUSE_WHEEL) {
          me = new MouseWheelEvent(nativeComponent, type, System.currentTimeMillis(), UIUtils.translateModifiers(e_stateMask), e_x, e_y, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, e_count, 1);
        } else {
          me = new MouseEvent(nativeComponent, type, System.currentTimeMillis(), UIUtils.translateModifiers(e_stateMask), e_x, e_y, e_count, false, button);
        }
      }
      nativeComponent.dispatchEvent(me);
      return null;
    }
  }
  
  private static class CMJ_dispatchKeyEvent extends ControlCommandMessage {
    @Override
    public void setArgs(Object... args) {
      org.eclipse.swt.events.KeyEvent e = (org.eclipse.swt.events.KeyEvent)args[0];
      super.setArgs(args[1], e.stateMask, e.character, e.keyCode);
    }
    @Override
    public Object run() {
      NativeComponent nativeComponent = getComponent();
      if(!nativeComponent.isShowing()) {
        return null;
      }
      int type = (Integer)args[0];
      int e_stateMask = (Integer)args[1];
      char e_character = (Character)args[2];
      int e_keyCode = (Integer)args[3];
      if(e_keyCode == SWT.TAB) {
        if(type == KeyEvent.KEY_PRESSED) {
          if((e_stateMask & SWT.CONTROL) != 0) {
            boolean isBackward = (e_stateMask & SWT.SHIFT) != 0;
            if(isBackward) {
              nativeComponent.transferFocusBackward();
            } else {
              nativeComponent.transferFocus();
            }
          }
        }
        return null;
      }
      char character = e_character;
      int keyCode;
      if(type == KeyEvent.KEY_TYPED) {
        if(character == '\0') {
          return null;
        }
        keyCode = KeyEvent.VK_UNDEFINED;
      } else {
        keyCode = UIUtils.translateKeyCode(e_keyCode);
      }
      final KeyEvent ke = new KeyEvent(nativeComponent, type, System.currentTimeMillis(), UIUtils.translateModifiers(e_stateMask), keyCode, character);
      nativeComponent.dispatchEvent(ke);
      return null;
    }
  }
  
  private static class CMN_createControl extends CommandMessage {
    public Shell createShell(long handle) throws Exception {
      // these are the methods that are in the Shell class, and can create the embedded shell:
      // win32: public static Shell win32_new (Display display, int handle) {
      // photon: public static Shell photon_new (Display display, int handle) {
      // motif: public static Shell motif_new (Display display, int handle) {
      // gtk: public static Shell gtk_new (Display display, int /*long*/ handle) {
      // carbon: Shell (Display display, Shell parent, int style, int handle) {
      Method shellCreationMethod = null;
      try {
        shellCreationMethod = Shell.class.getMethod(SWT.getPlatform() + "_new", Display.class, int.class); 
      } catch(Exception e) {}
      if(shellCreationMethod != null) {
        return (Shell)shellCreationMethod.invoke(null, NativeInterfaceHandler.getDisplay(), (int)handle);
      }
      try {
        shellCreationMethod = Shell.class.getMethod(SWT.getPlatform() + "_new", Display.class, long.class); 
      } catch(Exception e) {}
      if(shellCreationMethod != null) {
        return (Shell)shellCreationMethod.invoke(null, NativeInterfaceHandler.getDisplay(), handle);
      }
      Constructor<Shell> shellConstructor = null;
      try {
        shellConstructor = Shell.class.getConstructor(Display.class, Shell.class, int.class, int.class); 
      } catch(Exception e) {}
      if(shellConstructor != null) {
        shellConstructor.setAccessible(true);
        return shellConstructor.newInstance(NativeInterfaceHandler.getDisplay(), null, SWT.NO_TRIM, (int)handle);
      }
      try {
        shellConstructor = Shell.class.getConstructor(Display.class, Shell.class, int.class, long.class); 
      } catch(Exception e) {}
      if(shellConstructor != null) {
        shellConstructor.setAccessible(true);
        return shellConstructor.newInstance(NativeInterfaceHandler.getDisplay(), null, SWT.NO_TRIM, handle);
      }
      throw new IllegalStateException("Failed to create a Shell!");
    }
    @Override
    public Object run() throws Exception {
      Shell shell = createShell((Long)args[2]);
      shell.setVisible (true);
      shell.setLayout(new FillLayout());
      int componentID = (Integer)args[0];
      Method createControlMethod = Class.forName((String)args[1]).getDeclaredMethod("createControl", Shell.class);
      createControlMethod.setAccessible(true);
      Control control = (Control)createControlMethod.invoke(null, shell);
      NativeComponent.registry.add(control, componentID);
      configureControl(control, componentID);
      return null;
    }
  }

  protected static void configureControl(final Control control, int componentID) {
    control.setData("NS_ID", componentID);
    control.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
        NativeComponent.asyncExec(control, new CMJ_dispatchMouseEvent(), e, MouseEvent.MOUSE_PRESSED);
      }
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
        NativeComponent.asyncExec(control, new CMJ_dispatchMouseEvent(), e, MouseEvent.MOUSE_RELEASED);
      }
    });
    control.addMouseMoveListener(new MouseMoveListener() {
      public void mouseMove(org.eclipse.swt.events.MouseEvent e) {
        NativeComponent.asyncExec(control, new CMJ_dispatchMouseEvent(), e, MouseEvent.MOUSE_MOVED);
      }
    });
    control.addMouseWheelListener(new MouseWheelListener() {
      public void mouseScrolled(org.eclipse.swt.events.MouseEvent e) {
        NativeComponent.asyncExec(control, new CMJ_dispatchMouseEvent(), e, MouseEvent.MOUSE_WHEEL);
      }
    });
    control.addKeyListener(new KeyListener() {
      public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
        if((e.stateMask & SWT.CONTROL) != 0 && e.keyCode == SWT.TAB) {
          e.doit = false;
        }
        NativeComponent.asyncExec(control, new CMJ_dispatchKeyEvent(), e, KeyEvent.KEY_PRESSED);
      }
      public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
        NativeComponent.asyncExec(control, new CMJ_dispatchKeyEvent(), e, KeyEvent.KEY_RELEASED);
        // TODO: Maybe innacurate: swing may issue pressed events when a key is stuck. verify this behavior some day.
        NativeComponent.asyncExec(control, new CMJ_dispatchKeyEvent(), e, KeyEvent.KEY_TYPED);
      }
    });
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    String text = invalidControlText;
    if(text == null) {
      text = "Invalid " + getClass().getName() + "[" + hashCode() + "]";
    }
    if(!isValidControl()) {
      FontMetrics fm = g.getFontMetrics();
      BufferedReader r = new BufferedReader(new StringReader(text));
      int lineHeight = fm.getHeight();
      int ascent = fm.getAscent();
      try {
        String line;
        for(int i=0; (line=r.readLine()) != null; i++) {
          g.drawString(line, 5, ascent + 5 + lineHeight * i);
        }
      } catch(Exception e) {
      }
    }
  }
  
  @Override
  public void addNotify() {
    super.addNotify();
    NativeInterfaceHandler.checkUIThread();
    NativeInterfaceHandler._Internal_.addCanvas(this);
    if(initializationCommandMessageList == null) {
      throw new IllegalStateException("A native component cannot be re-created after having been disposed! To achieve re-parenting, set the options to use a proxied filiation and a finalization-time destruction.");
    }
    List<CommandMessage> initializationCommandMessageList_ = initializationCommandMessageList;
    initializationCommandMessageList = null;
    isInitialized = true;
    isValidControl = true;
    try {
      runSync(new CMN_createControl(), componentID, NativeComponent.this.getClass().getName(), Native.getComponentID(this));
    } catch(Exception e) {
      isValidControl = false;
      StringBuilder sb = new StringBuilder();
      for(Throwable t = e; t != null; t = t.getCause()) {
        sb.append("    " + t.toString() + "\n");
      }
      invalidControlText = "Failed to create " + NativeComponent.this.getClass().getName() + "[" + NativeComponent.this.hashCode() + "]\n\nReason:\n" + sb.toString();
      e.printStackTrace();
    }
    for(CommandMessage initCommandMessage: initializationCommandMessageList_) {
      if(!isValidControl()) {
        printFailedInvocation(initCommandMessage);
      } else {
        initCommandMessage.asyncExec();
      }
    }
    Object[] listeners = listenerList.getListenerList();
    InitializationEvent e = null;
    for(int i=listeners.length-2; i>=0; i-=2) {
      if(listeners[i] == InitializationListener.class) {
        if(e == null) {
          e = new InitializationEvent(NativeComponent.this);
        }
        ((InitializationListener)listeners[i + 1]).objectInitialized(e);
      }
    }
  }
  
  private static class CMN_destroyControl extends ControlCommandMessage {
    @Override
    public Object run() throws Exception {
      Control control = getControl();
      NativeComponent.registry.remove(getComponentID());
      if(!control.isDisposed()) {
        control.getShell().dispose();
      }
      return null;
    }
  }
  
  @Override
  public void removeNotify() {
    releaseResources();
    super.removeNotify();
  }
  
  private boolean isValidControl;
  private String invalidControlText;
  private boolean isInitialized;
  private boolean isDisposed;
  
  protected void releaseResources() {
    if(!isDisposed) {
      isDisposed = true;
      NativeInterfaceHandler._Internal_.removeCanvas(this);
      runSync(new CMN_destroyControl());
      NativeComponent.registry.remove(componentID);
    }
  }
  
  public boolean isDisposed() {
    return isDisposed;
  }
  
  public boolean isInitialized() {
    return isInitialized;
  }
  
  public boolean isValidControl() {
    return isValidControl && NativeInterfaceHandler._Internal_.isInterfaceAlive();
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
  
  static interface NativeComponentHolder {}
  
  private NativeComponentProxy componentProxy;
  
  void setComponentProxy(NativeComponentProxy componentProxy) {
    this.componentProxy = componentProxy;
  }
  
  public Component getComponentProxy() {
    return componentProxy;
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
  
  private static class CMN_setShellEnabled extends ControlCommandMessage {
    @Override
    public Object run() {
      getControl().getShell().setEnabled((Boolean)args[0]);
      return null;
    }
  }

  private boolean isShellEnabled = true;
  
  /**
   * This method is not part of the public API!
   */
  public void setShellEnabled(boolean isEnabled) {
    if(isEnabled == isShellEnabled) {
      return;
    }
    isShellEnabled = isEnabled;
    runSync(new CMN_setShellEnabled(), isEnabled);
  }

  private static class CMN_hasFocus extends ControlCommandMessage {
    @Override
    public Object run() throws Exception {
      return getControl().isFocusControl();
    }
  }

  @Override
  public boolean hasFocus() {
    boolean hasFocus = super.hasFocus();
    if(!hasFocus && isValidControl() && !isDisposed) {
      return Boolean.TRUE.equals(syncExec(new CMN_hasFocus()));
    }
    return hasFocus;
  }
  
  private static class CMN_getPreferredSize extends ControlCommandMessage {
    @Override
    public Object run() throws Exception {
      Control control = getControl();
      Point cSize = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
      return new Dimension(cSize.x, cSize.y);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension result = null;
    if(isValidControl() && !isDisposed) {
      result = (Dimension)syncExec(new CMN_getPreferredSize());
    }
    if(result == null) {
      result = super.getPreferredSize();
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
  
}
