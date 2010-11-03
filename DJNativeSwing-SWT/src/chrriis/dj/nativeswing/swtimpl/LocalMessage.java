/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

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

  @Override
  protected Object runCommand() {
    try {
      return super.runCommand();
    } catch(RuntimeException e) {
      throw e;
    } catch(Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Run the message.
   * @return the result.
   */
  @Override
  public abstract Object run(Object[] args);

}
