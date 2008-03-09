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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;

import org.eclipse.swt.widgets.Display;

import chrriis.common.NetworkURLClassLoader;
import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.dj.nativeswing.ui.NativeComponent;

/**
 * @author Christopher Deckers
 */
public class NativeInterfaceHandler {

  public static class NativeInterfaceInitOptions {
    
    private boolean isNativeSideRespawnedOnError = true;
    private boolean isPreferredLookAndFeelApplied;
    private Class<?>[] nativeClassPathReferenceClasses;
    private String[] nativeClassPathReferenceResources;
    private String[] peerVMParams;
    
    public void setNativeSideRespawnedOnError(boolean isNativeSideRespawnedOnError) {
      this.isNativeSideRespawnedOnError = isNativeSideRespawnedOnError;
    }
    
    public boolean isNativeSideRespawnedOnError() {
      return isNativeSideRespawnedOnError;
    }
    
    public void setPreferredLookAndFeelApplied(boolean isPreferredLookAndFeelApplied) {
      this.isPreferredLookAndFeelApplied = isPreferredLookAndFeelApplied;
    }
    
    public boolean isPreferredLookAndFeelApplied() {
      return isPreferredLookAndFeelApplied;
    }
    
    public void setNativeClassPathReferenceClasses(Class<?>[] nativeClassPathReferenceClasses) {
      this.nativeClassPathReferenceClasses = nativeClassPathReferenceClasses;
    }
    
    public Class<?>[] getNativeClassPathReferenceClasses() {
      return nativeClassPathReferenceClasses;
    }
    
    public void setNativeClassPathReferenceResources(String[] nativeClassPathReferenceResources) {
      this.nativeClassPathReferenceResources = nativeClassPathReferenceResources;
    }
    
    public String[] getNativeClassPathReferenceResources() {
      return nativeClassPathReferenceResources;
    }
    
    public void setPeerVMParams(String... peerVMParams) {
      this.peerVMParams = peerVMParams;
    }
    
    public String[] getPeerVMParams() {
      return peerVMParams;
    }
    
  }
  
  /**
   * This class is not part of the public API.
   * @author Christopher Deckers
   */
  public static class _Internal_ {
    
    private static volatile List<Canvas> canvasList;

    public static Canvas[] getCanvas() {
      return canvasList.toArray(new Canvas[0]);
    }
    
    public static void addCanvas(Canvas canvas) {
      canvasList.add(canvas);
    }
    
    public static void removeCanvas(Canvas canvas) {
      canvasList.remove(canvas);
    }
    
    private static Set<Window> windowSet;
    
    public static Window[] getWindows() {
      if(Utils.IS_JAVA_6_OR_GREATER) {
        return Window.getWindows();
      }
      return windowSet == null? new Window[0]: windowSet.toArray(new Window[0]);
    }
    
    public static boolean isInterfaceAlive() {
      return messagingInterface.isAlive();
    }
    
  }
  
  private static boolean isInitialized;
  
  private static boolean isInitialized() {
    return isInitialized;
  }
  
  private static void checkInitialized() {
    if(!isInitialized()) {
      throw new IllegalStateException("The native interface handler is not initialized! Please refer to the instructions to set it up properly.");
    }
  }
  
  public static void init() {
    init(new NativeInterfaceInitOptions());
  }
  
  private static class CMN_setProperties extends CommandMessage {
    @Override
    public Object run() throws Exception {
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

  private static NativeInterfaceInitOptions nativeInterfaceInitOptions;
  
  public static void init(NativeInterfaceInitOptions nativeInterfaceInitOptions) {
    if(isInitialized()) {
      return;
    }
    NativeInterfaceHandler.nativeInterfaceInitOptions = nativeInterfaceInitOptions;
    isInitialized = true;
    if(nativeInterfaceInitOptions.isPreferredLookAndFeelApplied()) {
      setPreferredLookAndFeel();
    }
    _Internal_.canvasList = new ArrayList<Canvas>();
    // Specific Sun property to prevent heavyweight components from erasing their background.
    System.setProperty("sun.awt.noerasebackground", "true");
    // It seems on Linux this is required to get the component visible.
    System.setProperty("sun.awt.xembedserver", "true");
    // Remove all lightweight windows, to avoid wrong overlaps
    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    System.setProperty("JPopupMenu.defaultLWPopupEnabledKey", "false");
    // We use our own HW forcing, so we disable the one from JNA
    System.setProperty("jna.force_hw_popups", "false");
    createCommunicationChannel();
    // Create window monitor
    Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
      protected Set<Dialog> dialogSet = new HashSet<Dialog>();
      protected volatile Set<Window> blockedWindowSet = new HashSet<Window>();
      protected void adjustNativeComponents() {
        for(int i=_Internal_.canvasList.size()-1; i>=0; i--) {
          final Canvas canvas = _Internal_.canvasList.get(i);
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
          if(canvas instanceof NativeComponent) {
            ((NativeComponent)canvas).setShellEnabled(!isBlocked && isShowing);
          }
          boolean hasFocus = canvas.hasFocus();
          if(!isShowing && hasFocus) {
            canvas.transferFocus();
          }
        }
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
          if(_Internal_.windowSet == null) {
            _Internal_.windowSet = new HashSet<Window>();
          }
          switch(e.getID()) {
            case WindowEvent.WINDOW_OPENED:
            case ComponentEvent.COMPONENT_SHOWN:
              _Internal_.windowSet.add((Window)e.getSource());
              break;
            case WindowEvent.WINDOW_CLOSED:
            case ComponentEvent.COMPONENT_HIDDEN:
              _Internal_.windowSet.remove(e.getSource());
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
  }
  
  static void createCommunicationChannel() {
    if(messagingInterface != null && !nativeInterfaceInitOptions.isNativeSideRespawnedOnError()) {
      return;
    }
    // Create the interface to communicate with the process handling the native side
    messagingInterface = createMessagingInterface(nativeInterfaceInitOptions);
    // Set the system properties
    new CMN_setProperties().syncExecArgs(System.getProperties());
  }
  
  private static Process createProcess(NativeInterfaceInitOptions nativeInterfaceInitOptions, int port) {
    List<String> classPathList = new ArrayList<String>();
    String pathSeparator = System.getProperty("path.separator");
    List<Object> referenceList = new ArrayList<Object>();
    referenceList.add(NativeInterfaceHandler.class);
    referenceList.add("org/eclipse/swt/widgets/Display.class");
    Class<?>[] nativeClassPathReferenceClasses = nativeInterfaceInitOptions.getNativeClassPathReferenceClasses();
    if(nativeClassPathReferenceClasses != null) {
      referenceList.addAll(Arrays.asList(nativeClassPathReferenceClasses));
    }
    String[] nativeClassPathReferenceResources = nativeInterfaceInitOptions.getNativeClassPathReferenceResources();
    if(nativeClassPathReferenceResources != null) {
      referenceList.addAll(Arrays.asList(nativeClassPathReferenceResources));
    }
    boolean isProxyClassLoaderUsed = false;
    for(Object o: referenceList) {
      File clazzClassPath;
      if(o instanceof Class) {
        clazzClassPath = Utils.getClassPathFile((Class<?>)o);
      } else {
        clazzClassPath = Utils.getClassPathFile((String)o);
        if(NativeInterfaceHandler.class.getResource("/" + o) == null) {
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
          BufferedInputStream in = new BufferedInputStream(NativeInterfaceHandler.class.getResourceAsStream("/" + classPath));
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
    if(nativeInterfaceInitOptions.peerVMParams != null) {
      for(String param: nativeInterfaceInitOptions.peerVMParams) {
        argList.add(param);
      }
    }
    argList.add("-Ddj.nativeswing.messaging.debug=" + Boolean.parseBoolean(System.getProperty("dj.nativeswing.messaging.debug")));
    argList.add("-classpath");
    argList.add(sb.toString());
    if(isProxyClassLoaderUsed) {
      argList.add(NetworkURLClassLoader.class.getName());
      argList.add(WebServer.getDefaultWebServer().getClassPathResourceURL("", ""));
    }
    argList.add(NativeInterfaceHandler.class.getName());
    argList.add(String.valueOf(port));
    // Try these arguments with the various candidate binaries.
    for(String candidateBinary: candidateBinaries) {
      argList.set(0, candidateBinary);
      if(Boolean.parseBoolean(System.getProperty("dj.nativeswing.native.commandline"))) {
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
  
  private static MessagingInterface createMessagingInterface(NativeInterfaceInitOptions nativeInterfaceInitOptions) {
    int port = Integer.parseInt(System.getProperty("dj.nativeswing.messaging.port", "-1"));
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
    if(Boolean.parseBoolean(System.getProperty("dj.nativeswing.messaging.nocreateprocess"))) {
      p = null;
    } else {
      p = createProcess(nativeInterfaceInitOptions, port);
    }
    Socket socket = null;
    for(int i=24; i>=0; i--) {
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
          String lineSeparator = System.getProperty("line.separator");
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
            final byte[] byteArray = baos.toByteArray();
            // Flushing directly to the out stream freezes in Webstart.
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                synchronized (out) {
                  try {
                    out.write(byteArray);
                  } catch(Exception e) {
                    e.printStackTrace();
                  }
                }
              }
            });
          }
        } catch(Exception e) {
        }
      }
    };
    streamThread.setDaemon(true);
    streamThread.start();
  }
  
  private NativeInterfaceHandler() {}
  
  private static volatile MessagingInterface messagingInterface;

  static MessagingInterface getMessagingInterface() {
    return messagingInterface;
  }
  
  public static Object syncExec(final Message message) {
    checkInitialized();
    if(message instanceof LocalMessage) {
      return ((LocalMessage)message).run();
    }
    return messagingInterface.syncExec(message);
  }
  
  public static void asyncExec(final Message message) {
    checkInitialized();
    if(message instanceof LocalMessage) {
      ((LocalMessage)message).run();
      return;
    }
    messagingInterface.asyncExec(message);
  }
  
  private static void setPreferredLookAndFeel() {
    try {
      String systemLookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
      if(!"com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(systemLookAndFeelClassName)) {
        UIManager.setLookAndFeel(systemLookAndFeelClassName);
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  private static Display display;
  
  /**
   * Only possible when in the native context
   */
  public static Display getDisplay() {
    return display;
  }
  
  public static boolean isNativeSide() {
    return display != null;
  }
  
  public static void checkUIThread() {
    messagingInterface.checkUIThread();
  }
  
  public static void main(String[] args) throws Exception {
    isInitialized = true;
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
    if(!Boolean.parseBoolean(System.getProperty("dj.nativeswing.native.keepalive"))) {
      Thread shutdownThread = new Thread("NativeSwing Shutdown") {
        @Override
        public void run() {
          try {
            sleep(5000);
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
              System.err.println("cleanup");
              //TODO: perform cleanup
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
    Socket socket;
    try {
      socket = serverSocket.accept();
    } catch(Exception e) {
      throw new IllegalStateException("The native side did not receive an incoming connection!");
    }
    display = new Display();
    final Thread uiThread = Thread.currentThread();
    messagingInterface = new MessagingInterface(socket, true) {
      @Override
      protected void asyncUIExec(Runnable runnable) {
        display.asyncExec(runnable);
      }
      @Override
      public boolean isUIThread() {
        return Thread.currentThread() == uiThread;
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
  
  public static interface NativeInterfaceListener extends EventListener {
    public void nativeInterfaceRestarted();
  }

  private static EventListenerList listenerList = new EventListenerList();
  
  public static void addNativeInterfaceListener(NativeInterfaceListener listener) {
    listenerList.add(NativeInterfaceListener.class, listener);
  }
  
  public static void removeNativeInterfaceListener(NativeInterfaceListener listener) {
    listenerList.remove(NativeInterfaceListener.class, listener);
  }
  
  public static NativeInterfaceListener[] getNativeInterfaceListeners() {
    return listenerList.getListeners(NativeInterfaceListener.class);
  }

}
