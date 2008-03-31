/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import chrriis.common.ObjectRegistry;
import chrriis.common.Utils;
import chrriis.dj.nativeswing.NativeComponentOptions.DestructionTime;
import chrriis.dj.nativeswing.NativeComponentOptions.FiliationType;
import chrriis.dj.nativeswing.NativeComponentOptions.VisibilityConstraint;

/**
 * A native component that gets connected to a native peer.
 * @author Christopher Deckers
 */
public abstract class NativeComponent extends Canvas {

  private class CMLocal_runInSequence extends LocalMessage {
    @Override
    public Object run() {
      ((Runnable)args[0]).run();
      return null;
    }
  }

  /**
   * Run a command in sequence with other calls from this class. Calls are performed only when the component is initialized, and this method adds to the queue of calls in case it is not.
   * @param runnable the command to run in sequence with other method calls.
   */
  public void runInSequence(Runnable runnable) {
    runSync(new CMLocal_runInSequence(), runnable);
  }
  
  private volatile List<CommandMessage> initializationCommandMessageList = new ArrayList<CommandMessage>();

  /**
   * Run the given command if the control is created, or store it to run it when the creation occurs.
   * If the component is disposed before the command has a chance to run, it is ignored silently.
   * @param commandMessage the command message to run.
   * @param args the arguments to pass to the command message.
   */
  public Object runSync(CommandMessage commandMessage, Object... args) {
    if(NativeInterface.isAlive()) {
      NativeInterface.checkUIThread();
    }
    if(commandMessage instanceof ControlCommandMessage) {
      ((ControlCommandMessage)commandMessage).setNativeComponent(this);
    }
    if(initializationCommandMessageList != null) {
      commandMessage.setArgs(args);
      initializationCommandMessageList.add(commandMessage);
      return null;
    }
    if(!isNativePeerValid()) {
      commandMessage.setArgs(args);
      printFailedInvocation(commandMessage);
      return null;
    }
    return commandMessage.syncExec(args);
  }
  
  /**
   * Run the given command if the control is created, or store it to run it when the creation occurs.
   * If the component is disposed before the command has a chance to run, it is ignored silently.
   * @param commandMessage the command message to run.
   * @param args the arguments to pass to the command message.
   */
  public void runAsync(CommandMessage commandMessage, Object... args) {
    if(NativeInterface.isAlive()) {
      NativeInterface.checkUIThread();
    }
    if(commandMessage instanceof ControlCommandMessage) {
      ((ControlCommandMessage)commandMessage).setNativeComponent(this);
    }
    if(initializationCommandMessageList != null) {
      commandMessage.setArgs(args);
      initializationCommandMessageList.add(commandMessage);
    } else if(!isNativePeerValid()) {
      commandMessage.setArgs(args);
      printFailedInvocation(commandMessage);
    } else {
      commandMessage.asyncExec(args);
    }
  }
  
  private void printFailedInvocation(Message message) {
    System.err.println("Invalid " + getClass().getName() + "[" + hashCode() + "]: " + message);
  }
  
  static ObjectRegistry registry = new ObjectRegistry();
  
  /**
   * Get the registry of the components, which references created components/controls using the component ID.
   * @return the registry.
   */
  protected static ObjectRegistry getRegistry() {
    return registry;
  }
  
  private static class CMN_reshape extends ControlCommandMessage {
    @Override
    public Object run() throws Exception {
      getControl().getShell().setSize((Integer)args[0], (Integer)args[1]);
      return null;
    }
  }

  private int componentID;
  
  /**
   * Get the unique identifier of this native component, used as a reference to communicate with the native peer.
   * @return the component ID.
   */
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

  /**
   * Construct a native component.
   */
  public NativeComponent() {
    componentID = NativeComponent.registry.add(this);
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        if(isNativePeerValid() && !isNativePeerDisposed()) {
          runSync(new CMN_transferFocus());
        }
      }
    });
    setFocusable(true);
    addHierarchyListener(new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
        long changeFlags = e.getChangeFlags();
        if((changeFlags & HierarchyEvent.PARENT_CHANGED) != 0) {
          Container parent = getParent();
          if(parent != null && !(parent instanceof NativeComponentHolder)) {
            throw new IllegalStateException("The native component cannot be added directly! Use the createEmbeddableComponent() method to get a component that can be added.");
          }
        }
      }
    });
  }
  
  private Thread resizeThread;
  
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
              if(isNativePeerValid()) {
                resizeThread = null;
                new CMN_reshape().asyncExec(NativeComponent.this, getWidth(), getHeight());
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
    @Override
    public Object run() {
      NativeComponent nativeComponent = getNativeComponent();
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
          break;
      }
      int button = SWTUtils.translateSWTMouseButton(e_button);
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
          me = new MouseWheelEvent(nativeComponent, type, System.currentTimeMillis(), SWTUtils.translateSWTModifiers(e_stateMask), e_x, e_y, e_cursorLocation.x, e_cursorLocation.y, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, e_count, 1);
        } else {
          me = new MouseEvent(nativeComponent, type, System.currentTimeMillis(), SWTUtils.translateSWTModifiers(e_stateMask), e_x, e_y, e_cursorLocation.x, e_cursorLocation.y, e_count, false, button);
        }
      } else {
        if(type == MouseEvent.MOUSE_WHEEL) {
          me = new MouseWheelEvent(nativeComponent, type, System.currentTimeMillis(), SWTUtils.translateSWTModifiers(e_stateMask), e_x, e_y, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, e_count, 1);
        } else {
          me = new MouseEvent(nativeComponent, type, System.currentTimeMillis(), SWTUtils.translateSWTModifiers(e_stateMask), e_x, e_y, e_count, false, button);
        }
      }
      nativeComponent.dispatchEvent(me);
      return null;
    }
  }
  
  private static Object[] getKeyEventArgs(org.eclipse.swt.events.KeyEvent keyEvent, int keyEventType) {
    return new Object[] {keyEventType, keyEvent.stateMask, keyEvent.character, keyEvent.keyCode};
  }
  
  private static class CMJ_dispatchKeyEvent extends ControlCommandMessage {
    @Override
    public Object run() {
      NativeComponent nativeComponent = getNativeComponent();
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
        keyCode = SWTUtils.translateSWTKeyCode(e_keyCode);
      }
      final KeyEvent ke = new KeyEvent(nativeComponent, type, System.currentTimeMillis(), SWTUtils.translateSWTModifiers(e_stateMask), keyCode, character);
      nativeComponent.dispatchEvent(ke);
      return null;
    }
  }
  
  private static class CMN_createControl extends CommandMessage {
    public Shell createShell(Object handle) throws Exception {
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
        return (Shell)shellCreationMethod.invoke(null, NativeInterface.getDisplay(), handle);
      }
      try {
        shellCreationMethod = Shell.class.getMethod(SWT.getPlatform() + "_new", Display.class, long.class); 
      } catch(Exception e) {}
      if(shellCreationMethod != null) {
        return (Shell)shellCreationMethod.invoke(null, NativeInterface.getDisplay(), handle);
      }
      Constructor<Shell> shellConstructor = null;
      try {
        shellConstructor = Shell.class.getConstructor(Display.class, Shell.class, int.class, int.class); 
      } catch(Exception e) {}
      if(shellConstructor != null) {
        shellConstructor.setAccessible(true);
        return shellConstructor.newInstance(NativeInterface.getDisplay(), null, SWT.NO_TRIM, handle);
      }
      try {
        shellConstructor = Shell.class.getConstructor(Display.class, Shell.class, int.class, long.class); 
      } catch(Exception e) {}
      if(shellConstructor != null) {
        shellConstructor.setAccessible(true);
        return shellConstructor.newInstance(NativeInterface.getDisplay(), null, SWT.NO_TRIM, handle);
      }
      throw new IllegalStateException("Failed to create a Shell!");
    }
    @Override
    public Object run() throws Exception {
      Shell shell = createShell(args[2]);
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

  private static Object[] getMouseEventArgs(Control control, org.eclipse.swt.events.MouseEvent e, int mouseEventType) {
    org.eclipse.swt.events.MouseEvent lastEvent = (org.eclipse.swt.events.MouseEvent)control.getData("NS_LastMouseEvent");
    if(lastEvent != null) {
      Integer lastEventType = (Integer)control.getData("NS_LastMouseEventType");
      if(lastEventType.intValue() == mouseEventType &&
          lastEvent.x == e.x &&
          lastEvent.y == e.y &&
          lastEvent.button == e.button &&
          lastEvent.count == e.count &&
          lastEvent.stateMask == e.stateMask
          ) {
        return null;
      }
    }
    control.setData("NS_LastMouseEvent", e);
    control.setData("NS_LastMouseEventType", mouseEventType);
    lastEvent = e;
    return new Object[] {mouseEventType, e.x, e.y, e.button, e.count, e.stateMask, e.display.getCursorLocation()};
  }
  
  private static void configureControl(final Control control, int componentID) {
    control.setData("NS_ID", componentID);
    control.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
        Object[] mouseEventArgs = getMouseEventArgs(control, e, MouseEvent.MOUSE_PRESSED);
        if(mouseEventArgs != null) {
          new CMJ_dispatchMouseEvent().asyncExec(control, mouseEventArgs);
        }
      }
      @Override
      public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
        Object[] mouseEventArgs = getMouseEventArgs(control, e, MouseEvent.MOUSE_RELEASED);
        if(mouseEventArgs != null) {
          new CMJ_dispatchMouseEvent().asyncExec(control, mouseEventArgs);
        }
      }
    });
    control.addMouseMoveListener(new MouseMoveListener() {
      public void mouseMove(org.eclipse.swt.events.MouseEvent e) {
        Object[] mouseEventArgs = getMouseEventArgs(control, e, MouseEvent.MOUSE_MOVED);
        if(mouseEventArgs != null) {
          new CMJ_dispatchMouseEvent().asyncExec(control, mouseEventArgs);
        }
      }
    });
    control.addMouseWheelListener(new MouseWheelListener() {
      public void mouseScrolled(org.eclipse.swt.events.MouseEvent e) {
        Object[] mouseEventArgs = getMouseEventArgs(control, e, MouseEvent.MOUSE_WHEEL);
        if(mouseEventArgs != null) {
          new CMJ_dispatchMouseEvent().asyncExec(control, mouseEventArgs);
        }
      }
    });
    control.addKeyListener(new KeyListener() {
      public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
        if((e.stateMask & SWT.CONTROL) != 0 && e.keyCode == SWT.TAB) {
          e.doit = false;
        }
        new CMJ_dispatchKeyEvent().asyncExec(control, getKeyEventArgs(e, KeyEvent.KEY_PRESSED));
      }
      public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
        new CMJ_dispatchKeyEvent().asyncExec(control, getKeyEventArgs(e, KeyEvent.KEY_RELEASED));
        // TODO: Maybe innacurate: swing may issue pressed events when a key is stuck. verify this behavior some day.
        new CMJ_dispatchKeyEvent().asyncExec(control, getKeyEventArgs(e, KeyEvent.KEY_TYPED));
      }
    });
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    String text = invalidNativePeerText;
    if(text == null) {
      text = "Invalid " + getClass().getName() + "[" + hashCode() + "]";
    }
    if(!isNativePeerValid()) {
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
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if(!isNativePeerInitialized && !isNativePeerDisposed) {
          createNativePeer();
        }
      }
    });
  }
  
  /**
   * Force the component to initialize. All method calls will then be synchronous instead of being queued waiting for the componant to be initialized.
   * This call fails if the component is not in a component hierarchy with a Window ancestor.
   */
  public void initializeNativePeer() {
    if(NativeInterface.isAlive()) {
      NativeInterface.checkUIThread();
    }
    Window windowAncestor = SwingUtilities.getWindowAncestor(this);
    if(windowAncestor == null) {
      throw new IllegalStateException("This method can only be called when the component has a Window ancestor!");
    }
    if(!isNativePeerInitialized && !isNativePeerDisposed) {
      windowAncestor.addNotify();
      createNativePeer();
    }
  }
  
  private Method getAWTHandleMethod;
  private Object getHandle() {
    try {
      if(getAWTHandleMethod == null) {
        Method loadLibraryMethod = SWT_AWT.class.getDeclaredMethod("loadLibrary");
        loadLibraryMethod.setAccessible(true);
        loadLibraryMethod.invoke(null);
        getAWTHandleMethod = SWT_AWT.class.getDeclaredMethod("getAWTHandle", Canvas.class);
        getAWTHandleMethod.setAccessible(true);
      }
      return getAWTHandleMethod.invoke(null, this);
    } catch(Exception e) {
      e.printStackTrace();
    }
    return 0;
  }
  
  private void createNativePeer() {
    boolean isInterfaceAlive = NativeInterface.isAlive();
    if(isInterfaceAlive) {
      NativeInterface.checkUIThread();
    }
    NativeInterface.addCanvas(this);
    if(initializationCommandMessageList == null) {
      isNativePeerValid = false;
      invalidNativePeerText = "Failed to create " + NativeComponent.this.getClass().getName() + "[" + NativeComponent.this.hashCode() + "]\n\nReason:\nA native component cannot be re-created after having been disposed.";
      throw new IllegalStateException("A native component cannot be re-created after having been disposed! To achieve re-parenting, set the options to use a proxied filiation and a destruction on finalization (re-parenting accross different frames is not supported).");
    }
    List<CommandMessage> initializationCommandMessageList_ = initializationCommandMessageList;
    initializationCommandMessageList = null;
    isNativePeerInitialized = true;
    isNativePeerValid = true;
    if(isInterfaceAlive) {
      try {
        runSync(new CMN_createControl(), componentID, NativeComponent.this.getClass().getName(), getHandle());
      } catch(Exception e) {
        isNativePeerValid = false;
        StringBuilder sb = new StringBuilder();
        for(Throwable t = e; t != null; t = t.getCause()) {
          sb.append("    " + t.toString() + "\n");
        }
        invalidNativePeerText = "Failed to create " + NativeComponent.this.getClass().getName() + "[" + NativeComponent.this.hashCode() + "]\n\nReason:\n" + sb.toString();
        e.printStackTrace();
      }
      new CMN_reshape().asyncExec(this, getWidth(), getHeight());
    } else {
      isNativePeerValid = false;
      invalidNativePeerText = "Failed to create " + NativeComponent.this.getClass().getName() + "[" + NativeComponent.this.hashCode() + "]\n\nReason:\nThe native interface is not alive! It may not have been initialized.";
    }
    for(CommandMessage initCommandMessage: initializationCommandMessageList_) {
      if(!isNativePeerValid()) {
        printFailedInvocation(initCommandMessage);
      } else {
        initCommandMessage.asyncSend();
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
      control.dispose();
      return null;
    }
  }
  
  @Override
  public void removeNotify() {
    disposeNativePeer();
    super.removeNotify();
  }
  
  private boolean isNativePeerValid;
  private String invalidNativePeerText;
  private boolean isNativePeerInitialized;
  private boolean isNativePeerDisposed;
  
  /**
   * Explicitely dispose the native resources. This is particularly useful if deferred destruction is used (cf native component options) and the component is not going to be used anymore.
   */
  protected void disposeNativePeer() {
    if(!isNativePeerDisposed) {
      isNativePeerDisposed = true;
      if(isNativePeerInitialized) {
        NativeInterface.removeCanvas(this);
        if(isNativePeerValid()) {
          runSync(new CMN_destroyControl());
        }
      }
      NativeComponent.registry.remove(componentID);
      isNativePeerValid = false;
      if(nativeComponentProxy != null) {
        nativeComponentProxy.dispose();
      }
    }
  }
  
  /**
   * Indicate whether the native peer is disposed.
   * @return true if the native peer is disposed. This method returns false if the native peer is not initialized.
   */
  public boolean isNativePeerDisposed() {
    return isNativePeerDisposed;
  }
  
  /**
   * Indicate whether the native peer initialization phase has happened. This method returns true even if the native peer is disposed of if the creation of the peer failed.
   * @return true if the native peer is initialized.
   */
  public boolean isNativePeerInitialized() {
    return isNativePeerInitialized;
  }
  
  /**
   * Indicate if the native peer is valid, which means initialized, not disposed, and alive (communication channel is alive).
   * @return true if the native peer is valid.
   */
  public boolean isNativePeerValid() {
    return isNativePeerValid && NativeInterface.isAlive();
  }
  
  void invalidateNativePeer(String invalidNativePeerText) {
    if(isNativePeerValid) {
      isNativePeerValid = false;
      this.invalidNativePeerText = "Invalid " + getClass().getName() + "[" + hashCode() + "]\n\n" + invalidNativePeerText;
      repaint();
    }
  }
  
  private NativeComponentOptions options;
  
  private void setOptions(NativeComponentOptions options) {
    this.options = options;
  }
  
  NativeComponentOptions getOptions() {
    return options;
  }
  
  static interface NativeComponentHolder {}
  
  private NativeComponentProxy nativeComponentProxy;
  
  void setNativeComponentProxy(NativeComponentProxy nativeComponentProxy) {
    this.nativeComponentProxy = nativeComponentProxy;
  }
  
  NativeComponentProxy getNativeComponentProxy() {
    return nativeComponentProxy;
  }
  
  static class SimpleNativeComponentHolder extends JPanel implements NativeComponentHolder {
    
    private NativeComponent nativeComponent;
    
    public SimpleNativeComponentHolder(NativeComponent nativeComponent) {
      super(new BorderLayout(0, 0));
      this.nativeComponent = nativeComponent;
      add(nativeComponent);
    }
    
    @Override
    public void print(Graphics g) {
      BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
      nativeComponent.paintComponent(image);
      g.drawImage(image, 0, 0, null);
      super.print(g);
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
  
  /**
   * A native component instance cannot be added directly to a component hierarchy. This method needs to be called to get a component that will add the native component.
   * @return the component that contains the native component and that can be added to the component hierarchy.
   */
  protected Component createEmbeddableComponent() {
    NativeComponentOptions nextInstanceOptions = NativeComponentOptions.getNextInstanceOptions();
    NativeComponentOptions.setNextInstanceOptions(null);
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
    NativeComponentOptions options = (NativeComponentOptions)nextInstanceOptions.clone();
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
  
  void setShellEnabled(boolean isEnabled) {
    if(isEnabled == isShellEnabled) {
      return;
    }
    isShellEnabled = isEnabled;
    runAsync(new CMN_setShellEnabled(), isEnabled);
  }

  private static class CMN_setEnabled extends ControlCommandMessage {
    @Override
    public Object run() {
      getControl().setEnabled((Boolean)args[0]);
      return null;
    }
  }

  /**
   * Set whether this component and its native peer are enabled.
   * @param isEnabled true it the component and its native peer should be enabled, false otherwise.
   */
  @Override
  public void setEnabled(boolean isEnabled) {
    super.setEnabled(isEnabled);
    runAsync(new CMN_setEnabled(), isEnabled);
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
    if(!hasFocus && isNativePeerValid() && !isNativePeerDisposed) {
      return Boolean.TRUE.equals(new CMN_hasFocus().syncExec(this));
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
    if(isNativePeerValid() && !isNativePeerDisposed) {
      result = (Dimension)new CMN_getPreferredSize().syncExec(this);
    }
    if(result == null) {
      result = super.getPreferredSize();
    }
    return result;
  }
  
  private static class CMN_getComponentImage extends ControlCommandMessage {
    private ImageData getImageData(Control control, Region region) {
      if(control.isDisposed()) {
        return null;
      }
      Point size = control.getSize();
      if(size.x <= 0 || size.y <= 0) {
        return null;
      }
      final Image image = new Image(NativeInterface.getDisplay(), size.x, size.y);
      GC gc = new GC(image);
      gc.setClipping(region);
      control.print(gc);
      gc.dispose();
      ImageData imageData = image.getImageData();
      image.dispose();
      return imageData;
    }
    @Override
    public Object run() throws Exception {
      int port = (Integer)args[0];
      Rectangle[] area = (Rectangle[])args[1];
      final Control control = getControl();
      ImageData imageData;
      final Region region = new Region();
      for(Rectangle rectangle: area) {
        region.add(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
      }
      if(!NativeInterface.isUIThread()) {
        final Exception[] eArray = new Exception[1];
        final ImageData[] resultArray = new ImageData[1];
        control.getDisplay().syncExec(new Runnable() {
          public void run() {
            try {
              resultArray[0] = getImageData(control, region);
            } catch (Exception e) {
              eArray[0] = e;
            }
          }
        });
        if(eArray[0] != null) {
          throw eArray[0];
        }
        imageData = resultArray[0];
      } else {
        imageData = getImageData(control, region);
      }
      if(imageData == null) {
        return null;
      }
      int cursor = 0;
      // Has to be a multiple of 3
      byte[] bytes = new byte[1024 * 3];
      PaletteData palette = imageData.palette;
      if (palette.isDirect) {
        Socket socket = new Socket("127.0.0.1", port);
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        int width = imageData.width;
        int height = imageData.height;
        try {
          for(Rectangle rectangle: area) {
            // TODO: check that imageData.width/height are large enough
            for(int i=0; i<rectangle.width; i++) {
              for(int j=0; j<rectangle.height; j++) {
                int x = rectangle.x + i;
                int y = rectangle.y + j;
                if(x < width && y < height) {
                  int pixel = imageData.getPixel(x, y);
                  int red = pixel & palette.redMask;
                  bytes[cursor++] = (byte)(palette.redShift < 0? red >>> -palette.redShift: red << palette.redShift);
                  int green = pixel & palette.greenMask;
                  bytes[cursor++] = (byte)((palette.greenShift < 0)? green >>> -palette.greenShift: green << palette.greenShift);
                  int blue = pixel & palette.blueMask;
                  bytes[cursor++] = (byte)((palette.blueShift < 0)? blue >>> -palette.blueShift: blue << palette.blueShift);
                } else {
                  cursor += 3;
                }
                if(cursor == bytes.length) {
                  out.write(bytes);
                  cursor = 0;
                }
              }
            }
          }
          out.write(bytes, 0, cursor);
        } catch(Exception e) {
          // swallow the exception, in case the socket gets closed for example.
        }
        out.close();
        socket.close();
        return null;
      }
      throw new IllegalStateException("Not implemented");
    }
  }
  
  /**
   * Can be called from a non-UI thread.
   */
  public void paintComponent(BufferedImage image) {
    paintComponent(image, null);
  }
  
  /**
   * Can be called from a non-UI thread.
   */
  public void paintComponent(BufferedImage image, Rectangle[] area) {
    if(image == null || !isNativePeerValid() || isNativePeerDisposed) {
      return;
    }
    int width = Math.min(getWidth(), image.getWidth());
    int height = Math.min(getHeight(), image.getHeight());
    if(width <= 0 || height <= 0) {
      return;
    }
    if(area == null) {
      area = new Rectangle[] {new Rectangle(width, height)};
    }
    Rectangle bounds = new Rectangle(width, height);
    List<Rectangle> rectangleList = new ArrayList<Rectangle>();
    for(Rectangle rectangle: area) {
      if(rectangle.intersects(bounds)) {
        rectangleList.add(rectangle.intersection(bounds));
      }
    }
    if(rectangleList.isEmpty()) {
      return;
    }
    area = rectangleList.toArray(new Rectangle[0]);
    if(nativeComponentProxy != null) {
      try {
        nativeComponentProxy.startCapture();
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    try {
      ServerSocket serverSocket = new ServerSocket(0);
      CMN_getComponentImage getComponentImage = new CMN_getComponentImage();
      getComponentImage.setNativeComponent(this);
      getComponentImage.asyncExec(serverSocket.getLocalPort(), area);
      Socket socket = serverSocket.accept();
      // Has to be a multiple of 3
      byte[] bytes = new byte[1024 * 3];
      int count = 0;
      try {
        BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
        synchronized(image) {
          for(Rectangle rectangle: area) {
            for(int x=0; x<rectangle.width; x++) {
              for(int y=0; y<rectangle.height; y++) {
                if(count == 0) {
                  in.read(bytes);
                }
                image.setRGB(rectangle.x + x, rectangle.y + y, 0xFF000000 | (0xFF & bytes[count]) << 16 | (0xFF & bytes[count + 1]) << 8 | (0xFF & bytes[count + 2]));
                count += 3;
                if(count == bytes.length) {
                  count = 0;
                }
              }
            }
          }
        }
        in.close();
        socket.close();
      } catch(Exception e) {
        e.printStackTrace();
      }
      serverSocket.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
    if(nativeComponentProxy != null) {
      nativeComponentProxy.stopCapture();
    }
  }
  
  @Override
  public Dimension getMinimumSize() {
    return new Dimension(0, 0);
  }
  
  /**
   * Create an image as a back buffer, which can be used for example to simulate alpha blending. Note that this is only possible for proxied components (cf component options). 
   */
  public void createBackBuffer() {
    if(nativeComponentProxy != null) {
      nativeComponentProxy.createBackgroundBuffer();
    }
  }
  
  /**
   * Update the back buffer on the areas that have non opaque overlays and that are not covered by opaque components.
   */
  public void updateBackBufferOnVisibleTranslucentAreas() {
    if(nativeComponentProxy != null) {
      nativeComponentProxy.updateBackBufferOnVisibleTranslucentAreas();
    }
  }
  
  /**
   * Releases the resources held by the back buffer.
   */
  public void releaseBackBuffer() {
    if(nativeComponentProxy != null) {
      nativeComponentProxy.releaseBackBuffer();
    }
  }
  
  protected EventListenerList listenerList = new EventListenerList();
  
  public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
    T[] result = listenerList.getListeners(listenerType);
    if(result.length == 0) { 
      return super.getListeners(listenerType); 
    }
    return result; 
  }
  
}
