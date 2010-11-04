/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.netbeans;

import org.openide.util.Lookup;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.internal.NativeCoreObjectFactory;

/**
 * @author Christopher Deckers
 */
public class NativeInterfaceNetBeansHandler {

  private NativeInterfaceNetBeansHandler() {}

  public static void initialize() {
    NativeCoreAPIProvider apiProvider = Lookup.getDefault().lookup(NativeCoreAPIProvider.class);
    NativeCoreObjectFactory.setDefaultFactory(apiProvider.getObjectFactory());
    NativeInterface.initialize();
    if(Utils.IS_MAC) {
      Thread eventPumpThread = new Thread("NativeSwing event pump thread") {
        @Override
        public void run() {
          if(!NativeInterface.isEventPumpRunning()) {
            // This is the Mac case, which uses the executor.
            NativeInterface.runEventPump();
          }
        }
      };
      eventPumpThread.setDaemon(true);
      eventPumpThread.start();
    }
  }

}
