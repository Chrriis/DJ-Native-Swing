/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import javax.swing.SwingUtilities;

/**
 * A utility class for event dispatching processes.
 * @author Christopher Deckers
 */
public class EventDispatchUtils {

  private EventDispatchUtils() {}
  
  /**
   * Sleep but dispatch the events currently in the Swing queue if called from the event dispatch thread, until the timeout is reached.
   * @param timeout The maximum time this processing should take.
   */
  public static void sleepWithEventDispatch(int timeout) {
    sleepWithEventDispatch(new boolean[1], timeout);
  }
  
  /**
   * Sleep but dispatch the events currently in the Swing queue if called from the event dispatch thread, until the first value of the conditionArray changes to true or the timeout is reached.
   * @param conditionArray The array with at least one cell that indicates whether to stop.
   * @param timeout The maximum time this processing should take.
   */
  public static void sleepWithEventDispatch(boolean[] conditionArray, int timeout) {
    boolean isEventDispatchThread = SwingUtilities.isEventDispatchThread();
    for(long time = System.currentTimeMillis(); !conditionArray[0] && System.currentTimeMillis() - time < timeout; ) {
      if(isEventDispatchThread) {
        new Message().syncSend();
      }
      try {
        Thread.sleep(50);
      } catch(Exception e) {}
    }
  }
  
}
