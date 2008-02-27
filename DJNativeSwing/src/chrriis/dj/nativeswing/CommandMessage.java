/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

/**
 * @author Christopher Deckers
 */
public abstract class CommandMessage extends Message {

  protected Object[] args;
  
  public void setArgs(Object... args) {
    this.args = args;
  }
  
  public CommandMessage(Object... args) {
    this.args = args;
  }
  
  public void asyncExecArgs(Object... args) {
    setArgs(args);
    asyncExec();
  }
  
  public Object syncExecArgs(Object... args) {
    setArgs(args);
    return syncExec();
  }
  
  public abstract Object run() throws Exception;
  
  @Override
  public String toString() {
    if(args == null || args.length == 0) {
      return getClass().getName() + "()";
    }
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getName()).append('(');
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
