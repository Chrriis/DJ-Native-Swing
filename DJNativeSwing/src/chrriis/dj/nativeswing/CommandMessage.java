/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

/**
 * A type of message that executes a command with optional arguments and returns a result.
 * @author Christopher Deckers
 */
public abstract class CommandMessage extends Message {

  /**
   * The arguments, which are accessed directly from within the run method.
   */
  protected Object[] args;
  
  /**
   * Set the arguments that will be used when the message is run.
   * @param args the arguments, which must be serializable.
   */
  public void setArgs(Object... args) {
    if(args.length == 0) {
      args = null;
    }
    this.args = args;
  }
  
  /**
   * Execute that message asynchronously with the given arguments.
   * @param args the arguments, which must be serializable.
   */
  public void asyncExecArgs(Object... args) {
    setArgs(args);
    asyncExec();
  }
  
  /**
   * Execute that message synchronously with the given arguments and return the result.
   * @param args the arguments, which must be serializable.
   * @return the result of the execution.
   */
  public Object syncExecArgs(Object... args) {
    setArgs(args);
    return syncExec();
  }
  
  /**
   * Run the message.
   * @return the result that may be passed back to the caller.
   * @throws Exception any exception that may happen, and which would be passed back if it is a synchronous execution.
   */
  public abstract Object run() throws Exception;
  
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
      sb.append(arg);
    }
    sb.append(')');
    return sb.toString();
  }
  
}
