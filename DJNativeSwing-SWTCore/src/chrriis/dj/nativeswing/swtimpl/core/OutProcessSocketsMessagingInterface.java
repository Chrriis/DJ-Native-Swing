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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.SwingUtilities;

import org.eclipse.swt.widgets.Display;

import chrriis.dj.nativeswing.swtimpl.Message;
import chrriis.dj.nativeswing.swtimpl.NSSystemPropertySWT;

/**
 * @author Christopher Deckers
 */
abstract class OutProcessSocketsMessagingInterface extends MessagingInterface {

  public OutProcessSocketsMessagingInterface(boolean isNativeSide, Socket socket, boolean exitOnEndOfStream, int pid) {
    super(isNativeSide, pid);
    this.socket = socket;
    initialize(exitOnEndOfStream);
  }

  private ObjectOutputStream oos;
  private ObjectInputStream ois;

  @Override
  public void destroy() {
    setAlive(false);
    try {
      ois.close();
    } catch(Exception e) {
    }
  }

  private Socket socket;

  @Override
  protected void openChannel() {
    try {
      oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()) {
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
      ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
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
      socket.close();
    } catch(Exception e) {
    }
    socket = null;
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
    System.err.println("Unknown message: " + o);
    return null;
  }

  static class SWTOutProcessSocketsMessagingInterface extends OutProcessSocketsMessagingInterface {

    private Display display;

    public SWTOutProcessSocketsMessagingInterface(Socket socket, final boolean exitOnEndOfStream, Display display, int pid) {
      super(true, socket, exitOnEndOfStream, pid);
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

    @Override
    protected void terminate() {
      if(isNativeSide() && Boolean.parseBoolean(NSSystemPropertySWT.PEERVM_DEBUG_PRINTSTOPMESSAGE.get())) {
        System.err.println("Stopping peer VM #" + getPID());
      }
      super.terminate();
    }

  }

  static class SwingOutProcessSocketsMessagingInterface extends OutProcessSocketsMessagingInterface {

    private final Process process;

    public SwingOutProcessSocketsMessagingInterface(Socket socket, final boolean exitOnEndOfStream, Process process, int pid) {
      super(false, socket, exitOnEndOfStream, pid);
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
