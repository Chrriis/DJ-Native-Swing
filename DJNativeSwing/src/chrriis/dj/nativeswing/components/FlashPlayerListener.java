/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components;

import java.util.EventListener;

/**
 * @author Christopher Deckers
 */
public interface FlashPlayerListener extends EventListener {

  public void commandReceived(String command, String[] args);
  
}
