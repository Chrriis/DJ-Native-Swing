/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.core;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import chrriis.common.NetworkURLClassLoader;
import chrriis.common.SystemProperty;
import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.dj.nativeswing.NSSystemProperty;
import chrriis.dj.nativeswing.NativeSwing;
import chrriis.dj.nativeswing.swtimpl.ApplicationMessageHandler;
import chrriis.dj.nativeswing.swtimpl.CommandMessage;
import chrriis.dj.nativeswing.swtimpl.LocalMessage;
import chrriis.dj.nativeswing.swtimpl.Message;
import chrriis.dj.nativeswing.swtimpl.NSSystemPropertySWT;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.NativeInterfaceConfiguration;
import chrriis.dj.nativeswing.swtimpl.NativeInterfaceListener;
import chrriis.dj.nativeswing.swtimpl.PeerVMProcessFactory;
import chrriis.dj.nativeswing.swtimpl.core.InProcessMessagingInterface.SWTInProcessMessagingInterface;
import chrriis.dj.nativeswing.swtimpl.core.InProcessMessagingInterface.SwingInProcessMessagingInterface;
import chrriis.dj.nativeswing.swtimpl.core.OutProcessIOMessagingInterface.SWTOutProcessIOMessagingInterface;
import chrriis.dj.nativeswing.swtimpl.core.OutProcessIOMessagingInterface.SwingOutProcessIOMessagingInterface;
import chrriis.dj.nativeswing.swtimpl.core.OutProcessSocketsMessagingInterface.SWTOutProcessSocketsMessagingInterface;
import chrriis.dj.nativeswing.swtimpl.core.OutProcessSocketsMessagingInterface.SwingOutProcessSocketsMessagingInterface;
import chrriis.dj.nativeswing.swtimpl.internal.ISWTNativeInterface;

/**
 * The native interface, which establishes the link between the peer VM (native side) and the local side.
 * @author Christopher Deckers
 */
public class SWTNativeInterface extends NativeInterface implements ISWTNativeInterface {

  private static final boolean IS_SYNCING_MESSAGES = Boolean.parseBoolean(NSSystemPropertySWT.INTERFACE_SYNCMESSAGES.get());

  public boolean isAlive() {
    synchronized(OPEN_STATE_LOCK) {
      return isOpen() && messagingInterface.isAlive();
    }
  }

  private static boolean isOpen;

  /**
   * Indicate whether the native interface is open.
   * @return true if the native interface is open, false otherwise.
   */
  public boolean isOpen_() {
    synchronized(OPEN_STATE_LOCK) {
      return isOpen;
    }
  }

  private void checkOpen() {
    if(!isOpen()) {
      throw new IllegalStateException("The native interface is not open! Please refer to the instructions to set it up properly.");
    }
  }

  /**
   * Close the native interface, which destroys the native side (peer VM). Note that the native interface can be re-opened later.
   */
  public void close_() {
    synchronized(OPEN_CLOSE_SYNC_LOCK) {
      if(!isOpen()) {
        return;
      }
      synchronized(OPEN_STATE_LOCK) {
        isOpen = false;
        messagingInterface.destroy();
        messagingInterface = null;
      }
      for(NativeInterfaceListener listener: getNativeInterfaceListeners()) {
        listener.nativeInterfaceClosed();
      }
    }
  }

  private static volatile NativeInterfaceConfiguration nativeInterfaceConfiguration;

  /**
   * Get the configuration, which allows to modify some parameters.
   */
  public NativeInterfaceConfiguration getConfiguration_() {
    if(nativeInterfaceConfiguration == null) {
      nativeInterfaceConfiguration = createConfiguration();
    }
    return nativeInterfaceConfiguration;
  }

  private void loadClipboardDebuggingProperties() {
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

  private volatile boolean isInitialized;

  /**
   * Indicate whether the native interface is initialized.
   * @return true if the native interface is initialized, false otherwise.
   */
  public boolean isInitialized_() {
    return isInitialized;
  }

  private boolean isInProcess;

  public boolean isInProcess_() {
    synchronized(OPEN_STATE_LOCK) {
      return isInProcess;
    }
  }

  private static class CMN_printStackTraces extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      boolean isToConsole = (Boolean)args[0];
      if(isToConsole) {
        Utils.printStackTraces();
        return null;
      }
      StringWriter sw = new StringWriter();
      Utils.printStackTraces(new PrintWriter(sw));
      return sw.toString();
    }
  }

  /**
   * Initialize the native interface, but do not open it. This method sets some properties and registers a few listeners to keep track of certain states necessary for the good functioning of the framework.<br/>
   * This method is automatically called if open() is used. It should be called early in the program, the best place being as the first call in the main method.
   */
  public void initialize_() {
    synchronized(OPEN_CLOSE_SYNC_LOCK) {
      if(isInitialized()) {
        return;
      }
      if(Boolean.parseBoolean(NSSystemPropertySWT.DEPENDENCIES_CHECKVERSIONS.get("true"))) {
        // Check the versions of the libraries.
        if(SWT.getVersion() < 4332) {
          throw new IllegalStateException("The version of SWT that is required is 4.3 or later!");
        }
      }
      if(nativeInterfaceConfiguration == null) {
        nativeInterfaceConfiguration = createConfiguration();
      }
      if(Utils.IS_MAC && !"applet".equals(NSSystemProperty.DEPLOYMENT_TYPE.get())) {
        // initialize() needs to be called in main, and AWT must not have any static initializer to have run before SWT.
        // We can detect that the call does not originate from an AWT Component subclass.
        mainLoop: for(StackTraceElement ste: Thread.currentThread().getStackTrace()) {
          try {
            Class<?> steClass = Class.forName(ste.getClassName());
            for(Class<?> clazz = steClass; clazz != null; clazz = clazz.getSuperclass()) {
              if(clazz.getName().equals("java.awt.Component")) {
                System.err.println("On Mac, \"NativeInterface.initialize()\"/\"NativeInterface.open()\" should not be called after AWT static initializers have run, otherwise there can be all sorts of side effects (non-functional modal dialogs, etc.). Generally, the problem is when the \"main(String[])\" method is located inside an AWT component subclass and the fix is to move that main method to a standalone class. The problematic class here is \"" + steClass.getName() + "\"");
                break mainLoop;
              }
            }
          } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
          }
        }
      }
      String inProcessProperty = NSSystemPropertySWT.INTERFACE_INPROCESS.get();
      if(inProcessProperty != null) {
        isInProcess = Boolean.parseBoolean(inProcessProperty);
      } else {
        isInProcess = Utils.IS_MAC;
      }
      try {
        for(NativeInterfaceListener listener: getNativeInterfaceListeners()) {
          listener.nativeInterfaceInitialized();
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
      if(isInProcess_()) {
        InProcess.initialize();
      } else {
        OutProcess.initialize();
      }
      Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
        public void eventDispatched(AWTEvent e) {
          KeyEvent ke = (KeyEvent)e;
          if(ke.getID() == KeyEvent.KEY_PRESSED && ke.getKeyCode() == KeyEvent.VK_F3 && ke.isControlDown() && ke.isAltDown() && ke.isShiftDown()) {
            printStackTraces();
          }
        }
      }, AWTEvent.KEY_EVENT_MASK);
      isInitialized = true;
    }
  }

  /**
   * Print the stack traces to system err, including the ones from the peer VM when applicable.
   */
  public void printStackTraces_() {
    Utils.printStackTraces();
    printPeerStackTrace(System.err);
  }

  /**
   * Print the stack traces to a print stream, including the ones from the peer VM when applicable.
   */
  public void printStackTraces_(PrintStream printStream) {
    Utils.printStackTraces(printStream);
    printPeerStackTrace(printStream);
  }

  /**
   * Print the stack traces to a print writer, including the ones from the peer VM when applicable.
   */
  public void printStackTraces_(PrintWriter printWriter) {
    Utils.printStackTraces(printWriter);
    printPeerStackTrace(printWriter);
  }

  private void printPeerStackTrace(final Object o) {
    if(!isInProcess_() && isOpen()) {
      if(isUIThread(false)) {
        Thread t = new Thread("NativeSwing stack traces dump") {
          @Override
          public void run() {
            printPeerStackTrace(o);
          }
        };
        t.start();
        try {
          t.join();
        } catch (InterruptedException e) {}
      } else {
        boolean isToConsole = o == null;
        CMN_printStackTraces message = new CMN_printStackTraces();
        setMessageArgs(message, isToConsole);
        String s = (String)syncSend_(true, message);
        if(!isToConsole) {
          String descriptor = "---- NativeSwing[" + getMessagingInterface(false).getPID() + "] Peer VM Stack Traces ----" + Utils.LINE_SEPARATOR;
          if(o instanceof PrintStream) {
            PrintStream ps = (PrintStream)o;
            ps.append(descriptor);
            ps.append(s);
          } else if(o instanceof PrintWriter) {
            PrintWriter pw = (PrintWriter)o;
            pw.append(descriptor);
            pw.append(s);
          }
        }
      }
    }
  }

  private static final Object OPEN_CLOSE_SYNC_LOCK = new Object();
  private static final Object OPEN_STATE_LOCK = new Object();

  /**
   * Open the native interface, which creates the peer VM that handles the native side of the native integration.<br/>
   * Initialization takes place if the interface was not already initialized. If initialization was not explicitely performed, this method should be called early in the program, the best place being as the first call in the main method.
   */
  public void open_() {
    synchronized(OPEN_CLOSE_SYNC_LOCK) {
      if(isOpen()) {
        return;
      }
      initialize();
      loadClipboardDebuggingProperties();
      if(isInProcess_()) {
        InProcess.createInProcessCommunicationChannel();
      } else {
        OutProcess.createOutProcessCommunicationChannel();
      }
      try {
        for(NativeInterfaceListener listener: getNativeInterfaceListeners()) {
          listener.nativeInterfaceOpened();
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }

  public boolean notifyKilled() {
    synchronized(OPEN_STATE_LOCK) {
      isOpen = false;
      messagingInterface = null;
    }
    try {
      for(NativeInterfaceListener listener: getNativeInterfaceListeners()) {
        listener.nativeInterfaceClosed();
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
    if(!SWTNativeInterface.OutProcess.isNativeSide() && nativeInterfaceConfiguration.isNativeSideRespawnedOnError()) {
      OutProcess.createOutProcessCommunicationChannel();
      return true;
    }
    return false;
  }

  public void notifyRespawned() {
    try {
      for(NativeInterfaceListener listener: getNativeInterfaceListeners()) {
        listener.nativeInterfaceOpened();
      }
//      for(NativeInterfaceListener listener: getNativeInterfaceListeners()) {
//        listener.nativeInterfaceRestarted();
//      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public Object syncSend_(boolean isTargetNativeSide, final Message message) {
    checkOpen();
    if(message instanceof LocalMessage) {
      LocalMessage localMessage = (LocalMessage)message;
      return runMessageCommand(localMessage);
    }
    return getMessagingInterface(!isTargetNativeSide).syncSend(message);
  }

  public void asyncSend_(boolean isTargetNativeSide, final Message message) {
    if(IS_SYNCING_MESSAGES) {
      syncSend_(isTargetNativeSide, message);
    } else {
      checkOpen();
      if(message instanceof LocalMessage) {
        LocalMessage localMessage = (LocalMessage)message;
        runMessageCommand(localMessage);
        return;
      }
      getMessagingInterface(!isTargetNativeSide).asyncSend(message);
    }
  }

  private static MessagingInterface messagingInterface;

  MessagingInterface getMessagingInterface(boolean isNativeSide) {
    synchronized(OPEN_STATE_LOCK) {
      if(isInProcess_()) {
        if(isNativeSide) {
          SWTInProcessMessagingInterface swtInProcessMessagingInterface = (SWTInProcessMessagingInterface)((SwingInProcessMessagingInterface)messagingInterface).getMirrorMessagingInterface();
          return swtInProcessMessagingInterface;
        }
        SwingInProcessMessagingInterface swingInProcessMessagingInterface = (SwingInProcessMessagingInterface)messagingInterface;
        return swingInProcessMessagingInterface;
      }
      if(isNativeSide) {
        if(messagingInterface instanceof SWTOutProcessSocketsMessagingInterface) {
          SWTOutProcessSocketsMessagingInterface swtOutProcessSocketsMessagingInterface = (SWTOutProcessSocketsMessagingInterface)messagingInterface;
          return swtOutProcessSocketsMessagingInterface;
        }
        SWTOutProcessIOMessagingInterface swtOutProcessIOMessagingInterface = (SWTOutProcessIOMessagingInterface)messagingInterface;
        return swtOutProcessIOMessagingInterface;
      }
      if(messagingInterface instanceof SwingOutProcessSocketsMessagingInterface) {
        SwingOutProcessSocketsMessagingInterface swingOutProcessSocketsMessagingInterface = (SwingOutProcessSocketsMessagingInterface)messagingInterface;
        return swingOutProcessSocketsMessagingInterface;
      }
      SwingOutProcessIOMessagingInterface swingOutProcessIOMessagingInterface = (SwingOutProcessIOMessagingInterface)messagingInterface;
      return swingOutProcessIOMessagingInterface;
    }
  }

  private static volatile Display display;

  /**
   * Get the SWT display. This is only possible when in the native context.
   * @return the display, or null.
   */
  public Display getDisplay() {
    return display;
  }

  public boolean isOutProcessNativeSide_() {
    return OutProcess.isNativeSide();
  }

  /**
   * Indicate if the current thread is the user interface thread.
   * @return true if the current thread is the user interface thread.
   * @throws IllegalStateException when the native interface is not alive.
   */
  public boolean isUIThread_(boolean isNativeSide) {
    if(!isAlive()) {
      throw new IllegalStateException("The native interface is not alive!");
    }
    return getMessagingInterface(isNativeSide).isUIThread();
  }

  public int getInterfaceID(boolean isNativeSide) {
    if(!isAlive()) {
      throw new IllegalStateException("The native interface is not alive!");
    }
    return getMessagingInterface(isNativeSide).getPID();
  }

  public void checkUIThread(boolean isNativeSide) {
    if(!isAlive()) {
      throw new IllegalStateException("The native interface is not alive!");
    }
    getMessagingInterface(isNativeSide).checkUIThread();
  }

  private static volatile boolean isEventPumpRunning;

  public boolean isEventPumpRunning_() {
    return isEventPumpRunning;
  }

  /**
   * Run the native event pump. Certain platforms require this method call at the end of the main method to function properly, so it is suggested to always add it.
   */
  public void runEventPump_() {
    if(!isInitialized()) {
      throw new IllegalStateException("Cannot run the event pump when the interface is not initialized!");
    }
    if(isEventPumpRunning) {
      throw new IllegalStateException("runEventPump was already called and can only be called once (the first call should be at the end of the main method)!");
    }
    isEventPumpRunning = true;
    startAutoShutdownThread();
    if(isInProcess_()) {
      InProcess.runEventPump();
    } else {
      OutProcess.runEventPump();
    }
  }

  private void startAutoShutdownThread() {
    final Thread displayThread = display == null? null: display.getThread();
    final Thread currentThread = Thread.currentThread();
    Thread autoShutdownThread = new Thread("NativeSwing Auto-Shutdown") {
      protected Thread[] activeThreads = new Thread[1024];
      @Override
      public void run() {
        boolean isAlive = true;
        while(isAlive) {
          try {
            Thread.sleep(1000);
          } catch(Exception e) {
          }
          ThreadGroup group = Thread.currentThread().getThreadGroup();
          for(ThreadGroup parentGroup = group; (parentGroup = parentGroup.getParent()) != null; group = parentGroup) {
          }
          isAlive = false;
          for(int i=group.enumerate(activeThreads, true)-1; i>=0; i--) {
            Thread t = activeThreads[i];
            if(t != displayThread && t != currentThread && !t.isDaemon() && t.isAlive()) {
              isAlive = true;
              break;
            }
          }
        }
        // Shutdown procedure
        if(display == null) {
          // out-process: no display
          isEventPumpRunning = false;
        } else if(!display.isDisposed()) {
          display.asyncExec(new Runnable() {
            public void run() {
              isEventPumpRunning = false;
            }
          });
        }
      }
    };
    autoShutdownThread.setDaemon(true);
    autoShutdownThread.start();
  }

  private EventListenerList listenerList = new EventListenerList();

  /**
   * Add a native interface listener.
   * @param listener the native listener to add.
   */
  public void addNativeInterfaceListener_(NativeInterfaceListener listener) {
    listenerList.add(NativeInterfaceListener.class, listener);
  }

  /**
   * Remove a native interface listener.
   * @param listener the native listener to remove.
   */
  public void removeNativeInterfaceListener_(NativeInterfaceListener listener) {
    listenerList.remove(NativeInterfaceListener.class, listener);
  }

  /**
   * Get all the native interface listeners.
   * @return the native interface listeners.
   */
  public NativeInterfaceListener[] getNativeInterfaceListeners_() {
    return listenerList.getListeners(NativeInterfaceListener.class);
  }
  
  private ApplicationMessageHandler applicationMessageHandler;
  
  public void setApplicationMessageHandler_(ApplicationMessageHandler applicationMessageHandler) {
    this.applicationMessageHandler = applicationMessageHandler;
  }
  
  public ApplicationMessageHandler getApplicationMessageHandler() {
    return applicationMessageHandler;
  }

  private static void handleQuit() {
    if(!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          handleQuit();
        }
      });
      return;
    }
    ApplicationMessageHandler applicationMessageHandler = getInstance().getApplicationMessageHandler();
    if(applicationMessageHandler == null) {
      System.exit(-1324);
    } else {
      applicationMessageHandler.handleQuit();
    }
  }

  static class InProcess {

    private static volatile int pid;

    static void createInProcessCommunicationChannel() {
      synchronized(OPEN_STATE_LOCK) {
        messagingInterface = createInProcessMessagingInterface();
        isOpen = true;
      }
    }

    private static void initialize() {
      Device.DEBUG = Boolean.parseBoolean(NSSystemPropertySWT.SWT_DEVICE_DEBUG.get());
      if(Utils.IS_MAC && "applet".equals(NSSystemProperty.DEPLOYMENT_TYPE.get())) {
        // Applets obviously have AWT initialized before SWT...
        NativeSwing.initialize();
        runWithMacExecutor(new Runnable() {
          public void run() {
            findSWTDisplay();
          }
        });
      } else {
        try {
          findSWTDisplay();
        } catch(SWTException e) {
          if(Utils.IS_MAC) {
            // When -XstartOnFirstThread is not specified, we can apply the same detection as the applet mode.
            runWithMacExecutor(new Runnable() {
              public void run() {
                findSWTDisplay();
              }
            });
          } else {
            throw e;
          }
        }
        // Tweak AWT/Swing, after SWT has finished initializing or else there can be stability issues.
        NativeSwing.initialize();
      }
      if(!Utils.IS_MAC || Boolean.parseBoolean(NSSystemPropertySWT.INTERFACE_INPROCESS_FORCESHUTDOWNHOOK.get())) {
        Runtime.getRuntime().addShutdownHook(new Thread("NativeSwing Shutdown Hook") {
          @Override
          public void run() {
            destroyControls();
          }
        });
      }
    }

    private static void runWithMacExecutor(final Runnable runnable) {
      // When in an applet on Mac, we have no access to the main thread so we have to use a special mechanism.
      Executor mainQueueExecutor;
      try {
        Object dispatch = Class.forName("com.apple.concurrent.Dispatch").getMethod("getInstance").invoke(null);
        mainQueueExecutor = (Executor)dispatch.getClass().getMethod("getNonBlockingMainQueueExecutor").invoke(dispatch);
      } catch(Exception e) {
        throw new IllegalStateException("Failed to use the Mac Dispatch executor. This may happen if the version of Java that is used is too old.", e);
      }
      // It seems the executor is asynchronous but we want this method to be synchronous: we sync using a lock.
      // Note that this code should work even if it were synchronous.
      final AtomicBoolean isExecutorCallComplete = new AtomicBoolean(false);
      final AtomicReference<Throwable> exceptionReference = new AtomicReference<Throwable>();
      synchronized(isExecutorCallComplete) {
        mainQueueExecutor.execute(new Runnable() {
          public void run() {
            try {
              runnable.run();
            } catch(Throwable t) {
              exceptionReference.set(t);
            } finally {
              synchronized(isExecutorCallComplete) {
                isExecutorCallComplete.set(true);
                isExecutorCallComplete.notify();
              }
            }
          }
        });
        while(!isExecutorCallComplete.get()) {
          try {
            isExecutorCallComplete.wait();
          } catch (InterruptedException e) {}
        }
      }
      Throwable throwable = exceptionReference.get();
      if(throwable != null) {
        if(throwable instanceof RuntimeException) {
          throw (RuntimeException)throwable;
        }
        throw new RuntimeException(throwable);
      }
    }

    private static void findSWTDisplay() {
      display = Display.getCurrent();
      if(display == null && Boolean.parseBoolean(NSSystemPropertySWT.INTERFACE_INPROCESS_USEEXTERNALSWTDISPLAY.get())) {
        display = Display.getDefault();
        if(display.getThread() == Thread.currentThread()) {
          // Though we wanted to recycle the display, it was actually created by us so we dispose it and create it properly.
          display.dispose();
          display = null;
          NSSystemPropertySWT.INTERFACE_INPROCESS_USEEXTERNALSWTDISPLAY.set("false");
        }
      }
      if(display == null) {
        DeviceData data = new DeviceData();
        data.debug = Boolean.parseBoolean(NSSystemPropertySWT.SWT_DEVICEDATA_DEBUG.get());
        data.tracking = Boolean.parseBoolean(NSSystemPropertySWT.SWT_DEVICEDATA_TRACKING.get());
        display = new Display(data);
      }
      display.addListener(SWT.Close, new Listener() {
        public void handleEvent(Event event) {
          handleQuit();
        }
      });
    }

    private static MessagingInterface createInProcessMessagingInterface() {
      int pid_ = ++pid;
      return new SWTInProcessMessagingInterface(display, pid_).getMirrorMessagingInterface();
    }

    static void runEventPump() {
      if(Boolean.parseBoolean(NSSystemPropertySWT.INTERFACE_INPROCESS_USEEXTERNALSWTDISPLAY.get()) && display.getThread() != Thread.currentThread()) {
        // If we recycle the display thread (we haven't created it) and runEventPump is called, we just return.
        return;
      }
      if(Utils.IS_MAC && display.getThread() != Thread.currentThread()) {
        runWithMacExecutor(new Runnable() {
          public void run() {
            runSWTEventPump();
          }
        });
        return;
      }
      runSWTEventPump();
    }

    private static void runSWTEventPump() {
      while(isEventPumpRunning) {
        try {
          if(display.isDisposed()) {
            isEventPumpRunning = false;
          } else if(!display.readAndDispatch()) {
            if(isEventPumpRunning) {
              display.sleep();
            }
          }
        } catch(Throwable t) {
          t.printStackTrace();
        }
      }
      display.dispose();
    }

  }

  static class OutProcess {

    private static class CMN_destroyControls extends CommandMessage {
      @Override
      public Object run(Object[] args) throws Exception {
        if(display != null && !display.isDisposed()) {
          display.syncExec(new Runnable() {
            public void run() {
              destroyControls();
            }
          });
        }
        return null;
      }
    }

    private static void initialize() {
      NativeSwing.initialize();
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          boolean isOpen_;
          synchronized(OPEN_STATE_LOCK) {
            isOpen_ = isOpen;
          }
          if(isOpen_) {
            new CMN_destroyControls().asyncExec(true);
          }
        }
      });
    }

    private static class CMN_setProperties extends CommandMessage {
      @Override
      public Object run(Object[] args) {
        Properties systemProperties = System.getProperties();
        Properties properties = (Properties)args[0];
        for(Object o: properties.keySet()) {
          if(!systemProperties.containsKey(o)) {
            try {
              System.setProperty((String)o, properties.getProperty((String)o));
            } catch(Exception e) {
            }
          }
        }
        return null;
      }
    }

    static boolean isNativeSide() {
      return display != null;
    }

    static void createOutProcessCommunicationChannel() {
      synchronized(OPEN_STATE_LOCK) {
        for(int i=2; i>=0; i--) {
          try {
            messagingInterface = createOutProcessMessagingInterface();
            break;
          } catch(RuntimeException e) {
            if(i == 0) {
              throw e;
            }
          }
        }
        isOpen = true;
      }
      Properties nativeProperties = new Properties();
      Properties properties = System.getProperties();
      for(Object key: properties.keySet()) {
        if(key instanceof String) {
          Object value = properties.get(key);
          if(value instanceof String) {
            nativeProperties.setProperty((String)key, (String)value);
          }
        }
      }
      new CMN_setProperties().syncExec(true, nativeProperties);
    }

    private static Process createProcess(String localHostAddress, int port, int pid) {
      List<String> classPathList = new ArrayList<String>();
      List<Object> referenceList = new ArrayList<Object>();
      Class<?>[] nativeClassPathReferenceClasses = getNativeClassPathReferenceClasses(nativeInterfaceConfiguration);
      if(nativeClassPathReferenceClasses != null) {
        referenceList.addAll(Arrays.asList(nativeClassPathReferenceClasses));
      }
      String[] nativeClassPathReferenceResources = getNativeClassPathReferenceResources(nativeInterfaceConfiguration);
      if(nativeClassPathReferenceResources != null) {
        referenceList.addAll(Arrays.asList(nativeClassPathReferenceResources));
      }
      List<String> optionalReferenceList = new ArrayList<String>();
      referenceList.add(NativeSwing.class);
      referenceList.add(NativeInterface.class);
      referenceList.add(SWTNativeInterface.class);
      if(SWTNativeInterface.class.getClassLoader() != NativeInterface.class.getClassLoader()) {
        WebServer.getDefaultWebServer().addReferenceClassLoader(SWTNativeInterface.class.getClassLoader());
      }
      referenceList.add("org/eclipse/swt/widgets/Display.class");
      optionalReferenceList.add("org/mozilla/xpcom/Mozilla.class");
      optionalReferenceList.add("org/mozilla/interfaces/nsIWebBrowser.class");
      for(String optionalReference: optionalReferenceList) {
        if(optionalReference.startsWith("/")) {
          optionalReference = optionalReference.substring(1);
        }
        if(SWTNativeInterface.class.getResource('/' + optionalReference) != null) {
          referenceList.add(optionalReference);
        }
      }
      boolean isProxyClassLoaderUsed = Boolean.parseBoolean(NSSystemPropertySWT.PEERVM_FORCEPROXYCLASSLOADER.get());
      if(!isProxyClassLoaderUsed) {
        for(Object o: referenceList) {
          File clazzClassPath;
          if(o instanceof Class<?>) {
            clazzClassPath = Utils.getClassPathFile((Class<?>)o);
          } else {
            String resource = (String)o;
            clazzClassPath = Utils.getClassPathFile(resource);
            if(SWTNativeInterface.class.getResource('/' + resource) == null) {
              throw new IllegalStateException("A resource that is needed in the classpath is missing: " + o);
            }
          }
          if(clazzClassPath != null) {
            String path = clazzClassPath.getAbsolutePath();
            if(!classPathList.contains(path)) {
              classPathList.add(path);
            }
          } else {
            isProxyClassLoaderUsed = true;
            // We don't break because we want to check that there are no mandatory missing resources.
          }
        }
      }
      if(isProxyClassLoaderUsed) {
        // We set only one item in the classpath: the path to the proxy class loader.
        classPathList.clear();
        File classPathFile = new File(SystemProperty.JAVA_IO_TMPDIR.get(), ".djnativeswing/classpath");
        Utils.deleteAll(classPathFile);
        String classPath = NetworkURLClassLoader.class.getName().replace('.', '/') + ".class";
        File mainClassFile = new File(classPathFile, classPath);
        mainClassFile.getParentFile().mkdirs();
        if(!mainClassFile.exists()) {
          try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(mainClassFile));
            BufferedInputStream in = new BufferedInputStream(SWTNativeInterface.class.getResourceAsStream("/" + classPath));
            byte[] bytes = new byte[1024];
            for(int n; (n=in.read(bytes)) != -1; out.write(bytes, 0, n)) {
            }
            in.close();
            out.close();
          } catch(Exception e) {
            e.printStackTrace();
//            throw new IllegalStateException("Cannot find a suitable classpath to spawn VM!");
          }
          mainClassFile.deleteOnExit();
        }
        classPathList.add(classPathFile.getAbsolutePath());
      }
      List<String> vmParamList = new ArrayList<String>();
      Map<String, String> systemPropertiesMap = new HashMap<String, String>();
//      systemPropertiesMap.put("visualvm.display.name", "NativeSwingPeer#" + pid);
      String[] peerVMParams = getPeerVMParams(nativeInterfaceConfiguration);
      boolean isJavaLibraryPathProperySpecified = false;
      boolean isSWTLibraryPathProperySpecified = false;
      if(peerVMParams != null) {
        for(String peerVMParam: peerVMParams) {
          if(peerVMParam.startsWith("-D")) {
            String property = peerVMParam.substring(2);
            int index = property.indexOf('=');
            String propertyKey = property.substring(0, index);
            String propertyValue = property.substring(index + 1);
            systemPropertiesMap.put(propertyKey, propertyValue);
            if(SystemProperty.JAVA_LIBRARY_PATH.getName().equals(propertyKey)) {
              isJavaLibraryPathProperySpecified = true;
            } else if("swt.library.path".equals(propertyKey)) {
              isSWTLibraryPathProperySpecified = true;
            }
          } else {
            vmParamList.add(peerVMParam);
          }
        }
      }
      if(!isJavaLibraryPathProperySpecified) {
        String javaLibraryPath = SystemProperty.JAVA_LIBRARY_PATH.get();
        if(javaLibraryPath != null) {
          systemPropertiesMap.put(SystemProperty.JAVA_LIBRARY_PATH.getName(), javaLibraryPath);
        }
      }
      if(!isSWTLibraryPathProperySpecified) {
        String swtLibraryPath = NSSystemPropertySWT.SWT_LIBRARY_PATH.get();
        if(swtLibraryPath != null) {
          systemPropertiesMap.put(NSSystemPropertySWT.SWT_LIBRARY_PATH.getName(), swtLibraryPath);
        }
      }
      String[] flags = new String[] {
          NSSystemPropertySWT.INTERFACE_SYNCMESSAGES.getName(),
          NSSystemPropertySWT.INTERFACE_DEBUG_PRINTMESSAGES.getName(),
          NSSystemPropertySWT.PEERVM_DEBUG_PRINTSTARTMESSAGE.getName(),
          NSSystemPropertySWT.PEERVM_DEBUG_PRINTSTOPMESSAGE.getName(),
          NSSystemPropertySWT.SWT_DEVICE_DEBUG.getName(),
          NSSystemPropertySWT.SWT_DEVICEDATA_DEBUG.getName(),
          NSSystemPropertySWT.SWT_DEVICEDATA_TRACKING.getName(),
      };
      for(String flag: flags) {
        if(Boolean.parseBoolean(System.getProperty(flag))) {
          systemPropertiesMap.put(flag, "true");
        }
      }
      systemPropertiesMap.put(NSSystemProperty.LOCALHOSTADDRESS.getName(), localHostAddress);
      String mainClass;
      List<String> mainClassParameterList = new ArrayList<String>();
      if(isProxyClassLoaderUsed) {
        mainClass = NetworkURLClassLoader.class.getName();
        mainClassParameterList.add(WebServer.getDefaultWebServer().getClassPathResourceURL("", ""));
        mainClassParameterList.add(NativeInterface.class.getName());
      } else {
        mainClass = NativeInterface.class.getName();
      }
      mainClassParameterList.add(String.valueOf(pid));
      mainClassParameterList.add(String.valueOf(port));
      PeerVMProcessFactory peerVMProcessFactory = nativeInterfaceConfiguration.getPeerVMProcessFactory();
      if(peerVMProcessFactory == null) {
        peerVMProcessFactory = new DefaultPeerVMProcessFactory();
      }
      Process p = null;
      try {
        p = peerVMProcessFactory.createProcess(classPathList.toArray(new String[0]), systemPropertiesMap, vmParamList.toArray(new String[0]), mainClass, mainClassParameterList.toArray(new String[0]));
      } catch(Exception e) {
        throw new IllegalStateException("Failed to spawn the peer VM!", e);
      }
      if(p == null) {
        throw new IllegalStateException("Failed to spawn the peer VM!");
      }
      return p;
    }

    private static final boolean IS_PROCESS_IO_CHANNEL_MODE = "processio".equals(NSSystemPropertySWT.INTERFACE_OUTPROCESS_COMMUNICATION.get());

    private static volatile int pid;

    private static MessagingInterface createOutProcessMessagingInterface() {
      String localHostAddress = Utils.getLocalHostAddress();
      if(localHostAddress == null) {
        throw new IllegalStateException("Failed to find a suitable local host address to communicate with a spawned VM!");
      }
      boolean isCreatingProcess = Boolean.parseBoolean(NSSystemPropertySWT.PEERVM_CREATE.get("true"));
      int port;
      boolean isProcessIOChannelMode = IS_PROCESS_IO_CHANNEL_MODE && isCreatingProcess;
      if(isProcessIOChannelMode) {
        port = 0;
      } else {
        port = Integer.parseInt(NSSystemPropertySWT.INTERFACE_PORT.get("-1"));
        if(port <= 0) {
          ServerSocket serverSocket;
          try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(false);
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName(localHostAddress), 0));
          } catch(IOException e) {
            throw new RuntimeException(e);
          }
          port = serverSocket.getLocalPort();
          try {
            serverSocket.close();
          } catch(IOException e) {
          }
        }
      }
      int pid_ = ++pid;
      Process p;
      if(isCreatingProcess) {
        p = createProcess(localHostAddress, port, pid_);
        if(!isProcessIOChannelMode) {
          connectStream(System.out, p.getInputStream(), "out", pid_);
        }
        connectStream(System.err, p.getErrorStream(), "err", pid_);
      } else {
        p = null;
      }
      if(isProcessIOChannelMode) {
        // We need the process in this mode, so it cannot be null.
        return new SwingOutProcessIOMessagingInterface(p.getInputStream(), p.getOutputStream(), false, p, pid_);
      }
      Exception exception = null;
      Socket socket = null;
      long peerVMConnectionTimeout = Integer.parseInt(NSSystemPropertySWT.INTERFACE_OUTPROCESS_CONNECTIONTIMEOUT.get("10000"));
      long startTime = System.currentTimeMillis();
      do {
        if(p != null) {
          try {
            p.exitValue();
            // The process is terminated so no need to try to connect to it.
            break;
          } catch(IllegalThreadStateException e) {
            // Process is not terminated, which means no error
          }
        }
        try {
          socket = new Socket(localHostAddress, port);
          exception = null;
          break;
        } catch(Exception e) {
          exception = e;
        }
        try {
          Thread.sleep(200);
        } catch(Exception e) {
        }
      } while(System.currentTimeMillis() - startTime < peerVMConnectionTimeout);
      if(socket == null) {
        if(p != null) {
          p.destroy();
        }
        if(exception == null) {
          throw new IllegalStateException("Failed to connect to spawned VM! The native side process was already terminated.");
        }
        throw new IllegalStateException("Failed to connect to spawned VM!", exception);
      }
      return new SwingOutProcessSocketsMessagingInterface(socket, false, p, pid_);
    }

    private static class IOStreamFormatter {

      private ByteArrayOutputStream baos = new ByteArrayOutputStream();
      private byte lastByte = (byte)Utils.LINE_SEPARATOR.charAt(Utils.LINE_SEPARATOR.length() - 1);
      private boolean isAddingMessage = true;
      private final byte[] prefixBytes;

      public IOStreamFormatter(int pid) {
        prefixBytes = ("NativeSwing[" + pid + "]: ").getBytes();
      }

      public byte[] process(byte[] bytes, int offset, int length) throws IOException {
        baos.reset();
        for(int i=offset; i<length; i++) {
          byte b = bytes[i];
          if(isAddingMessage) {
            baos.write(prefixBytes);
          }
          isAddingMessage = b == lastByte;
          baos.write(b);
        }
        return baos.toByteArray();
      }

    }

    private static void connectStream(final PrintStream out, InputStream in, String name, final int pid) {
      final BufferedInputStream bin = new BufferedInputStream(in);
      Thread streamThread = new Thread("NativeSwing[" + pid + "] " + name + " Stream Connector") {
        private IOStreamFormatter byteProcessor = new IOStreamFormatter(pid);
        @Override
        public void run() {
          try {
            byte[] bytes = new byte[1024];
            for(int i; (i=bin.read(bytes)) != -1; ) {
              byte[] result = byteProcessor.process(bytes, 0, i);
              try {
                out.write(result);
              } catch(Exception e) {
                e.printStackTrace();
              }
            }
          } catch(Exception e) {
          }
        }
      };
      streamThread.setDaemon(true);
      streamThread.start();
    }

    private static class CMJ_handleClosedDisplay extends CommandMessage {
      @Override
      public Object run(Object[] args) {
        handleQuit();
        return null;
      }
    }
    
    private static class CMJ_systemOut extends CommandMessage {
      @Override
      public Object run(Object[] args) {
        try {
          System.out.write((byte[])args[0]);
        } catch (IOException e) {
          e.printStackTrace();
        }
        return null;
      }
    }

    private static class CMJ_unlockSystemIn extends CommandMessage {
      @Override
      public Object run(Object[] args) throws Exception {
        new Message().asyncSend(true);
        return null;
      }
    }

    static void runNativeSide(String[] args) throws IOException {
      final int pid = Integer.parseInt(args[0]);
      if(Boolean.parseBoolean(NSSystemPropertySWT.PEERVM_DEBUG_PRINTSTARTMESSAGE.get())) {
        System.err.println("Starting peer VM #" + pid);
      }
      synchronized(OPEN_STATE_LOCK) {
        isOpen = true;
      }
      int port = Integer.parseInt(args[1]);
      boolean isProcessIOChannelMode = port <= 0;
      Socket socket = null;
      if(!isProcessIOChannelMode) {
        ServerSocket serverSocket = null;
        long startTime = System.currentTimeMillis();
        IOException exception;
        do {
          try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(Utils.getLocalHostAddress(), port));
            exception = null;
            break;
          } catch(IOException e) {
            exception = e;
            if(serverSocket != null) {
              try {
                serverSocket.close();
              } catch(Exception ex) {
              }
            }
            serverSocket = null;
          }
          try {
            Thread.sleep(200);
          } catch(Exception e) {
          }
        } while(System.currentTimeMillis() - startTime < 5000);
        if(serverSocket == null) {
          if(exception == null) {
            throw new IllegalStateException("Failed to create the server socket for native side communication!");
          }
          throw exception;
        }
        final ServerSocket serverSocket_ = serverSocket;
        if(!Boolean.parseBoolean(NSSystemPropertySWT.PEERVM_KEEPALIVE.get())) {
          Thread shutdownThread = new Thread("NativeSwing Shutdown") {
            @Override
            public void run() {
              try {
                sleep(10000);
              } catch(Exception e) {
              }
              boolean isNull;
              synchronized(OPEN_STATE_LOCK) {
                isNull = messagingInterface == null;
              }
              if(isNull) {
                try {
                  serverSocket_.close();
                } catch(Exception e) {
                }
              }
            }
          };
          shutdownThread.setDaemon(true);
          shutdownThread.start();
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
          @Override
          public void run() {
            // There used to be cases in the past where VM was not closed properly.
            // To prevent this case, we forcibly halt the VM after a timeout
            Thread t = new Thread("Forced VM termination thread") {
              @Override
              public void run() {
                try {
                  sleep(20000);
                } catch (InterruptedException e) {
                }
                Runtime.getRuntime().halt(-1);
              }
            };
            t.setDaemon(false);
            t.start();
            destroyControls();
          }
        });
        try {
          socket = serverSocket.accept();
        } catch(Exception e) {
          throw new IllegalStateException("The native side did not receive an incoming connection!", e);
        }
      }
//      // We set up a new security manager to track exit calls.
//      // When this happens, we dispose native resources.
//      try {
//        System.setSecurityManager(new SecurityManager() {
//          protected SecurityManager securityManager = System.getSecurityManager();
//          @Override
//          public void checkExit(int status) {
//            super.checkExit(status);
//            for(StackTraceElement stackTraceElement: Thread.currentThread().getStackTrace()) {
//              String className = stackTraceElement.getClassName();
//              String methodName = stackTraceElement.getMethodName();
//              if("java.lang.Runtime".equals(className) && ("exit".equals(methodName) || "halt".equals(methodName)) || "java.lang.System".equals(className) && "exit".equals(methodName)) {
//                //TODO: perform cleanup
//                break;
//              }
//            }
//          }
//          @Override
//          public void checkPermission(Permission perm) {
//            if(securityManager != null) {
//              securityManager.checkPermission(perm);
//            }
//          }
//        });
//      } catch(Exception e) {
//        e.printStackTrace();
//      }
      Device.DEBUG = Boolean.parseBoolean(NSSystemPropertySWT.SWT_DEVICE_DEBUG.get());
      DeviceData data = new DeviceData();
      data.debug = Boolean.parseBoolean(NSSystemPropertySWT.SWT_DEVICEDATA_DEBUG.get());
      data.tracking = Boolean.parseBoolean(NSSystemPropertySWT.SWT_DEVICEDATA_TRACKING.get());
      display = new Display(data);
      display.addListener(SWT.Close, new Listener() {
        public void handleEvent(Event event) {
          new CMJ_handleClosedDisplay().asyncExec(false);
        }
      });
      Display.setAppName("DJ Native Swing");
      if(isProcessIOChannelMode) {
        PrintStream sysout = System.out;
        InputStream sysin = System.in;
        SWTOutProcessIOMessagingInterface outInterface = new SWTOutProcessIOMessagingInterface(sysin, sysout, true, display, pid);
        synchronized(OPEN_STATE_LOCK) {
          messagingInterface = outInterface;
        }
        System.setIn(new InputStream() {
          @Override
          public int read() throws IOException {
            while(true) {
              try {
                Thread.sleep(Long.MAX_VALUE);
              } catch(Exception e) {}
            }
          }
        });
        System.setOut(new PrintStream(new OutputStream() {
          private IOStreamFormatter byteProcessor = new IOStreamFormatter(pid);
          @Override
          public void write(int b) throws IOException {
            sendBytes(new byte[] {(byte)b}, 0, 1);
          }
          @Override
          public void write(byte[] b) throws IOException {
            sendBytes(b, 0, b.length);
          }
          @Override
          public void write(byte[] b, int off, int len) throws IOException {
            sendBytes(b, off, len);
          }
          private void sendBytes(byte[] bytes, int offset, int length) {
            try {
              new CMJ_systemOut().asyncExec(false, byteProcessor.process(bytes, offset, length));
            } catch(Exception e) {}
          }
        }));
        if(Utils.IS_WINDOWS) {
          // TODO: remove when SWT bug 270364 is fixed.
          final MessagingInterface messagingInterface_;
          synchronized(OPEN_STATE_LOCK) {
            messagingInterface_ = messagingInterface;
          }
          new Thread("System.in unlocker") {
            @Override
            public void run() {
              while(messagingInterface_.isAlive()) {
                if(System.currentTimeMillis() - lastProcessTime > 100) {
                  new CMJ_unlockSystemIn().asyncExec(false);
                  lastProcessTime = System.currentTimeMillis();
                }
                try {
                  sleep(100);
                } catch (Exception e) {
                }
              }
            }
          }.start();
        }
      } else {
        SWTOutProcessSocketsMessagingInterface outInterface = new SWTOutProcessSocketsMessagingInterface(socket, true, display, pid);
        synchronized(OPEN_STATE_LOCK) {
          messagingInterface = outInterface;
        }
      }
      while(display != null && !display.isDisposed()) {
        try {
          lastProcessTime = System.currentTimeMillis();
          if(!display.readAndDispatch()) {
            lastProcessTime = Long.MAX_VALUE;
            display.sleep();
          }
          lastProcessTime = Long.MAX_VALUE;
        } catch(Throwable t) {
          t.printStackTrace();
        }
      }
      if(Boolean.parseBoolean(NSSystemPropertySWT.PEERVM_DEBUG_PRINTSTOPMESSAGE.get())) {
        System.err.println("Stopping peer VM #" + pid);
      }
    }

    static void runEventPump() {
      // There is nothing to be done for out process, but we want this call to be blocking.
      while(isEventPumpRunning) {
        try {
          Thread.sleep(1000);
        } catch (Exception e) {
        }
      }
    }

  }

  private static volatile long lastProcessTime = Long.MAX_VALUE;

  private static void destroyControls() {
    if(display != null && !display.isDisposed()) {
      if(display.getThread() != Thread.currentThread()) {
        display.syncExec(new Runnable() {
          public void run() {
            destroyControls();
          }
        });
        return;
      }
      for(Control control: SWTNativeComponent.getControls()) {
        Shell shell = control.isDisposed()? null: control.getShell();
        try {
          if(shell != null) {
            shell.dispose();
          }
        } catch(Exception e) {
          // An exception happens with OLE components... but things get cleared as expected it seems
        }
        control.dispose();
      }
      try {
        // This line was commented out but I don't remember if there was a reason.
        // It seems disposing the display solves certain issue, like the browser not terminating cleanly.
        // Thus: I am going to be defensive and wrap the call with a try/catch (but dump the trace to follow up).
        display.dispose();
      } catch(Throwable t) {
        t.printStackTrace();
      }
    }
  }

  public static SWTNativeInterface getInstance() {
    return (SWTNativeInterface)NativeInterface.getInstance();
  }

  protected static int getMessageID(Message message) {
    return NativeInterface.getMessageID(message);
  }

  protected static boolean isMessageValid(Message message) {
    return NativeInterface.isMessageValid(message);
  }

  protected static Object runMessageCommand(LocalMessage commandMessage) {
    return NativeInterface.runMessageCommand(commandMessage);
  }

  protected static Object runMessageCommand(CommandMessage commandMessage) throws Exception {
    return NativeInterface.runMessageCommand(commandMessage);
  }

  protected static boolean isMessageSyncExec(Message message) {
    return NativeInterface.isMessageSyncExec(message);
  }

  protected static void setMessageSyncExec(Message message, boolean isSyncExec) {
    NativeInterface.setMessageSyncExec(message, isSyncExec);
  }

  protected static void setMessageArgs(CommandMessage message, Object... args) {
    NativeInterface.setMessageArgs(message, args);
  }

  protected static void computeMessageID(Message message, boolean isTargetNativeSide) {
    NativeInterface.computeMessageID(message, isTargetNativeSide);
  }

  protected static void setMessageUI(Message message, boolean isUI) {
    NativeInterface.setMessageUI(message, isUI);
  }

  protected static boolean isMessageUI(Message message) {
    return NativeInterface.isMessageUI(message);
  }

  /**
   * The main method that is called by the native side (peer VM).
   * @param args the arguments that are passed to the peer VM.
   */
  public void main_(String[] args) throws Exception {
    OutProcess.runNativeSide(args);
  }

}
