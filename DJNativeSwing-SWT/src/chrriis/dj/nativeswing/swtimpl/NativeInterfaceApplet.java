/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import javax.swing.JApplet;

import chrriis.common.WebServer;

/**
 * @author Christopher Deckers
 */
public class NativeInterfaceApplet extends JApplet {

  static {
    System.setProperty("nativeswing.deployment.type", "applet");
  }

  private static final Object INITIALIZATION_LOCK = new Object();
  private static int count;

  @Override
  public void init() {
    synchronized(INITIALIZATION_LOCK) {
      if(count == 0) {
        NativeInterface.initialize();
      }
      count++;
    }
  }

  @Override
  public void stop() {
    synchronized(INITIALIZATION_LOCK) {
      count--;
      if(count == 0) {
        NativeInterface.close();
        WebServer.stopDefaultWebServer();
      }
    }
  }

}
