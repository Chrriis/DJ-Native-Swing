/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui.event;

import java.util.EventObject;

import chrriis.dj.nativeswing.ui.JFlashPlayer;

/**
 * @author Christopher Deckers
 */
public class FlashPlayerEvent extends EventObject{

  protected JFlashPlayer flashPlayer;
  
  public FlashPlayerEvent(JFlashPlayer flashPlayer) {
    super(flashPlayer);
    this.flashPlayer = flashPlayer;
  }
  
  public JFlashPlayer getFlashPlayer() {
    return flashPlayer;
  }
  
}
