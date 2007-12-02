/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui.event;

import java.awt.Component;
import java.util.EventObject;

/**
 * @author Christopher Deckers
 */
public class InitializationEvent extends EventObject {

  protected Component component;
  
  public InitializationEvent(Component component) {
    super(component);
  }
  
  public Component getComponent() {
    return component;
  }
  
}
