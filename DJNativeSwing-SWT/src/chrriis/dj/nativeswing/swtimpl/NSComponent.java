/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

/**
 * The interface that all Swing wrappers of native components should expose.
 * @author Christopher Deckers
 */
public interface NSComponent {

  /**
   * Force the native peer to initialize. All method calls will then be synchronous instead of being queued waiting for the componant to be initialized.
   * This call fails if the component is not in a component hierarchy with a Window ancestor.
   */
  public void initializeNativePeer();

  /**
   * Explicitely dispose the native resources. This is particularly useful if deferred destruction is used (cf native component options) and the component is not going to be used anymore.
   */
  public void disposeNativePeer();

  /**
   * Indicate whether the native peer is disposed.
   * @return true if the native peer is disposed. This method returns false if the native peer is not initialized.
   */
  public boolean isNativePeerDisposed();

  /**
   * Indicate whether the native peer initialization phase has happened. This method returns true even if the native peer is disposed of if the creation of the peer failed.
   * @return true if the native peer is initialized.
   */
  public boolean isNativePeerInitialized();

  /**
   * Indicate if the native peer is valid, which means initialized, not disposed, and with a communication channel alive.
   * @return true if the native peer is valid.
   */
  public boolean isNativePeerValid();

  /**
   * Run a command in sequence with other method calls from this class. Calls are performed only when the native peer is initialized, and this method adds the command to the queue of calls in case it is not.
   * @param runnable the command to run in sequence with other method calls.
   */
  public void runInSequence(Runnable runnable);

}
