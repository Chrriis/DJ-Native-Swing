/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import org.mozilla.interfaces.nsIComponentManager;
import org.mozilla.interfaces.nsIComponentRegistrar;
import org.mozilla.interfaces.nsIServiceManager;
import org.mozilla.interfaces.nsIWebBrowser;
import org.mozilla.xpcom.XPCOMInitializationException;

import chrriis.dj.nativeswing.swtimpl.CommandMessage;
import chrriis.dj.nativeswing.swtimpl.components.internal.INativeMozillaXPCOM;
import chrriis.dj.nativeswing.swtimpl.internal.NativeCoreObjectFactory;

/**
 * This class is meant to allow accessing the JavaXPCOM nsIWebBrowser interface, and other Mozilla XPCOM interfaces.
 * @author Christopher Deckers
 */
public class MozillaXPCOM {

  private static INativeMozillaXPCOM nativeMozillaXPCOM = NativeCoreObjectFactory.create(INativeMozillaXPCOM.class, "chrriis.dj.nativeswing.swtimpl.components.core.NativeMozillaXPCOM", new Class<?>[0], new Object[0]);

  /**
   * A class that gives access to the functionalities of <code>org.mozilla.xpcom.Mozilla</code>.
   * @author Christopher Deckers
   */
  public static class Mozilla {

    private static boolean isInitialized;

    /**
     * @return false is the initialization did not occur.
     */
    private static boolean initialize() {
      if(isInitialized) {
        return false;
      }
      isInitialized = true;
      return nativeMozillaXPCOM.initialize();
    }

    private static class CMN_getComponentRegistrar extends CommandMessage {
      @Override
      public Object run(Object[] args) {
        try {
          return pack(org.mozilla.xpcom.Mozilla.getInstance().getComponentRegistrar(), true);
        } catch (XPCOMInitializationException e) {
          if(!initialize()) {
            throw e;
          }
          return pack(org.mozilla.xpcom.Mozilla.getInstance().getComponentRegistrar(), true);
        }
      }
    }

    /**
     * Get the Mozilla JavaXPCOM component registrar.
     * @return the Mozilla JavaXPCOM component registrar.
     */
    public static nsIComponentRegistrar getComponentRegistrar() {
      return (nsIComponentRegistrar)unpack(new CMN_getComponentRegistrar().syncExec(true));
    }

    private static class CMN_getComponentManager extends CommandMessage {
      @Override
      public Object run(Object[] args) {
        try {
          return pack(org.mozilla.xpcom.Mozilla.getInstance().getComponentManager(), true);
        } catch (XPCOMInitializationException e) {
          if(!initialize()) {
            throw e;
          }
          return pack(org.mozilla.xpcom.Mozilla.getInstance().getComponentManager(), true);
        }
      }
    }

    /**
     * Get the Mozilla JavaXPCOM component manager.
     * @return the Mozilla JavaXPCOM component manager.
     */
    public static nsIComponentManager getComponentManager() {
      return (nsIComponentManager)unpack(new CMN_getComponentManager().syncExec(true));
    }

    private static class CMN_getServiceManager extends CommandMessage {
      @Override
      public Object run(Object[] args) {
        try {
          return pack(org.mozilla.xpcom.Mozilla.getInstance().getServiceManager(), true);
        } catch (XPCOMInitializationException e) {
          if(!initialize()) {
            throw e;
          }
          return pack(org.mozilla.xpcom.Mozilla.getInstance().getServiceManager(), true);
        }
      }
    }

    /**
     * Get the Mozilla JavaXPCOM service manager.
     * @return the Mozilla JavaXPCOM service manager.
     */
    public static nsIServiceManager getServiceManager() {
      return (nsIServiceManager)unpack(new CMN_getServiceManager().syncExec(true));
    }

  }

  private MozillaXPCOM() {}

  /**
   * Get the Mozilla JavaXPCOM nsIWebBrowser if it is available.<br/>
   * Availability requires the web browser to be using the XULRunner runtime (version 1.8.1.2 or greater) and the JavaXPCOM classes (version 1.8.1.2 or greater) to be in the classpath.
   * @return the Mozilla JavaXPCOM nsIWebBrowser, or null if it is not available.
   */
  public static nsIWebBrowser getWebBrowser(JWebBrowser webBrowser) {
    return (nsIWebBrowser)nativeMozillaXPCOM.getWebBrowser(webBrowser);
  }

  private static Object pack(Object o, boolean isNativeSide) {
    return nativeMozillaXPCOM.pack(o, isNativeSide);
  }

  private static Object unpack(Object o) {
    return nativeMozillaXPCOM.unpack(o);
  }

}
