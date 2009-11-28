/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

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
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.event.EventListenerList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Display;

import chrriis.common.NetworkURLClassLoader;
import chrriis.common.SystemProperty;
import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.dj.nativeswing.NativeSwing;
import chrriis.dj.nativeswing.swtimpl.InProcessMessagingInterface.SWTInProcessMessagingInterface;
import chrriis.dj.nativeswing.swtimpl.InProcessMessagingInterface.SwingInProcessMessagingInterface;
import chrriis.dj.nativeswing.swtimpl.OutProcessIOMessagingInterface.SWTOutProcessIOMessagingInterface;
import chrriis.dj.nativeswing.swtimpl.OutProcessIOMessagingInterface.SwingOutProcessIOMessagingInterface;
import chrriis.dj.nativeswing.swtimpl.OutProcessSocketsMessagingInterface.SWTOutProcessSocketsMessagingInterface;
import chrriis.dj.nativeswing.swtimpl.OutProcessSocketsMessagingInterface.SwingOutProcessSocketsMessagingInterface;

/**
 * The native interface, which establishes the link between the peer VM (native side) and the local side.
 * @author Christopher Deckers
 */
public class NativeInterface {

  private static final boolean IS_SYNCING_MESSAGES = Boolean.parseBoolean(NSSystemPropertySWT.INTERFACE_SYNCMESSAGES.get());

  static boolean isAlive() {
    synchronized(OPEN_STATE_LOCK) {
      return isOpen() && messagingInterface.isAlive();
    }
  }

  private static boolean isOpen;

  /**
   * Indicate whether the native interface is open.
   * @return true if the native interface is open, false otherwise.
   */
  public static boolean isOpen() {
    synchronized(OPEN_STATE_LOCK) {
      return isOpen;
    }
  }

  private static void checkOpen() {
    if(!isOpen()) {
      throw new IllegalStateException("The native interface is not open! Please refer to the instructions to set it up properly.");
    }
  }

  /**
   * Close the native interface, which destroys the native side (peer VM). Note that the native interface can be re-opened later.
   */
  public static void close() {
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
  public static NativeInterfaceConfiguration getConfiguration() {
    if(nativeInterfaceConfiguration == null) {
      nativeInterfaceConfiguration = new NativeInterfaceConfiguration();
    }
    return nativeInterfaceConfiguration;
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

  private static volatile boolean isInitialized;

  /**
   * Indicate whether the native interface is initialized.
   * @return true if the native interface is initialized, false otherwise.
   */
  public static boolean isInitialized() {
    return isInitialized;
  }

  private static boolean isInProcess;

  static boolean isInProcess() {
    synchronized(OPEN_STATE_LOCK) {
      return isInProcess;
    }
  }

  private static class CMN_dumpStackTraces extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      Utils.dumpStackTraces();
      return null;
    }
  }

  /**
   * Initialize the native interface, but do not open it. This method sets some properties and registers a few listeners to keep track of certain states necessary for the good functioning of the framework.<br/>
   * This method is automatically called if open() is used. It should be called early in the program, the best place being as the first call in the main method.
   */
  public static void initialize() {
    synchronized(OPEN_CLOSE_SYNC_LOCK) {
      if(isInitialized()) {
        return;
      }
      // Check the versions of the libraries.
      if(SWT.getVersion() < 3617) {
        throw new IllegalStateException("The version of SWT that is required is 3.6M3 or later!");
      }
      if(nativeInterfaceConfiguration == null) {
        nativeInterfaceConfiguration = new NativeInterfaceConfiguration();
      }
      NativeSwing.initialize();
      Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
        public void eventDispatched(AWTEvent e) {
          KeyEvent ke = (KeyEvent)e;
          if(ke.getID() == KeyEvent.KEY_PRESSED && ke.getKeyCode() == KeyEvent.VK_F3 && ke.isControlDown() && ke.isAltDown() && ke.isShiftDown()) {
            new Thread("Dump stack traces") {
              @Override
              public void run() {
                CMN_dumpStackTraces cmnDumpStackTraces = new CMN_dumpStackTraces();
                cmnDumpStackTraces.run(null);
                if(!isInProcess() && isOpen()) {
                  syncSend(true, cmnDumpStackTraces);
                }
              }
            }.start();
          }
        }
      }, AWTEvent.KEY_EVENT_MASK);
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
      if(isInProcess()) {
        InProcess.initialize();
      }
      isInitialized = true;
    }
  }

  private static final Object OPEN_CLOSE_SYNC_LOCK = new Object();
  private static final Object OPEN_STATE_LOCK = new Object();

  /**
   * Open the native interface, which creates the peer VM that handles the native side of the native integration.<br/>
   * Initialization takes place if the interface was not already initialized. If initialization was not explicitely performed, this method should be called early in the program, the best place being as the first call in the main method.
   */
  public static void open() {
    synchronized(OPEN_CLOSE_SYNC_LOCK) {
      if(isOpen()) {
        return;
      }
      initialize();
      loadClipboardDebuggingProperties();
      if(isInProcess()) {
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

  static boolean notifyKilled() {
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
    if(!NativeInterface.OutProcess.isNativeSide() && nativeInterfaceConfiguration.isNativeSideRespawnedOnError()) {
      OutProcess.createOutProcessCommunicationChannel();
      return true;
    }
    return false;
  }

  static void notifyRespawned() {
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

  private NativeInterface() {}

  static Object syncSend(boolean isTargetNativeSide, final Message message) {
    checkOpen();
    if(message instanceof LocalMessage) {
      LocalMessage localMessage = (LocalMessage)message;
      return localMessage.runCommand();
    }
    return getMessagingInterface(!isTargetNativeSide).syncSend(message);
  }

  static void asyncSend(boolean isTargetNativeSide, final Message message) {
    if(IS_SYNCING_MESSAGES) {
      syncSend(isTargetNativeSide, message);
    } else {
      checkOpen();
      if(message instanceof LocalMessage) {
        LocalMessage localMessage = (LocalMessage)message;
        localMessage.runCommand();
        return;
      }
      getMessagingInterface(!isTargetNativeSide).asyncSend(message);
    }
  }

  private static MessagingInterface messagingInterface;

  static MessagingInterface getMessagingInterface(boolean isNativeSide) {
    synchronized(OPEN_STATE_LOCK) {
      if(isInProcess()) {
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

  private static Display display;

  /**
   * Get the SWT display. This is only possible when in the native context.
   * @return the display, or null.
   */
  public static Display getDisplay() {
    return display;
  }

  /**
   * Indicate if the current thread is the user interface thread.
   * @return true if the current thread is the user interface thread.
   * @throws IllegalStateException when the native interface is not alive.
   */
  public static boolean isUIThread(boolean isNativeSide) {
    if(!isAlive()) {
      throw new IllegalStateException("The native interface is not alive!");
    }
    return getMessagingInterface(isNativeSide).isUIThread();
  }

  static void checkUIThread(boolean isNativeSide) {
    if(!isAlive()) {
      throw new IllegalStateException("The native interface is not alive!");
    }
    getMessagingInterface(isNativeSide).checkUIThread();
  }

  private static volatile boolean isEventPumpRunning;

  /**
   * Run the native event pump. Certain platforms require this method call at the end of the main method to function properly, so it is suggested to always add it.
   */
  public static void runEventPump() {
    if(!isInitialized()) {
      throw new IllegalStateException("Cannot run the event pump when the interface is not initialized!");
    }
    if(isEventPumpRunning) {
      throw new IllegalStateException("runEventPump was already called and can only be called once (the first call should be at the end of the main method)!");
    }
    isEventPumpRunning = true;
    if(isInProcess()) {
      InProcess.runEventPump();
    }
  }

  private static EventListenerList listenerList = new EventListenerList();

  /**
   * Add a native interface listener.
   * @param listener the native listener to add.
   */
  public static void addNativeInterfaceListener(NativeInterfaceListener listener) {
    listenerList.add(NativeInterfaceListener.class, listener);
  }

  /**
   * Remove a native interface listener.
   * @param listener the native listener to remove.
   */
  public static void removeNativeInterfaceListener(NativeInterfaceListener listener) {
    listenerList.remove(NativeInterfaceListener.class, listener);
  }

  /**
   * Get all the native interface listeners.
   * @return the native interface listeners.
   */
  public static NativeInterfaceListener[] getNativeInterfaceListeners() {
    return listenerList.getListeners(NativeInterfaceListener.class);
  }

  static class InProcess {

    static void createInProcessCommunicationChannel() {
      synchronized(OPEN_STATE_LOCK) {
        messagingInterface = createInProcessMessagingInterface();
        isOpen = true;
      }
    }

    private static void initialize() {
      Device.DEBUG = Boolean.parseBoolean(NSSystemPropertySWT.SWT_DEVICE_DEBUG.get());
      display = Display.getCurrent();
      if(display == null && Boolean.parseBoolean(System.getProperty("nativeswing.interface.inprocess.useExternalSWTDisplay"))) {
        display = Display.getDefault();
        if(display.getThread() == Thread.currentThread()) {
          // The display was created by us, so we dispose it and create it properly.
          display.dispose();
          display = null;
          System.setProperty("nativeswing.interface.inprocess.useExternalSWTDisplay", "false");
        }
      }
      if(display == null) {
        DeviceData data = new DeviceData();
        data.debug = Boolean.parseBoolean(System.getProperty("nativeswing.swt.devicedata.debug"));
        data.tracking = Boolean.parseBoolean(System.getProperty("nativeswing.swt.devicedata.tracking"));
        display = new Display(data);
      }
    }

    private static MessagingInterface createInProcessMessagingInterface() {
      return new SWTInProcessMessagingInterface(display).getMirrorMessagingInterface();
    }

    static void runEventPump() {
      if(Boolean.parseBoolean(System.getProperty("nativeswing.interface.inprocess.useExternalSWTDisplay")) && display.getThread() != Thread.currentThread()) {
        // If we recycle the display thread (we haven't created it) and runEventPump is called, we just return.
        return;
      }
      startAutoShutdownThread();
      while(isEventPumpRunning) {
        try {
          if(!display.readAndDispatch()) {
            if(isEventPumpRunning) {
              display.sleep();
            }
          }
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
      display.dispose();
    }

    static void startAutoShutdownThread() {
      final Thread displayThread = display.getThread();
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
              if(t != displayThread && !t.isDaemon() && t.isAlive()) {
                isAlive = true;
                break;
              }
            }
          }
          // Shutdown procedure
          display.asyncExec(new Runnable() {
            public void run() {
              isEventPumpRunning = false;
            }
          });
        }
      };
      autoShutdownThread.setDaemon(true);
      autoShutdownThread.start();
    }

  }

  static class OutProcess {

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
      String pathSeparator = SystemProperty.PATH_SEPARATOR.get();
      List<Object> referenceList = new ArrayList<Object>();
      List<String> optionalReferenceList = new ArrayList<String>();
      referenceList.add(NativeSwing.class);
      referenceList.add(NativeInterface.class);
      referenceList.add("org/eclipse/swt/widgets/Display.class");
      optionalReferenceList.add("org/mozilla/xpcom/Mozilla.class");
      optionalReferenceList.add("org/mozilla/interfaces/nsIWebBrowser.class");
      for(String optionalReference: optionalReferenceList) {
        if(NativeInterface.class.getClassLoader().getResource(optionalReference) != null) {
          referenceList.add(optionalReference);
        }
      }
      Class<?>[] nativeClassPathReferenceClasses = nativeInterfaceConfiguration.getNativeClassPathReferenceClasses();
      if(nativeClassPathReferenceClasses != null) {
        referenceList.addAll(Arrays.asList(nativeClassPathReferenceClasses));
      }
      String[] nativeClassPathReferenceResources = nativeInterfaceConfiguration.getNativeClassPathReferenceResources();
      if(nativeClassPathReferenceResources != null) {
        referenceList.addAll(Arrays.asList(nativeClassPathReferenceResources));
      }
      boolean isProxyClassLoaderUsed = Boolean.parseBoolean(NSSystemPropertySWT.PEERVM_FORCEPROXYCLASSLOADER.get());
      if(!isProxyClassLoaderUsed) {
        for(Object o: referenceList) {
          File clazzClassPath;
          if(o instanceof Class<?>) {
            clazzClassPath = Utils.getClassPathFile((Class<?>)o);
          } else {
            clazzClassPath = Utils.getClassPathFile((String)o);
            if(NativeInterface.class.getClassLoader().getResource((String)o) == null) {
              throw new IllegalStateException("A resource that is needed in the classpath is missing: " + o);
            }
          }
          clazzClassPath = o instanceof Class<?>? Utils.getClassPathFile((Class<?>)o): Utils.getClassPathFile((String)o);
          if(clazzClassPath != null) {
            String path = clazzClassPath.getAbsolutePath();
            if(!classPathList.contains(path)) {
              classPathList.add(path);
            }
          } else {
            isProxyClassLoaderUsed = true;
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
            BufferedInputStream in = new BufferedInputStream(NativeInterface.class.getResourceAsStream("/" + classPath));
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
      StringBuilder sb = new StringBuilder();
      for(int i=0; i<classPathList.size(); i++) {
        if(i > 0) {
          sb.append(pathSeparator);
        }
        sb.append(classPathList.get(i));
      }
      String javaHome = SystemProperty.JAVA_HOME.get();
      String[] candidateBinaries = new String[] {
          new File(javaHome, "bin/java").getAbsolutePath(),
          new File("/usr/lib/java").getAbsolutePath(),
          "java",
      };
      Process p = null;
      // Create the argument list for the Java process that will be created
      List<String> argList = new ArrayList<String>();
      // The first argument will be the binary
      argList.add(null);
//      argList.add("-Dvisualvm.display.name=NativeSwingPeer#" + pid);
      String[] peerVMParams = nativeInterfaceConfiguration.getPeerVMParams();
      if(peerVMParams != null) {
        for(String param: peerVMParams) {
          argList.add(param);
        }
      }
      String[] flags = new String[] {
          NSSystemPropertySWT.INTERFACE_SYNCMESSAGES.getName(),
          NSSystemPropertySWT.INTERFACE_DEBUG_PRINTMESSAGES.getName(),
          NSSystemPropertySWT.PEERVM_DEBUG_PRINTSTARTMESSAGE.getName(),
          NSSystemPropertySWT.SWT_DEVICE_DEBUG.getName(),
          "nativeswing.swt.devicedata.debug",
          "nativeswing.swt.devicedata.tracking",
      };
      for(String flag: flags) {
        if(Boolean.parseBoolean(System.getProperty(flag))) {
          argList.add("-D" + flag + "=true");
        }
      }
      argList.add("-Dnativeswing.localhostaddress=" + localHostAddress);
      argList.add("-classpath");
      argList.add(sb.toString());
      if(isProxyClassLoaderUsed) {
        argList.add(NetworkURLClassLoader.class.getName());
        argList.add(WebServer.getDefaultWebServer().getClassPathResourceURL("", ""));
      }
      argList.add(NativeInterface.class.getName());
      argList.add(String.valueOf(pid));
      argList.add(String.valueOf(port));
      // Try compatibility with Java applets on update 10.
      String javaVersion = SystemProperty.JAVA_VERSION.get();
      if(javaVersion != null && javaVersion.compareTo("1.6.0_10") >= 0 && "Sun Microsystems Inc.".equals(SystemProperty.JAVA_VENDOR.get())) {
        boolean isTryingAppletCompatibility = true;
        if(peerVMParams != null) {
          for(String peerVMParam: peerVMParams) {
            if(peerVMParam.startsWith("-Xbootclasspath/a:")) {
              isTryingAppletCompatibility = false;
              break;
            }
          }
        }
        if(isTryingAppletCompatibility) {
          File[] deploymentFiles = new File[] {
              new File(javaHome, "lib/deploy.jar"),
              new File(javaHome, "lib/plugin.jar"),
              new File(javaHome, "lib/javaws.jar"),
          };
          List<String> argListX = new ArrayList<String>();
          argListX.add(candidateBinaries[0]);
          StringBuilder sbX = new StringBuilder();
          for(int i=0; i<deploymentFiles.length; i++) {
            if(i != 0) {
              sbX.append(pathSeparator);
            }
            File deploymentFile = deploymentFiles[i];
            if(deploymentFile.exists()) {
              sbX.append(deploymentFile.getAbsolutePath());
            }
          }
          if(sbX.indexOf(" ") != -1) {
            // TODO: check what to do when there are spaces in paths on non-windows machines
            argListX.add("\"-Xbootclasspath/a:" + sbX + "\"");
          } else {
            argListX.add("-Xbootclasspath/a:" + sbX);
          }
          argListX.addAll(argList.subList(1, argList.size()));
          if(Boolean.parseBoolean(NSSystemPropertySWT.PEERVM_DEBUG_PRINTCOMMANDLINE.get())) {
            System.err.println("Native Command: " + Arrays.toString(argListX.toArray()));
          }
          try {
            p = new ProcessBuilder(argListX).start();
          } catch(IOException e) {
          }
        }
      }
      if(p == null) {
        // Try these arguments with the various candidate binaries.
        for(String candidateBinary: candidateBinaries) {
          argList.set(0, candidateBinary);
          if(Boolean.parseBoolean(NSSystemPropertySWT.PEERVM_DEBUG_PRINTCOMMANDLINE.get())) {
            System.err.println("Native Command: " + Arrays.toString(argList.toArray()));
          }
          try {
            p = new ProcessBuilder(argList).start();
            break;
          } catch(IOException e) {
          }
        }
      }
      if(p == null) {
        throw new IllegalStateException("Failed to spawn the VM!");
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
      Process p;
      if(isCreatingProcess) {
        pid++;
        p = createProcess(localHostAddress, port, pid);
        if(!isProcessIOChannelMode) {
          connectStream(System.out, p.getInputStream(), pid);
        }
        connectStream(System.err, p.getErrorStream(), pid);
      } else {
        p = null;
      }
      if(isProcessIOChannelMode) {
        // We need the process in this mode, so it cannot be null.
        return new SwingOutProcessIOMessagingInterface(p.getInputStream(), p.getOutputStream(), false, p);
      }
      Exception exception = null;
      Socket socket = null;
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
      } while(System.currentTimeMillis() - startTime < 10000);
      if(socket == null) {
        if(p != null) {
          p.destroy();
        }
        if(exception == null) {
          throw new IllegalStateException("Failed to connect to spawned VM! The native side process was already terminated.");
        }
        throw new IllegalStateException("Failed to connect to spawned VM!", exception);
      }
      return new SwingOutProcessSocketsMessagingInterface(socket, false, p);
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

    private static void connectStream(final PrintStream out, InputStream in, final int pid) {
      final BufferedInputStream bin = new BufferedInputStream(in);
      Thread streamThread = new Thread("NativeSwing Stream Connector") {
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
        try {
          socket = serverSocket.accept();
        } catch(Exception e) {
          throw new IllegalStateException("The native side did not receive an incoming connection!");
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
      data.debug = Boolean.parseBoolean(System.getProperty("nativeswing.swt.devicedata.debug"));
      data.tracking = Boolean.parseBoolean(System.getProperty("nativeswing.swt.devicedata.tracking"));
      display = new Display(data);
      Display.setAppName("DJ Native Swing");
      if(isProcessIOChannelMode) {
        PrintStream sysout = System.out;
        InputStream sysin = System.in;
        SWTOutProcessIOMessagingInterface outInterface = new SWTOutProcessIOMessagingInterface(sysin, sysout, true, display);
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
        SWTOutProcessSocketsMessagingInterface outInterface = new SWTOutProcessSocketsMessagingInterface(socket, true, display);
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
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static volatile long lastProcessTime = Long.MAX_VALUE;

  private static class CMJ_unlockSystemIn extends CommandMessage {
    @Override
    public Object run(Object[] args) throws Exception {
      new Message().asyncSend(true);
      return null;
    }
  }

  /**
   * The main method that is called by the native side (peer VM).
   * @param args the arguments that are passed to the peer VM.
   */
  public static void main(String[] args) throws Exception {
    OutProcess.runNativeSide(args);
  }

}
