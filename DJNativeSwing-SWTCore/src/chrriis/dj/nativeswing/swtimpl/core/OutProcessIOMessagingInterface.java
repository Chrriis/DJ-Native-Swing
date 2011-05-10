/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.swing.SwingUtilities;

import org.eclipse.swt.widgets.Display;

import chrriis.dj.nativeswing.swtimpl.Message;
import chrriis.dj.nativeswing.swtimpl.NSSystemPropertySWT;

/**
 * @author Christopher Deckers
 */
abstract class OutProcessIOMessagingInterface extends MessagingInterface {

  public OutProcessIOMessagingInterface(boolean isNativeSide, InputStream is, OutputStream os, boolean exitOnEndOfStream, int pid) {
    super(isNativeSide, pid);
    this.is = is;
    this.os = os;
    initialize(exitOnEndOfStream);
  }

  private ObjectOutputStream oos;
  private ObjectInputStream ois;

  @Override
  public void destroy() {
    setAlive(false);
    try {
      oos.close();
    } catch(Exception e) {
    }
    try {
      ois.close();
    } catch(Exception e) {
    }
  }

  private InputStream is;
  private OutputStream os;

  @Override
  protected void openChannel() {
    try {
      oos = new ObjectOutputStream(new BufferedOutputStream(os) {
        @Override
        public synchronized void write(int b) throws IOException {
          super.write(b);
          oosByteCount++;
        }
        @Override
        public synchronized void write(byte[] b, int off, int len) throws IOException {
          super.write(b, off, len);
          oosByteCount += len;
        }
      });
      oos.flush();
      ois = new ObjectInputStream(new BufferedInputStream(is));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void closeChannel() {
    try {
      oos.close();
    } catch(Exception e) {
    }
    try {
      ois.close();
    } catch(Exception e) {
    }
    try {
      is.close();
    } catch(Exception e) {
    }
    is = null;
    try {
      os.close();
    } catch(Exception e) {
    }
    os = null;
  }

  private static final int OOS_RESET_THRESHOLD;

  static {
    String maxByteCountProperty = NSSystemPropertySWT.INTERFACE_STREAMRESETTHRESHOLD.get();
    if(maxByteCountProperty != null) {
      OOS_RESET_THRESHOLD = Integer.parseInt(maxByteCountProperty);
    } else {
      OOS_RESET_THRESHOLD = 500000;
    }
  }

  private int oosByteCount;

  @Override
  protected void writeMessageToChannel(Message message) throws IOException {
    synchronized(oos) {
      oos.writeUnshared(message);
      oos.flush();
      // Messages are cached, so we need to reset() from time to time to clean the cache, or else we get an OutOfMemoryError.
      if(oosByteCount > OOS_RESET_THRESHOLD) {
        oos.reset();
        oosByteCount = 0;
      }
    }
  }

  @Override
  protected Message readMessageFromChannel() throws IOException, ClassNotFoundException {
    Object o = ois.readUnshared();
    if(o instanceof Message) {
      Message message = (Message)o;
      if(IS_DEBUGGING_MESSAGES) {
        System.err.println("RECV: " + SWTNativeInterface.getMessageID(message) + ", " + message);
      }
      return message;
    }
    return null;
  }

  static class SWTOutProcessIOMessagingInterface extends OutProcessIOMessagingInterface {

    private Display display;

    public SWTOutProcessIOMessagingInterface(InputStream is, OutputStream os, final boolean exitOnEndOfStream, Display display, int pid) {
      super(true, is, os, exitOnEndOfStream, pid);
      this.display = display;
    }

    @Override
    protected void asyncUIExec(Runnable runnable) {
      display.asyncExec(runnable);
    }

    @Override
    public boolean isUIThread() {
      return Thread.currentThread() == display.getThread();
    }

  }

  static class SwingOutProcessIOMessagingInterface extends OutProcessIOMessagingInterface {

    private final Process process;

    public SwingOutProcessIOMessagingInterface(InputStream is, OutputStream os, final boolean exitOnEndOfStream, Process process, int pid) {
      super(false, is, os, exitOnEndOfStream, pid);
      this.process = process;
    }

    @Override
    protected void asyncUIExec(Runnable runnable) {
      SwingUtilities.invokeLater(runnable);
    }

    @Override
    public boolean isUIThread() {
      return SwingUtilities.isEventDispatchThread();
    }

    @Override
    public void destroy() {
      super.destroy();
      // It is unclear whether there is any benefit in waiting for the peer VM to be closed (I don't think of any conceptually).
      // However, a user seems to have an issue that is improved when doing so, so let's have this waiting conditional.
      if(process != null && Boolean.parseBoolean(NSSystemPropertySWT.INTERFACE_OUTPROCESS_SYNCCLOSING.get())) {
        while(true) {
          try {
            process.waitFor();
            break;
          } catch (InterruptedException e) {
          }
        }
      }
    }

  }

}
