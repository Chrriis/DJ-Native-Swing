/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

/**
 * A local message is a special message that is not sent through the messaging interface. It is normally used to sequence a local command among remote commands.
 * @author Christopher Deckers
 */
public abstract class LocalMessage extends CommandMessage {

  /**
   * Construct a local message.
   */
  public LocalMessage() {
  }
  
  /**
   * Run the message.
   * @return the result.
   */
  public abstract Object run();

}
