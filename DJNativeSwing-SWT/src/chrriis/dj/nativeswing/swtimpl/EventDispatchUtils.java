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
   * Sleep but dispatch the events currently in the queue if called from the event dispatch thread, until the timeout is reached.
   * @param timeout The maximum time this processing should take.
   */
  public static void sleepWithEventDispatch(int timeout) {
    sleepWithEventDispatch(new Condition() {
      public boolean getValue() {
        return false;
      }
    }, timeout);
  }

  public static interface Condition {
    public boolean getValue();
  }

  /**
   * Sleep but dispatch the events currently in the queue if called from the event dispatch thread, until the condition becomes true or the timeout is reached.
   * @param condition The condition that indicates whether to stop.
   * @param timeout The maximum time this processing should take.
   */
  public static void sleepWithEventDispatch(Condition condition, int timeout) {
    boolean isEventDispatchThread = SwingUtilities.isEventDispatchThread();
    long time = System.currentTimeMillis();
    while(true) {
      if(condition.getValue() || System.currentTimeMillis() - time > timeout) {
        return;
      }
      if(isEventDispatchThread) {
        new Message().syncSend(true);
        if(condition.getValue() || System.currentTimeMillis() - time > timeout) {
          return;
        }
      }
      try {
        Thread.sleep(50);
      } catch(Exception e) {}
    }
  }

}
