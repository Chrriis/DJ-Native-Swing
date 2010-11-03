/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import chrriis.common.Utils;

/**
 * A type of message that executes a command with optional arguments and returns a result.
 * @author Christopher Deckers
 */
public abstract class CommandMessage extends Message {

  /**
   * Construct a command message.
   */
  public CommandMessage() {
  }

  private Object[] args;

  /**
   * Set the arguments that will be used when the message is run.
   * @param args the arguments, which must be serializable.
   */
  void setArgs(Object... args) {
    if(args.length == 0) {
      args = null;
    }
    this.args = args;
  }

  /**
   * Execute that message asynchronously with the given arguments.
   * @param isTargetNativeSide true if the target is the native side, false otherwise.
   * @param args the arguments, which must be serializable.
   */
  public void asyncExec(boolean isTargetNativeSide, Object... args) {
    setArgs(args);
    asyncSend(isTargetNativeSide);
  }

  /**
   * Execute that message synchronously with the given arguments and return the result.
   * @param isTargetNativeSide true if the target is the native side, false otherwise.
   * @param args the arguments, which must be serializable.
   * @return the result of the execution.
   */
  public Object syncExec(boolean isTargetNativeSide, Object... args) {
    setArgs(args);
    return syncSend(isTargetNativeSide);
  }

  private static final Object[] EMPTY_ARGS = new Object[0];

  protected Object runCommand() throws Exception {
    return run(args == null? EMPTY_ARGS: args);
  }

  /**
   * Run the message.
   * @param args the arguments that were specified for that command, or an empty array if none were specified.
   * @return the result that may be passed back to the caller.
   * @throws Exception any exception that may happen, and which would be passed back if it is a synchronous execution.
   */
  public abstract Object run(Object[] args) throws Exception;

  @Override
  public String toString() {
    String s = super.toString();
    if(args == null || args.length == 0) {
      return s + "()";
    }
    StringBuilder sb = new StringBuilder();
    sb.append(s).append('(');
    for(int i=0; i<args.length; i++) {
      Object arg = args[i];
      if(i > 0) {
        sb.append(", ");
      }
      if(arg != null && arg.getClass().isArray()) {
        sb.append(Utils.arrayDeepToString(arg));
      } else {
        sb.append(arg);
      }
    }
    sb.append(')');
    return sb.toString();
  }

}
