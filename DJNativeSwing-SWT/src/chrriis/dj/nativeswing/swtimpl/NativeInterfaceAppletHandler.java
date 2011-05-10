/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import java.applet.Applet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import chrriis.common.WebServer;
import chrriis.dj.nativeswing.NSSystemProperty;

/**
 * A special helper that allows Native Swing to work in a JApplet.<br/>
 * <br/>
 * Here is how to use it:<br/>
 * <br/>
 * 1. Add a static initializer at the top of the JApplet subclass:
 * <code><pre>static {
 *  NativeInterfaceAppletHandler.activateAppletMode();
 *}</pre></code>
 * <br/>
 * 2. Override all applet lifecycle methods to call a corresponding method of the NativeInterfaceAppletHandler as the first statement.
 * <code><pre>public void init() {
 *  NativeInterfaceAppletHandler.init(this);
 *  // Rest of init().
 *}
 *public void start() {
 *  NativeInterfaceAppletHandler.start(this);
 *  // Rest of start().
 *}
 *public void stop() {
 *  NativeInterfaceAppletHandler.stop(this);
 *  // Rest of stop().
 *}
 *public void destroy() {
 *  NativeInterfaceAppletHandler.destroy(this);
 *  // Rest of destroy().
 *}</pre></code>
 * <br/>
 * 3. Open the interface explicitly.<br/>
 * While the methods of this class do close the interface and reopen it when re-starting if it was previously open, it does not automatically open it. In most cases, one would probably change the start() method like this:
 * <code><pre>public void start() {
 *  NativeInterfaceAppletHandler.start(this);
 *  NativeInterface.open();
 *  // Rest of start().
 *}</pre></code>
 * @author Christopher Deckers
 */
public class NativeInterfaceAppletHandler {

  private NativeInterfaceAppletHandler() {}

  private static final Object INITIALIZATION_LOCK = new Object();
  private static boolean isInterfaceToOpen;
  private static Set<Applet> activeAppletSet = new HashSet<Applet>();

  public static void activateAppletMode() {
    NSSystemProperty.DEPLOYMENT_TYPE.set("applet");
    if(NativeInterface.isInProcess()) {
      final AtomicBoolean isInitialized = new AtomicBoolean(false);
      synchronized(isInitialized) {
        Thread eventPumpThread = new Thread("NativeSwing event pump thread") {
          @Override
          public void run() {
            try {
              NativeInterface.initialize();
            } finally {
              isInitialized.set(true);
              synchronized (isInitialized) {
                isInitialized.notify();
              }
            }
            if(!NativeInterface.isEventPumpRunning()) {
              NativeInterface.runEventPump();
            }
          }
        };
        eventPumpThread.setDaemon(true);
        eventPumpThread.start();
        while(!isInitialized.get()) {
          try {
            isInitialized.wait();
          } catch (InterruptedException e) {
          }
        }
      }
    } else {
      NativeInterface.initialize();
    }
  }

  public static void init(Applet applet) {
    checkAppletMode();
  }

  public static void start(Applet applet) {
    checkAppletMode();
    synchronized(INITIALIZATION_LOCK) {
      activeAppletSet.add(applet);
      if(isInterfaceToOpen) {
        NativeInterface.open();
      }
    }
  }

  public static void stop(Applet applet) {
    checkAppletMode();
    synchronized(INITIALIZATION_LOCK) {
      activeAppletSet.remove(applet);
      if(activeAppletSet.isEmpty()) {
        isInterfaceToOpen = NativeInterface.isOpen();
        stopActivity();
      }
    }
  }

  public static void destroy(Applet applet) {
    checkAppletMode();
    synchronized(INITIALIZATION_LOCK) {
      activeAppletSet.remove(applet);
      if(activeAppletSet.isEmpty()) {
        isInterfaceToOpen = false;
        stopActivity();
      }
    }
  }

  private static void stopActivity() {
    NativeInterface.close();
    WebServer.stopDefaultWebServer();
  }

  private static void checkAppletMode() {
    if(!"applet".equals(NSSystemProperty.DEPLOYMENT_TYPE.get())) {
      throw new IllegalStateException(NativeInterfaceAppletHandler.class.getName() + ".activateAppletMode() was not called! This code has to be placed first in the applet subclass in a static initializer.");
    }
  }

}
