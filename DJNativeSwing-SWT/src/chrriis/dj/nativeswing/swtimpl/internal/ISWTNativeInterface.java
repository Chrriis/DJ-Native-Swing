/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.internal;

import java.io.PrintStream;
import java.io.PrintWriter;

import chrriis.dj.nativeswing.swtimpl.Message;
import chrriis.dj.nativeswing.swtimpl.NativeInterfaceConfiguration;
import chrriis.dj.nativeswing.swtimpl.NativeInterfaceListener;

/**
 * The native interface, which establishes the link between the peer VM (native side) and the local side.
 * @author Christopher Deckers
 */
public interface ISWTNativeInterface {

  public boolean isOpen_();

  public void close_();

  public NativeInterfaceConfiguration getConfiguration_();

  public boolean isInitialized_();

  public boolean isInProcess_();

  public void initialize_();

  public void printStackTraces_();

  public void printStackTraces_(PrintStream printStream);

  public void printStackTraces_(PrintWriter printWriter);

  public void open_();

  public Object syncSend_(boolean isTargetNativeSide, final Message message);

  public void asyncSend_(boolean isTargetNativeSide, final Message message);

  public boolean isOutProcessNativeSide_();

  /**
   * Indicate if the current thread is the user interface thread.
   * @return true if the current thread is the user interface thread.
   * @throws IllegalStateException when the native interface is not alive.
   */
  public boolean isUIThread_(boolean isNativeSide);

  public void runEventPump_();

  public boolean isEventPumpRunning_();

  public void addNativeInterfaceListener_(NativeInterfaceListener listener);

  public void removeNativeInterfaceListener_(NativeInterfaceListener listener);

  public NativeInterfaceListener[] getNativeInterfaceListeners_();

  public void main_(String[] args) throws Exception;

}
