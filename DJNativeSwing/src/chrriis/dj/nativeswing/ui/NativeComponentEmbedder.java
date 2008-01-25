/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.Component;

/**
 * @author Christopher Deckers
 */
public class NativeComponentEmbedder {

  public static enum Layering {
    NO_LAYERING,
    COMPONENT_LAYERING,
    WINDOW_LAYERING,
  }
  
  protected NativeComponentEmbedder() {}
  
  protected static Layering preferredLayering = Layering.NO_LAYERING;
  
  public static void setPreferredLayering(Layering preferredLayering) {
    if(preferredLayering == null) {
      preferredLayering = Layering.NO_LAYERING;
    }
    NativeComponentEmbedder.preferredLayering = preferredLayering;
  }
  
  /**
   * Get the layering, taking into account the preferred layering as well as the availability of the necessary libraries.
   */
  public static Layering getLayering() {
    switch(preferredLayering) {
      case COMPONENT_LAYERING:
      case WINDOW_LAYERING:
        try {
          Class.forName("com.sun.jna.examples.WindowUtils");
        } catch(Exception e) {
          return Layering.NO_LAYERING;
        }
    }
    return preferredLayering;
  }
  
  public static Component getEmbeddedComponent(NativeComponent nativeComponent) {
    switch(getLayering()) {
      case COMPONENT_LAYERING:
        return new NativeComponentProxyPanel(nativeComponent);
      case WINDOW_LAYERING:
        return new NativeComponentProxyWindow(nativeComponent);
      default:
        return nativeComponent;
    }
  }
  
}
