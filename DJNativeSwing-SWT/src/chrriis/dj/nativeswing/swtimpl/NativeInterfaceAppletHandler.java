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

import chrriis.common.WebServer;

/**
 * @author Christopher Deckers
 */
public class NativeInterfaceAppletHandler {

  private NativeInterfaceAppletHandler() {}

  private static final Object INITIALIZATION_LOCK = new Object();
  private static boolean isInterfaceToOpen;
  private static Set<Applet> activeAppletSet = new HashSet<Applet>();

  public static void activateAppletMode() {
    checkAppletMode();
    System.setProperty("nativeswing.deployment.type", "applet");
    NativeInterface.initialize();
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
    if(!"applet".equals(System.getProperty("nativeswing.deployment.type"))) {
      throw new IllegalStateException(NativeInterfaceAppletHandler.class.getName() + ".activateAppletMode() was not called! This code has to be placed first in the applet subclass in a static initializer.");
    }
  }

}
