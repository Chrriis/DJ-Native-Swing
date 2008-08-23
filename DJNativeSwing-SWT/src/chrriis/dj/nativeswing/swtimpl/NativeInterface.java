/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Display;

import chrriis.common.NetworkURLClassLoader;
import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.dj.nativeswing.NativeSwing;

/**
 * The native interface, which establishes the link between the peer VM (native side) and the local side.
 * @author Christopher Deckers
 */
public class NativeInterface {

  private static final boolean IS_SYNCING_MESSAGES = Boolean.parseBoolean(System.getProperty("nativeswing.interface.syncmessages"));
    
  static boolean isAlive() {
    return isOpen() && messagingInterface.isAlive();
  }
  
  private static boolean isOpen;
  
  private static boolean isOpen() {
    return isOpen;
  }
  
  private static void checkOpen() {
    if(!isOpen()) {
      throw new IllegalStateException("The native interface is not open! Please refer to the instructions to set it up properly.");
    }
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
  
  /**
   * Close the native interface, which destroys the native side (peer VM). Note that the native interface can be re-opened later.
   */
  public static void close() {
    isOpen = false;
    messagingInterface.destroy();
    messagingInterface = null;
    for(NativeInterfaceListener listener: getNativeInterfaceListeners()) {
      listener.nativeInterfaceClosed();
    }
  }

  private static NativeInterfaceConfiguration nativeInterfaceConfiguration;
  
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
  
  private static boolean isInitialized;

  private static boolean isInitialized() {
    return isInitialized;
  }
  
  /**
   * Initialize the native interface, but do not open it. This method sets some properties and registers a few listeners to keep track of certain states necessary for the good functioning of the framework.<br/>
   * This method is automatically called if open() is used. It should be called early in the program, the best place being as the first call in the main method.
   */
  public static void initialize() {
    if(isInitialized()) {
      return;
    }
    if(nativeInterfaceConfiguration == null) {
      nativeInterfaceConfiguration = new NativeInterfaceConfiguration();
    }
    NativeSwing.initialize();
    try {
      for(NativeInterfaceListener listener: getNativeInterfaceListeners()) {
        listener.nativeInterfaceInitialized();
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Open the native interface, which creates the peer VM that handles the native side of the native integration.<br/>
   * Initialization takes place if the interface was not already initialized. If initialization was not explicitely performed, this method should be called early in the program, the best place being as the first call in the main method.
   */
  public static void open() {
    if(isOpen()) {
      return;
    }
    initialize();
    loadClipboardDebuggingProperties();
    createCommunicationChannel();
    try {
      for(NativeInterfaceListener listener: getNativeInterfaceListeners()) {
        listener.nativeInterfaceOpened();
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  static boolean notifyKilled() {
    isOpen = false;
    messagingInterface = null;
    try {
      for(NativeInterfaceListener listener: getNativeInterfaceListeners()) {
        listener.nativeInterfaceClosed();
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
    if(!NativeInterface.isNativeSide() && nativeInterfaceConfiguration.isNativeSideRespawnedOnError()) {
      createCommunicationChannel();
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
  
  private static void createCommunicationChannel() {
    // Create the interface to communicate with the process handling the native side
    messagingInterface = createMessagingInterface();
    isOpen = true;
    // Set the system properties
    new CMN_setProperties().syncExec(System.getProperties());
  }
  
  private static Process createProcess(int port) {
    List<String> classPathList = new ArrayList<String>();
    String pathSeparator = System.getProperty("path.separator");
    List<Object> referenceList = new ArrayList<Object>();
    referenceList.add(NativeSwing.class);
    referenceList.add(NativeInterface.class);
    referenceList.add("org/eclipse/swt/widgets/Display.class");
    Class<?>[] nativeClassPathReferenceClasses = nativeInterfaceConfiguration.getNativeClassPathReferenceClasses();
    if(nativeClassPathReferenceClasses != null) {
      referenceList.addAll(Arrays.asList(nativeClassPathReferenceClasses));
    }
    String[] nativeClassPathReferenceResources = nativeInterfaceConfiguration.getNativeClassPathReferenceResources();
    if(nativeClassPathReferenceResources != null) {
      referenceList.addAll(Arrays.asList(nativeClassPathReferenceResources));
    }
    boolean isProxyClassLoaderUsed = Boolean.parseBoolean(System.getProperty("nativeswing.peervm.forceproxyclassloader"));
    if(!isProxyClassLoaderUsed) {
      for(Object o: referenceList) {
        File clazzClassPath;
        if(o instanceof Class) {
          clazzClassPath = Utils.getClassPathFile((Class<?>)o);
        } else {
          clazzClassPath = Utils.getClassPathFile((String)o);
          if(NativeInterface.class.getResource("/" + o) == null) {
            throw new IllegalStateException("A resource that is needed in the classpath is missing: " + o);
          }
        }
        clazzClassPath = o instanceof Class? Utils.getClassPathFile((Class<?>)o): Utils.getClassPathFile((String)o);
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
      File classPathFile = new File(System.getProperty("java.io.tmpdir"), ".djnativeswing/classpath");
      Utils.deleteAll(classPathFile);
      String classPath = NetworkURLClassLoader.class.getName().replace('.', '/') + ".class";
      File mainClassFile = new File(classPathFile, classPath);
      mainClassFile.getParentFile().mkdirs();
      if(!mainClassFile.exists()) {
        try {
          BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(mainClassFile));
          BufferedInputStream in = new BufferedInputStream(NativeInterface.class.getResourceAsStream("/" + classPath));
          byte[] bytes = new byte[1024];
          for(int n; (n=in.read(bytes)) != -1; out.write(bytes, 0, n));
          in.close();
          out.close();
        } catch(Exception e) {
//          throw new IllegalStateException("Cannot find a suitable classpath to spawn VM!");
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
    String javaHome = System.getProperty("java.home");
    String[] candidateBinaries = new String[] {
        new File(javaHome, "bin/java").getAbsolutePath(),
        new File("/usr/lib/java").getAbsolutePath(),
        "java",
    };
    Process p = null;
    // Create the argument list for the Java process that will be created
    List<String> argList = new ArrayList<String>();
    argList.add(null);
    String[] peerVMParams = nativeInterfaceConfiguration.getPeerVMParams();
    if(peerVMParams != null) {
      for(String param: peerVMParams) {
        argList.add(param);
      }
    }
    String[] flags = new String[] {
        "nativeswing.interface.syncmessages",
        "nativeswing.interface.debug.printmessages",
        "nativeswing.peervm.debug.printstartmessage",
        "nativeswing.swt.debug.device",
        "nativeswing.swt.devicedata.debug",
        "nativeswing.swt.devicedata.tracking",
    };
    for(String flag: flags) {
      if(Boolean.parseBoolean(System.getProperty(flag))) {
        argList.add("-D" + flag + "=true");
      }
    }
    argList.add("-classpath");
    argList.add(sb.toString());
    if(isProxyClassLoaderUsed) {
      argList.add(NetworkURLClassLoader.class.getName());
      argList.add(WebServer.getDefaultWebServer().getClassPathResourceURL("", ""));
    }
    argList.add(NativeInterface.class.getName());
    argList.add(String.valueOf(port));
    // Try these arguments with the various candidate binaries.
    for(String candidateBinary: candidateBinaries) {
      argList.set(0, candidateBinary);
      if(Boolean.parseBoolean(System.getProperty("nativeswing.peervm.debug.printcommandline"))) {
        System.err.println("Native Command: " + Arrays.toString(argList.toArray()));
      }
      try {
        p = new ProcessBuilder(argList).start();
        break;
      } catch(IOException e) {
      }
    }
    if(p == null) {
      throw new IllegalStateException("Failed to spawn the VM!");
    }
    connectStream(System.err, p.getErrorStream());
    connectStream(System.out, p.getInputStream());
    return p;
  }
  
  private static MessagingInterface createMessagingInterface() {
    int port = Integer.parseInt(System.getProperty("nativeswing.interface.port", "-1"));
    if(port <= 0) {
      ServerSocket serverSocket;
      try {
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(false);
        serverSocket.bind(new InetSocketAddress(0));
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
      port = serverSocket.getLocalPort();
      try {
        serverSocket.close();
      } catch(IOException e) {
      }
    }
    Process p;
    if(Boolean.parseBoolean(System.getProperty("nativeswing.peervm.create", "true"))) {
      p = createProcess(port);
    } else {
      p = null;
    }
    Socket socket = null;
    for(int i=99; i>=0; i--) {
      try {
        socket = new Socket("127.0.0.1", port);
        break;
      } catch(IOException e) {
        if(i == 0) {
          throw new RuntimeException(e);
        }
      }
      try {
        Thread.sleep(200);
      } catch(Exception e) {
      }
    }
    if(socket == null) {
      if(p != null) {
        p.destroy();
      }
      throw new IllegalStateException("Failed to connect to spawned VM!");
    }
    return new MessagingInterface(socket, false) {
      @Override
      protected void asyncUIExec(Runnable runnable) {
        SwingUtilities.invokeLater(runnable);
      }
      @Override
      public boolean isUIThread() {
        return SwingUtilities.isEventDispatchThread();
      }
    };
  }
  
  private static void connectStream(final PrintStream out, InputStream in) {
    final BufferedInputStream bin = new BufferedInputStream(in);
    Thread streamThread = new Thread("NativeSwing Stream Connector") {
      @Override
      public void run() {
        try {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          String lineSeparator = Utils.LINE_SEPARATOR;
          byte lastByte = (byte)lineSeparator.charAt(lineSeparator.length() - 1);
          boolean addMessage = true;
          byte[] bytes = new byte[1024];
          for(int i; (i=bin.read(bytes)) != -1; ) {
            baos.reset();
            for(int j=0; j<i; j++) {
              byte b = bytes[j];
              if(addMessage) {
                baos.write("NativeSwing: ".getBytes());
              }
              addMessage = b == lastByte;
              baos.write(b);
            }
            try {
              out.write(baos.toByteArray());
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
  
  private NativeInterface() {}
  
  private static volatile MessagingInterface messagingInterface;

  static MessagingInterface getMessagingInterface() {
    return messagingInterface;
  }
  
  static Object syncSend(final Message message) {
    checkOpen();
    if(message instanceof LocalMessage) {
      LocalMessage localMessage = (LocalMessage)message;
      return localMessage.runCommand();
    }
    return messagingInterface.syncSend(message);
  }
  
  static void asyncSend(final Message message) {
    if(IS_SYNCING_MESSAGES) {
      syncSend(message);
    } else {
      checkOpen();
      if(message instanceof LocalMessage) {
        LocalMessage localMessage = (LocalMessage)message;
        localMessage.runCommand();
        return;
      }
      messagingInterface.asyncSend(message);
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
  
  static boolean isNativeSide() {
    return display != null;
  }
  
  /**
   * Indicate if the current thread is the user interface thread.
   * @return true if the current thread is the user interface thread.
   * @throws IllegalStateException when the native interface is not alive.
   */
  public static boolean isUIThread() {
    if(!isAlive()) {
      throw new IllegalStateException("The native interface is not alive!");
    }
    return messagingInterface.isUIThread();
  }
  
  static void checkUIThread() {
    if(!isAlive()) {
      throw new IllegalStateException("The native interface is not alive!");
    }
    messagingInterface.checkUIThread();
  }
  
  /**
   * The main method that is called by the native side (peer VM).
   * @param args the arguments that are passed to the peer VM.
   */
  public static void main(String[] args) throws Exception {
    if(Boolean.parseBoolean(System.getProperty("nativeswing.peervm.debug.printstartmessage"))) {
      System.err.println("Starting spawned VM");
    }
    isOpen = true;
    int port = Integer.parseInt(args[0]);
    ServerSocket serverSocket = null;
    for(int i=19; i>=0; i--) {
      try {
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(port));
        break;
      } catch(IOException e) {
        if(i == 0) {
          throw e;
        }
      }
      try {
        Thread.sleep(100);
      } catch(Exception e) {
      }
    }
    final ServerSocket serverSocket_ = serverSocket;
    if(!Boolean.parseBoolean(System.getProperty("nativeswing.peervm.keepalive"))) {
      Thread shutdownThread = new Thread("NativeSwing Shutdown") {
        @Override
        public void run() {
          try {
            sleep(10000);
          } catch(Exception e) {
          }
          if(messagingInterface == null) {
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
//    // We set up a new security manager to track exit calls.
//    // When this happens, we dispose native resources.
//    try {
//      System.setSecurityManager(new SecurityManager() {
//        protected SecurityManager securityManager = System.getSecurityManager();
//        @Override
//        public void checkExit(int status) {
//          super.checkExit(status);
//          for(StackTraceElement stackTraceElement: Thread.currentThread().getStackTrace()) {
//            String className = stackTraceElement.getClassName();
//            String methodName = stackTraceElement.getMethodName();
//            if("java.lang.Runtime".equals(className) && ("exit".equals(methodName) || "halt".equals(methodName)) || "java.lang.System".equals(className) && "exit".equals(methodName)) {
//              //TODO: perform cleanup
//              break;
//            }
//          }
//        }
//        @Override
//        public void checkPermission(Permission perm) {
//          if(securityManager != null) {
//            securityManager.checkPermission(perm);
//          }
//        }
//      });
//    } catch(Exception e) {
//      e.printStackTrace();
//    }
    Socket socket;
    try {
      socket = serverSocket.accept();
    } catch(Exception e) {
      throw new IllegalStateException("The native side did not receive an incoming connection!");
    }
    Device.DEBUG = Boolean.parseBoolean(System.getProperty("nativeswing.swt.debug.device"));
    DeviceData data = new DeviceData();
    data.debug = Boolean.parseBoolean(System.getProperty("nativeswing.swt.devicedata.debug"));
    data.tracking = Boolean.parseBoolean(System.getProperty("nativeswing.swt.devicedata.tracking"));
    display = new Display(data);
    Display.setAppName("DJ Native Swing");
    messagingInterface = new MessagingInterface(socket, true) {
      @Override
      protected void asyncUIExec(Runnable runnable) {
        display.asyncExec(runnable);
      }
      @Override
      public boolean isUIThread() {
        return Thread.currentThread() == display.getThread();
      }
    };
    while(display != null && !display.isDisposed()) {
      try {
        if(!display.readAndDispatch()) {
          display.sleep();
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
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

}
