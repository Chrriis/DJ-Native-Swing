/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import java.awt.AWTEvent;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.MenuComponent;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import chrriis.common.ObjectRegistry;
import chrriis.common.Utils;
import chrriis.dj.nativeswing.NativeComponentWrapper;
import chrriis.dj.nativeswing.swtimpl.ControlCommandMessage.DisposedControlException;

import com.sun.jna.Native;

/**
 * A native component that gets connected to a native peer.
 * @author Christopher Deckers
 */
public abstract class NativeComponent extends Canvas {

  private static final boolean IS_PRINTING_FAILED_MESSAGES = Boolean.parseBoolean(NSSystemPropertySWT.COMPONENTS_DEBUG_PRINTFAILEDMESSAGES.get());

  private NativeComponentWrapper nativeComponentWrapper = new NativeComponentWrapper(this) {

    @Override
    protected String getComponentDescription() {
      return NativeComponent.this.getComponentDescription();
    }

    @Override
    protected void paintNativeComponent(BufferedImage image, Rectangle[] rectangles) {
      NativeComponent.this.paintComponent(image, rectangles);
    }

    @Override
    protected void setNativeComponentEnabled(boolean isEnabled) {
      setShellEnabled(isEnabled);
    }

  };

  private class CMLocal_runInSequence extends LocalMessage {
    @Override
    public Object run(Object[] args) {
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
   * @return the result of running the message, or null if the message is queued.
   */
  public Object runSync(CommandMessage commandMessage, Object... args) {
    if(NativeInterface.isAlive()) {
      NativeInterface.checkUIThread(false);
    }
    if(commandMessage instanceof ControlCommandMessage) {
      ((ControlCommandMessage)commandMessage).setNativeComponent(this);
    }
    if(initializationCommandMessageList != null) {
      commandMessage.setSyncExec(true);
      commandMessage.setArgs(args);
      initializationCommandMessageList.add(commandMessage);
      return null;
    }
    if(!isNativePeerValid()) {
      commandMessage.setArgs(args);
      printFailedInvocation(commandMessage);
      return null;
    }
    try {
      return commandMessage.syncExec(true, args);
    } catch(RuntimeException e) {
      processFailedMessageException(e, commandMessage);
      return null;
    }
  }

  /**
   * Run the given command if the control is created, or store it to run it when the creation occurs.
   * If the component is disposed before the command has a chance to run, it is ignored silently.
   * @param commandMessage the command message to run.
   * @param args the arguments to pass to the command message.
   */
  public void runAsync(CommandMessage commandMessage, Object... args) {
    if(NativeInterface.isAlive()) {
      NativeInterface.checkUIThread(false);
    }
    if(commandMessage instanceof ControlCommandMessage) {
      ((ControlCommandMessage)commandMessage).setNativeComponent(this);
    }
    if(initializationCommandMessageList != null) {
      commandMessage.setSyncExec(false);
      commandMessage.setArgs(args);
      initializationCommandMessageList.add(commandMessage);
    } else if(!isNativePeerValid()) {
      commandMessage.setArgs(args);
      printFailedInvocation(commandMessage);
    } else {
      commandMessage.asyncExec(true, args);
    }
  }

  private void printFailedInvocation(Message message) {
    if(IS_PRINTING_FAILED_MESSAGES) {
      System.err.println("Failed message to " + getComponentDescription() + ": " + message);
    }
  }

  private static ObjectRegistry nativeComponentRegistry;
  private static ObjectRegistry controlRegistry;

  static {
    if(NativeInterface.isInProcess()) {
      nativeComponentRegistry = new ObjectRegistry();
      controlRegistry = new ObjectRegistry();
    } else {
      if(NativeInterface.OutProcess.isNativeSide()) {
        controlRegistry = new ObjectRegistry();
      } else {
        nativeComponentRegistry = new ObjectRegistry();
      }
    }
  }

  /**
   * Get the native components that are currently registered, which may have an invalid native peer.
   * @return The currently registered native components, which may have an invalid native peer.
   */
  public static NativeComponent[] getNativeComponents() {
    List<NativeComponent> nativeComponentList = new ArrayList<NativeComponent>();
    for(int instanceID: nativeComponentRegistry.getInstanceIDs()) {
      NativeComponent nativeComponent = (NativeComponent)nativeComponentRegistry.get(instanceID);
      if(nativeComponent != null) {
        nativeComponentList.add(nativeComponent);
      }
    }
    return nativeComponentList.toArray(new NativeComponent[0]);
  }

  /**
   * Get the registry of the components, which references created components using the component ID.
   * @return the registry.
   */
  protected static ObjectRegistry getNativeComponentRegistry() {
    return nativeComponentRegistry;
  }

  /**
   * Get the registry of the controls, which references created controls using the component ID.
   * @return the registry.
   */
  protected static ObjectRegistry getControlRegistry() {
    return controlRegistry;
  }

  private static class CMN_reshape extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      Shell shell = getControl().getShell();
      if(!shell.isDisposed()) {
        shell.setSize((Integer)args[0], (Integer)args[1]);
      }
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
    public Object run(Object[] args) {
      getControl().traverse(SWT.TRAVERSE_TAB_NEXT);
      return null;
    }
  }

  /**
   * Construct a native component.
   */
  public NativeComponent() {
    componentID = NativeComponent.getNativeComponentRegistry().add(this);
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        if(isNativePeerValid() && !isNativePeerDisposed()) {
          runSync(new CMN_transferFocus());
        }
      }
    });
    // We enable key events because of the special processing added to processKeyEvent(KeyEvent).
    enableEvents(AWTEvent.KEY_EVENT_MASK);
    setFocusable(true);
  }

  @SuppressWarnings("deprecation")
  @Override
  protected void processKeyEvent(KeyEvent e) {
    KeyEvent ke = e;
    if(!(ke instanceof CKeyEvent)) {
      // Under Linux, native key events are received in addition to our synthetic events.
      // This causes problems, because ancestor key strokes may accept a key combination which consumes the native event.
      // That means the native event is not received by the native component.
      // The solution is to dispatch the event directly to the peer if it is not our synthetic event (listeners do not get called).
      // Listeners are then called when our synthetic events are dispatched.
      getPeer().handleEvent(e);
      e.consume();
      return;
    }
    super.processKeyEvent(ke);
  }

  private volatile Thread resizeThread;

  @SuppressWarnings("deprecation")
  @Override
  public void reshape(int x, int y, int width, int height) {
    if(resizeThread == null && (width != getWidth() || height != getHeight())) {
      resizeThread = new Thread("NativeSwing Resize") {
        @Override
        public void run() {
          try {
            sleep(50);
          } catch(Exception e) {
          }
          applyPendingReshape();
        }
      };
      resizeThread.start();
    }
    super.reshape(x, y, width, height);
  }

  private void applyPendingReshape() {
    if(resizeThread == null) {
      return;
    }
    if(!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          applyPendingReshape();
        }
      });
      return;
    }
    if(resizeThread == null) {
      return;
    }
    resizeThread = null;
    if(isNativePeerValid()) {
      new CMN_reshape().asyncExec(NativeComponent.this, getWidth(), getHeight());
    }
  }

  private static class CMJ_dispatchMouseEvent extends ControlCommandMessage {
    private static int buttonPressedCount;
    @Override
    public Object run(Object[] args) {
      NativeComponent nativeComponent = getNativeComponent();
      if(nativeComponent == null || !nativeComponent.isShowing()) {
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
          if(buttonPressedCount < 0) {
            buttonPressedCount = 0;
          }
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
      MouseEvent me;
      if(Utils.IS_JAVA_6_OR_GREATER) {
        // Not specifying the absX and Y in Java 6 results in a deadlock when pressing alt+F4 while moving the mouse over a native control
        if(type == MouseEvent.MOUSE_WHEEL) {
          me = new MouseWheelEvent(nativeComponent, type, System.currentTimeMillis(), SWTUtils.translateSWTModifiers(e_stateMask), e_x, e_y, e_cursorLocation.x, e_cursorLocation.y, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, Math.abs(e_count), e_count < 0? 1: -1);
        } else {
          boolean isPopupTrigger = type == MouseEvent.MOUSE_RELEASED && button == MouseEvent.BUTTON3;
          me = new MouseEvent(nativeComponent, type, System.currentTimeMillis(), SWTUtils.translateSWTModifiers(e_stateMask), e_x, e_y, e_cursorLocation.x, e_cursorLocation.y, e_count, isPopupTrigger, button);
        }
      } else {
        if(type == MouseEvent.MOUSE_WHEEL) {
          me = new MouseWheelEvent(nativeComponent, type, System.currentTimeMillis(), SWTUtils.translateSWTModifiers(e_stateMask), e_x, e_y, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, Math.abs(e_count), e_count < 0? 1: -1);
        } else {
          boolean isPopupTrigger = type == MouseEvent.MOUSE_RELEASED && button == MouseEvent.BUTTON3;
          me = new MouseEvent(nativeComponent, type, System.currentTimeMillis(), SWTUtils.translateSWTModifiers(e_stateMask), e_x, e_y, e_count, isPopupTrigger, button);
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
    public Object run(Object[] args) {
      NativeComponent nativeComponent = getNativeComponent();
      if(nativeComponent == null || !nativeComponent.isShowing()) {
        return null;
      }
      int type = (Integer)args[0];
      int e_stateMask = (Integer)args[1];
      char e_character = (Character)args[2];
      int e_keyCode = (Integer)args[3];
      if(e_keyCode == SWT.TAB) {
        if(type == KeyEvent.KEY_PRESSED) {
          if((e_stateMask & SWT.CONTROL) != 0) {
            boolean isForward = (e_stateMask & SWT.SHIFT) == 0;
            nativeComponent.nativeComponentWrapper.transferFocus(!isForward);
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
      KeyEvent ke = new CKeyEvent(nativeComponent, type, System.currentTimeMillis(), SWTUtils.translateSWTModifiers(e_stateMask), keyCode, character);
      nativeComponent.dispatchEvent(ke);
      return null;
    }
  }

  private static class CKeyEvent extends KeyEvent {
    public CKeyEvent(Component source, int id, long when, int modifiers, int keyCode, char keyChar) {
      super(source, id, when, modifiers, keyCode, keyChar);
    }
    @Override
    public String toString() {
      String srcName = null;
      if (source instanceof Component) {
        srcName = ((Component)source).getName();
      } else if (source instanceof MenuComponent) {
        srcName = ((MenuComponent)source).getName();
      }
      return KeyEvent.class.getName() + "[" + paramString() + "] on " + (srcName != null? srcName : source);
    }
  }

  private static class CMN_createControl extends CommandMessage {
    public Shell createShell(Object handle) throws Exception {
      if(NativeInterface.isInProcess()) {
        Canvas canvas = (Canvas)handle;
        // SWT_AWT adds a component listener, but it conflicts. Thus we have to restore the listeners.
        ComponentListener[] componentListeners = canvas.getComponentListeners();
        Shell shell = SWT_AWT.new_Shell(NativeInterface.getDisplay(), canvas);
        for(ComponentListener componentListener: canvas.getComponentListeners()) {
          canvas.removeComponentListener(componentListener);
        }
        for(ComponentListener componentListener: componentListeners) {
          canvas.addComponentListener(componentListener);
        }
        return shell;
      }
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
        return (Shell)shellCreationMethod.invoke(null, NativeInterface.getDisplay(), ((Number)handle).intValue());
      }
      try {
        shellCreationMethod = Shell.class.getMethod(SWT.getPlatform() + "_new", Display.class, long.class);
      } catch(Exception e) {}
      if(shellCreationMethod != null) {
        return (Shell)shellCreationMethod.invoke(null, NativeInterface.getDisplay(), ((Number)handle).longValue());
      }
      Constructor<Shell> shellConstructor = null;
      try {
        shellConstructor = Shell.class.getConstructor(Display.class, Shell.class, int.class, int.class);
      } catch(Exception e) {}
      if(shellConstructor != null) {
        shellConstructor.setAccessible(true);
        return shellConstructor.newInstance(NativeInterface.getDisplay(), null, SWT.NO_TRIM, ((Number)handle).intValue());
      }
      try {
        shellConstructor = Shell.class.getConstructor(Display.class, Shell.class, int.class, long.class);
      } catch(Exception e) {}
      if(shellConstructor != null) {
        shellConstructor.setAccessible(true);
        return shellConstructor.newInstance(NativeInterface.getDisplay(), null, SWT.NO_TRIM, ((Number)handle).longValue());
      }
      throw new IllegalStateException("Failed to create a Shell!");
    }
    @Override
    public Object run(Object[] args) throws Exception {
      // We need to synchronize: a non-UI thread may send a message thinking the component is valid, but the message would be invalid as the control is not yet in the registry.
      synchronized(NativeComponent.getControlRegistry()) {
        final Shell shell = createShell(args[2]);
        shell.addControlListener(new ControlAdapter() {
          private boolean isAdjusting;
          @Override
          public void controlMoved(ControlEvent e) {
            if(isAdjusting) {
              return;
            }
            Shell shell = (Shell)e.widget;
            Point location = shell.getLocation();
            if(location.x != 0 || location.y != 0) {
              // On Linux Ubuntu, I found that the location cannot be forced and this would cause a stack overflow.
              isAdjusting = true;
              shell.setLocation(0, 0);
              isAdjusting = false;
            }
          }
        });
        shell.setLayout(new FillLayout());
        final int componentID = (Integer)args[0];
        Method createControlMethod = Class.forName((String)args[1]).getDeclaredMethod("createControl", Shell.class, Object[].class);
        createControlMethod.setAccessible(true);
        final Control control = (Control)createControlMethod.invoke(null, shell, args[3]);
        if(Boolean.parseBoolean(NSSystemPropertySWT.COMPONENTS_DEBUG_PRINTCREATION.get())) {
          System.err.println("Created control: " + componentID);
        }
        control.addDisposeListener(new DisposeListener() {
          public void widgetDisposed(DisposeEvent e) {
            if(Boolean.parseBoolean(NSSystemPropertySWT.COMPONENTS_DEBUG_PRINTDISPOSAL.get())) {
              System.err.println("Disposed control: " + componentID);
            }
          }
        });
        NativeComponent.getControlRegistry().add(control, componentID);
        configureControl(control, componentID);
        shell.setVisible (true);
        shell.getDisplay().asyncExec(new Runnable() {
          public void run() {
            if(!shell.isDisposed()) {
              shell.setLocation(0, 0);
            }
          }
        });
      }
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
    control.setData("NS_EnabledEventsMask", 0L);
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
        if((((Long)e.widget.getData("NS_EnabledEventsMask")) & MouseEvent.MOUSE_MOTION_EVENT_MASK) != 0) {
          Object[] mouseEventArgs = getMouseEventArgs(control, e, MouseEvent.MOUSE_MOVED);
          if(mouseEventArgs != null) {
            new CMJ_dispatchMouseEvent().asyncExec(control, mouseEventArgs);
          }
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

  private static class CMN_setEventsEnabled extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      long eventMask = (Long)args[0];
      boolean isEnabled = (Boolean)args[1];
      long events = (Long)getControl().getData("NS_EnabledEventsMask");
      if(isEnabled) {
        events |= eventMask;
      } else {
        events &= ~eventMask;
      }
      getControl().setData("NS_EnabledEventsMask", events);
      return null;
    }
  }

  @Override
  public synchronized void addMouseMotionListener(MouseMotionListener listener) {
    if(getMouseMotionListeners().length == 0 && listener != null) {
      runAsync(new CMN_setEventsEnabled(), MouseEvent.MOUSE_MOTION_EVENT_MASK, true);
    }
    super.addMouseMotionListener(listener);
  }

  @Override
  public synchronized void removeMouseMotionListener(MouseMotionListener listener) {
    super.removeMouseMotionListener(listener);
    if(getMouseMotionListeners().length == 0) {
      runAsync(new CMN_setEventsEnabled(), MouseEvent.MOUSE_MOTION_EVENT_MASK, false);
    }
  }

  /**
   * Paint the component, which also paints the back buffer if any.
   * @param g the graphics to paint to.
   */
  @Override
  public void paint(Graphics g) {
    super.paint(g);
    if(!isNativePeerValid()) {
      String text = invalidNativePeerText;
      if(text == null) {
        text = "Invalid " + getComponentDescription();
      }
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
    } else {
      nativeComponentWrapper.paintBackBuffer(g);
    }
  }

  /**
   * Print the component, which also prints the native peer.
   * @param g the graphics to paint to.
   */
  @Override
  public void print(Graphics g) {
    BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
    paintComponent(image);
    g.drawImage(image, 0, 0, null);
    g.dispose();
    image.flush();
  }

  private void throwDuplicateCreationException() {
    isNativePeerValid = false;
    invalidNativePeerText = "Failed to create " + getComponentDescription() + "\n\nReason:\nA native component cannot be removed then re-added to a component hierarchy.";
    repaint();
    throw new IllegalStateException("A native component cannot be removed then re-added to a component hierarchy! Nevertheless, you could achieve re-parenting by setting the constructor option to defer destruction until finalization (note that re-parenting accross different windows is not supported).");
  }

  private int additionCount;

  @Override
  public void addNotify() {
    super.addNotify();
    if(isForcingInitialization) {
      return;
    }
    if(isNativePeerDisposed) {
      throwDuplicateCreationException();
    }
    additionCount++;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if(isNativePeerDisposed) {
          if(additionCount > 1) {
            throwDuplicateCreationException();
          }
        }
        if(!isNativePeerInitialized) {
          createNativePeer();
        }
      }
    });
  }

  private boolean isForcingInitialization;

  /**
   * Force the component to initialize. All method calls will then be synchronous instead of being queued waiting for the componant to be initialized.
   * This call fails if the component is not in a component hierarchy with a Window ancestor.
   */
  public void initializeNativePeer() {
    if(NativeInterface.isAlive()) {
      NativeInterface.checkUIThread(false);
    }
    Window windowAncestor = SwingUtilities.getWindowAncestor(this);
    if(windowAncestor == null) {
      throw new IllegalStateException("This method can only be called when the component has a Window ancestor!");
    }
    if(isNativePeerDisposed) {
      throwDuplicateCreationException();
    }
    if(!isNativePeerInitialized) {
      isForcingInitialization = true;
      try {
        windowAncestor.addNotify();
        createNativePeer();
      } finally {
        isForcingInitialization = false;
      }
    }
  }

  private Method getAWTHandleMethod;
  private Object getHandle() {
    if(NativeInterface.isInProcess()) {
      return this;
    }
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
      try {
        return Native.getComponentID(this);
      } catch(Exception ex) {
        ex.printStackTrace();
      }
    }
    return 0;
  }

  private NativeInterfaceListener nativeInterfaceListener;

  private static class NNativeInterfaceListener extends NativeInterfaceAdapter {
    protected Reference<NativeComponent> nativeComponent;
    protected NNativeInterfaceListener(NativeComponent nativeComponent) {
      this.nativeComponent = new WeakReference<NativeComponent>(nativeComponent);
    }
    @Override
    public void nativeInterfaceClosed() {
      NativeInterface.removeNativeInterfaceListener(this);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          NativeComponent nativeComponent = NNativeInterfaceListener.this.nativeComponent.get();
          if(nativeComponent == null) {
            return;
          }
          nativeComponent.invalidateNativePeer("The native peer died unexpectedly.");
          nativeComponent.repaint();
        }
      });
    }
  }

  /**
   * Get the parameters that are passed to the creation method. This method can be overriden by subclasses to pass additional information necessary for the native peer creation.
   * @return the parameters.
   */
  protected Object[] getNativePeerCreationParameters() {
    return null;
  }

  private void createNativePeer() {
    boolean isInterfaceAlive = NativeInterface.isAlive();
    if(isInterfaceAlive) {
      NativeInterface.checkUIThread(false);
    }
    if(initializationCommandMessageList == null) {
      throwDuplicateCreationException();
    }
    List<CommandMessage> initializationCommandMessageList_ = initializationCommandMessageList;
    initializationCommandMessageList = null;
    isNativePeerInitialized = true;
    if(isInterfaceAlive) {
      nativeInterfaceListener = new NNativeInterfaceListener(this);
      NativeInterface.addNativeInterfaceListener(nativeInterfaceListener);
      isNativePeerValid = true;
      try {
        runSync(new CMN_createControl(), componentID, NativeComponent.this.getClass().getName(), getHandle(), getNativePeerCreationParameters());
      } catch(Exception e) {
        isNativePeerValid = false;
        StringBuilder sb = new StringBuilder();
        for(Throwable t = e; t != null; t = t.getCause()) {
          sb.append("    " + t.toString() + "\n");
        }
        invalidNativePeerText = "Failed to create " + getComponentDescription() + "\n\nReason:\n" + sb.toString();
        e.printStackTrace();
      }
      new CMN_reshape().asyncExec(this, getWidth(), getHeight());
    } else {
      invalidNativePeerText = "Failed to create " + getComponentDescription() + "\n\nReason:\nThe native interface is not open!";
    }
    for(CommandMessage initCommandMessage: initializationCommandMessageList_) {
      if(!isNativePeerValid()) {
        printFailedInvocation(initCommandMessage);
      } else {
        // We have to restore the sync state, otherwise 2 sync calls (like navigation calls) would be sent
        // before processing messages generated by each of these calls (like location changing events).
        if(initCommandMessage.isSyncExec()) {
          try {
            initCommandMessage.syncSend(true);
          } catch(RuntimeException e) {
            processFailedMessageException(e, initCommandMessage);
          }
        } else {
          initCommandMessage.asyncSend(true);
        }
      }
    }
  }

  /**
   * Print a message or rethrows the exception.
   */
  private void processFailedMessageException(RuntimeException e, CommandMessage commandMessage) {
    boolean isCatchingException = false;
    for(Throwable ex=e; ex != null; ex = ex.getCause()) {
      // Disposed control exceptions should always be caught.
      if(ex instanceof DisposedControlException) {
        isCatchingException = true;
        break;
      }
    }
    if(!isCatchingException && Boolean.parseBoolean(NSSystemPropertySWT.COMPONENTS_SWALLOWRUNTIMEEXCEPTIONS.get())) {
      // When the exception is not because of a disposal, we print the exception to the console.
      e.printStackTrace();
      isCatchingException = true;
    }
    if(isCatchingException) {
      printFailedInvocation(commandMessage);
    } else {
      throw e;
    }
  }

  private static class CMN_destroyControl extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      Control control = getControl();
      NativeComponent.getControlRegistry().remove(getComponentID());
      if(control != null) {
        if(!control.isDisposed()) {
          Shell shell = control.getShell();
          if(shell != null) {
            shell.dispose();
          }
        }
        control.dispose();
      }
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
        NativeInterface.removeNativeInterfaceListener(nativeInterfaceListener);
        if(isNativePeerValid()) {
          runSync(new CMN_destroyControl());
        }
      }
      invalidateNativePeer("The native component was disposed.");
      NativeComponent.getNativeComponentRegistry().remove(componentID);
      nativeComponentWrapper.disposeNativeComponent();
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
   * Indicate whether the native peer initialization phase has happened. This method returns true even if the native peer is disposed or if the creation of the peer failed.
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

  private void invalidateNativePeer(String invalidNativePeerText) {
    if(isNativePeerValid) {
      isNativePeerValid = false;
      this.invalidNativePeerText = "Invalid " + getComponentDescription() + "\n\nReason:\n" + invalidNativePeerText;
      repaint();
    }
  }

  private String getComponentDescription() {
    return getClass().getName() + "[" + getComponentID() + "," + hashCode() + "]";
  }

  @Override
  public String toString() {
    return getComponentDescription();
  }

  /**
   * A native component instance cannot be added directly to a component hierarchy. This method needs to be called to get a component that will add the native component.
   * @param optionMap the options to configure the behavior of this component.
   * @return the component that contains the native component and that can be added to the component hierarchy.
   */
  protected Component createEmbeddableComponent(Map<Object, Object> optionMap) {
    return nativeComponentWrapper.createEmbeddableComponent(optionMap);
  }

  private static class CMN_setShellEnabled extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      if(getControl().isDisposed()) {
        return null;
      }
      getControl().getShell().setEnabled((Boolean)args[0]);
      return null;
    }
  }

  private boolean isShellEnabled = true;

  private void setShellEnabled(boolean isEnabled) {
    if(isEnabled == isShellEnabled) {
      return;
    }
    isShellEnabled = isEnabled;
    // We do not want to send this message on a disposed or dead component
    if(!isNativePeerInitialized() || isNativePeerValid()) {
      runAsync(new CMN_setShellEnabled(), isEnabled);
    }
  }

  private static class CMN_setEnabled extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
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
    public Object run(Object[] args) {
      Control control = getControl();
      return control.isDisposed()? false: control.isFocusControl();
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
    public Object run(Object[] args) {
      Control control = getControl();
      if(control.isDisposed()) {
        return null;
      }
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

    private static boolean printRemoveClip(Control control, GC gc) {
      org.eclipse.swt.graphics.Rectangle bounds = control.getBounds();
      Display display = control.getDisplay();
      Composite oldParent = control.getParent();
      Shell tmpHiddenParentShell = new Shell();
      Shell tmpParentShell = new Shell(tmpHiddenParentShell, SWT.NO_TRIM | SWT.NO_FOCUS | SWT.NO_BACKGROUND);
      Point location = display.map(control, null, control.getLocation());
      tmpParentShell.setLocation(location);
      tmpParentShell.setSize(bounds.width, bounds.height);
      org.eclipse.swt.widgets.Canvas screenshotCanvas = new org.eclipse.swt.widgets.Canvas(tmpParentShell, SWT.NO_BACKGROUND);
      screenshotCanvas.setSize(bounds.width, bounds.height);
      GC displayGC = new GC(display);
      final Image screenshot = new Image(display, bounds.width, bounds.height);
      displayGC.copyArea(screenshot, location.x, location.y);
      displayGC.dispose();
      PaintListener paintListener = new PaintListener() {
        public void paintControl(PaintEvent e) {
          e.gc.drawImage(screenshot, 0, 0);
        }
      };
      tmpParentShell.addPaintListener(paintListener);
      screenshotCanvas.addPaintListener(paintListener);
      oldParent.addPaintListener(paintListener);
      org.eclipse.swt.widgets.Canvas controlReplacementCanvas = new org.eclipse.swt.widgets.Canvas(oldParent, SWT.NO_BACKGROUND);
      controlReplacementCanvas.setSize(bounds.width, bounds.height);
      controlReplacementCanvas.addPaintListener(paintListener);
      control.setRedraw(false);
      oldParent.setRedraw(false);
      control.setParent(tmpParentShell);
      control.setLocation(0, 0);
      control.moveBelow(screenshotCanvas);
      tmpParentShell.setVisible(true);
      boolean result = control.print(gc);
      control.setParent(oldParent);
      control.setLocation(bounds.x, bounds.y);
      control.moveAbove(controlReplacementCanvas);
      controlReplacementCanvas.dispose();
      oldParent.removePaintListener(paintListener);
      tmpParentShell.dispose();
      tmpHiddenParentShell.dispose();
      oldParent.setRedraw(true);
      control.setRedraw(true);
      control.redraw();
      control.update();
      screenshot.dispose();
      return result;
    }

    private ImageData getImageData(Control control, Region region) {
      if(control.isDisposed()) {
        return null;
      }
      Point size = control.getSize();
      if(size.x <= 0 || size.y <= 0) {
        return null;
      }
      org.eclipse.swt.graphics.Rectangle regionBounds = region.getBounds();
      Display display = control.getDisplay();
      final Image image = new Image(display, regionBounds.x + regionBounds.width, regionBounds.y + regionBounds.height);
      GC gc = new GC(image);
      gc.setClipping(region);
      if(Boolean.parseBoolean(System.getProperty("nativeswing.components.printingHack"))) {
        // 1. https://bugs.eclipse.org/bugs/show_bug.cgi?id=223590
        // 2. https://bugs.eclipse.org/bugs/show_bug.cgi?id=299714
        // Note 1: bug 1 is marked as fixed, but preliminary testing shows some other bugs. Have to test more before removing this hack.
        // Note 2: 3.6M3 seems to fix this bug, so I comment the implementation.
        // Note 3: some issues on win 7 make me add a property to turn the old hack back on in case of future unexpected user issues.
        printRemoveClip(control, gc);
      } else if(Utils.IS_WINDOWS) {
        org.eclipse.swt.graphics.Rectangle bounds = control.getBounds();
        control.print(gc);
        // If the window is moving while the component is printed, it is reparented at the wrong location: we have to restore the right location.
        control.setLocation(bounds.x, bounds.y);
        // There can be painting artifacts when dragging another window slowly on top: refresh the component.
        control.redraw(0, 0, bounds.width, bounds.height, true);
        control.update();
      } else {
        control.print(gc);
      }
      gc.dispose();
      ImageData imageData = image.getImageData();
      image.dispose();
      return imageData;
    }

    @Override
    public Object run(Object[] args) throws Exception {
      int port = (Integer)args[0];
      Rectangle[] rectangles = (Rectangle[])args[1];
      String hostAddress = (String)args[2];
      final Control control = getControl();
      ImageData imageData;
      final Region region = new Region();
      for(Rectangle rectangle: rectangles) {
        region.add(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
      }
      if(!NativeInterface.isUIThread(true)) {
        final AtomicReference<Exception> exception = new AtomicReference<Exception>();
        final AtomicReference<ImageData> result = new AtomicReference<ImageData>();
        control.getDisplay().syncExec(new Runnable() {
          public void run() {
            try {
              result.set(getImageData(control, region));
            } catch (Exception e) {
              exception.set(e);
            }
          }
        });
        if(exception.get() != null) {
          new Socket(hostAddress, port).close();
          throw exception.get();
        }
        imageData = result.get();
      } else {
        imageData = getImageData(control, region);
      }
      region.dispose();
      if(imageData == null) {
        new Socket(hostAddress, port).close();
        return null;
      }
      int cursor = 0;
      // Has to be a multiple of 3
      byte[] bytes = new byte[1024 * 3];
      PaletteData palette = imageData.palette;
      if (palette.isDirect) {
        Socket socket = new Socket(hostAddress, port);
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        int width = imageData.width;
        int height = imageData.height;
        try {
          for(Rectangle rectangle: rectangles) {
            for(int j=0; j<rectangle.height; j++) {
              int y = rectangle.y + j;
              for(int i=0; i<rectangle.width; i++) {
                int x = rectangle.x + i;
                if(x < width && y < height) {
                  int pixel = imageData.getPixel(x, y);
                  // We cannot use palette.getRGB() because all the creations of RGB objects make it too slow.
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
          out.flush();
        } catch(Exception e) {
          e.printStackTrace();
        }
        out.close();
        socket.close();
        return null;
      }
      throw new IllegalStateException("Not implemented");
    }

  }

  /**
   * Paint the native component including its native peer in an image. This method can be called from a non-UI thread.
   * @param image the image to paint to.
   */
  public void paintComponent(BufferedImage image) {
    paintComponent(image, null);
  }

  /**
   * Paint the native component including its native peer in an image, in the areas that are specified. This method can be called from a non-UI thread.
   * @param image the image to paint to.
   * @param rectangles the area in which the component should be painted.
   */
  public void paintComponent(BufferedImage image, Rectangle[] rectangles) {
    if(image == null || !isNativePeerValid() || isNativePeerDisposed) {
      return;
    }
    applyPendingReshape();
    int width = Math.min(getWidth(), image.getWidth());
    int height = Math.min(getHeight(), image.getHeight());
    if(width <= 0 || height <= 0) {
      return;
    }
    if(rectangles == null) {
      rectangles = new Rectangle[] {new Rectangle(width, height)};
    }
    Rectangle bounds = new Rectangle(width, height);
    List<Rectangle> rectangleList = new ArrayList<Rectangle>();
    for(Rectangle rectangle: rectangles) {
      if(rectangle.intersects(bounds)) {
        rectangleList.add(rectangle.intersection(bounds));
      }
    }
    if(rectangleList.isEmpty()) {
      return;
    }
    rectangles = rectangleList.toArray(new Rectangle[0]);
    try {
      final ServerSocket serverSocket = new ServerSocket();
      String localHostAddress = Utils.getLocalHostAddress();
      if(localHostAddress == null) {
        localHostAddress = "127.0.0.1";
      }
      serverSocket.bind(new InetSocketAddress(InetAddress.getByName(localHostAddress), 0));
      NativeInterfaceListener nativeInterfaceListener = new NativeInterfaceAdapter() {
        @Override
        public void nativeInterfaceClosed() {
          NativeInterface.removeNativeInterfaceListener(this);
          try {
            serverSocket.close();
          } catch(Exception e) {
          }
        }
      };
      CMN_getComponentImage getComponentImage = new CMN_getComponentImage();
      NativeInterface.addNativeInterfaceListener(nativeInterfaceListener);
      getComponentImage.asyncExec(this, serverSocket.getLocalPort(), rectangles, localHostAddress);
      Socket socket = serverSocket.accept();
      // Has to be a multiple of 3
      byte[] bytes = new byte[1024 * 3];
      int count = 0;
      int readCount = 0;
      try {
        BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
        synchronized(image) {
          for(Rectangle rectangle: rectangles) {
            int[] pixels = new int[rectangle.width];
            for(int y=0; y<rectangle.height && readCount != -1; y++) {
              for(int x=0; x<rectangle.width && readCount != -1; x++) {
                if(readCount == 0) {
                  readCount = in.read(bytes);
                  if(readCount != -1 && (readCount % 3) != 0) {
                    int c = in.read(bytes, readCount, bytes.length - readCount);
                    if(c == -1) {
                      readCount = -1;
                    } else {
                      readCount += c;
                    }
                  }
                }
                if(readCount == -1) {
                  break;
                }
                pixels[x] = 0xFF000000 | (0xFF & bytes[count]) << 16 | (0xFF & bytes[count + 1]) << 8 | (0xFF & bytes[count + 2]);
                count += 3;
                if(count == readCount) {
                  count = 0;
                  readCount = 0;
                }
              }
              if(readCount != -1) {
                image.setRGB(rectangle.x, rectangle.y + y, rectangle.width, 1, pixels, 0, rectangle.width);
              }
            }
            if(readCount == -1) {
              break;
            }
          }
        }
        NativeInterface.removeNativeInterfaceListener(nativeInterfaceListener);
        in.close();
        socket.close();
      } catch(Exception e) {
        e.printStackTrace();
      }
      serverSocket.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public Dimension getMinimumSize() {
    return new Dimension(0, 0);
  }

  /**
   * Create an image of the native peer as a back buffer, which can be used when painting the component, or to simulate alpha blending.
   */
  public void createBackBuffer() {
    nativeComponentWrapper.createBackBuffer();
  }

  /**
   * Update the back buffer on the areas that have non opaque overlays and that are not covered by opaque components.
   */
  public void updateBackBufferOnVisibleTranslucentAreas() {
    nativeComponentWrapper.updateBackBufferOnVisibleTranslucentAreas();
  }

  /**
   * Update (eventually creating an empty one if it does not exist) the back buffer on the area specified by the rectangles.
   * @param rectangles the area to update.
   */
  public void updateBackBuffer(Rectangle[] rectangles) {
    nativeComponentWrapper.updateBackBuffer(rectangles);
  }

  /**
   * Destroy the back buffer.
   */
  public void destroyBackBuffer() {
    nativeComponentWrapper.destroyBackBuffer();
  }

  protected EventListenerList listenerList = new EventListenerList();

  @Override
  public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
    T[] result = listenerList.getListeners(listenerType);
    if(result.length == 0) {
      return super.getListeners(listenerType);
    }
    return result;
  }

}
