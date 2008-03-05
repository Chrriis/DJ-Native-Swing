/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

/**
 * A local message is not sent through the messaging interface. It is usually used to place an action in a sequence of messages.
 * @author Christopher Deckers
 */
public abstract class LocalMessage extends CommandMessage {

  public abstract Object run();

}
