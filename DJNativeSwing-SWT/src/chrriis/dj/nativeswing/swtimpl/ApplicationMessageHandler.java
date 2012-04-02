/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

/**
 * @author Christopher Deckers
 */
public interface ApplicationMessageHandler {

  /**
   * Handle a global application quit event, like when clicking on the Mac quit menu.
   */
  public void handleQuit();
  
}
