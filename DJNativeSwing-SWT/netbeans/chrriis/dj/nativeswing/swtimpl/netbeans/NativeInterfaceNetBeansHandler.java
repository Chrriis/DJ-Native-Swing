/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.netbeans;

import java.util.concurrent.atomic.AtomicBoolean;

import org.openide.util.Lookup;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.internal.NativeCoreObjectFactory;

/**
 * A special helper that allows Native Swing to work as a NetBeans module.<br/>
 * @author Christopher Deckers
 */
public class NativeInterfaceNetBeansHandler {

  private NativeInterfaceNetBeansHandler() {}

  public static void initialize() {
    NativeCoreAPIProvider apiProvider = Lookup.getDefault().lookup(NativeCoreAPIProvider.class);
    NativeCoreObjectFactory.setDefaultFactory(apiProvider.getObjectFactory());
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

}
