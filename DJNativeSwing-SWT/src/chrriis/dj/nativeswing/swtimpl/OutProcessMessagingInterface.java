/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.SwingUtilities;

import org.eclipse.swt.widgets.Display;

/**
 * @author Christopher Deckers
 */
abstract class OutProcessMessagingInterface extends MessagingInterface {

  public OutProcessMessagingInterface(Socket socket, boolean exitOnEndOfStream) {
    this.socket = socket;
    initialize(exitOnEndOfStream);
  }
  
  private ObjectOutputStream oos;
  private ObjectInputStream ois;

  public void destroy() {
    setAlive(false);
    try {
      ois.close();
    } catch(Exception e) {
    }
  }
  
  private Socket socket;
  
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
    String maxByteCountProperty = System.getProperty("nativeswing.interface.streamresetthreshold");
    if(maxByteCountProperty != null) {
      OOS_RESET_THRESHOLD = Integer.parseInt(maxByteCountProperty);
    } else {
      OOS_RESET_THRESHOLD = 500000;
    }
  }
  
  private int oosByteCount;
  
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
  
  protected Message readMessageFromChannel() throws IOException, ClassNotFoundException {
    Object o = OutProcessMessagingInterface.this.ois.readUnshared();
    if(o instanceof Message) {
      Message message = (Message)o;
      if(IS_DEBUGGING_MESSAGES) {
        System.err.println("RECV: " + message.getID() + ", " + message);
      }
      return message;
    }
    System.err.println("Unknown message: " + o);
    return null;
  }
  
  static class SWTOutProcessMessagingInterface extends OutProcessMessagingInterface {
    
    private Display display;
    
    public SWTOutProcessMessagingInterface(Socket socket, final boolean exitOnEndOfStream, Display display) {
      super(socket, exitOnEndOfStream);
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
  
  static class SwingOutProcessMessagingInterface extends OutProcessMessagingInterface {
    
    public SwingOutProcessMessagingInterface(Socket socket, final boolean exitOnEndOfStream) {
      super(socket, exitOnEndOfStream);
    }
    
    @Override
    protected void asyncUIExec(Runnable runnable) {
      SwingUtilities.invokeLater(runnable);
    }
    
    @Override
    public boolean isUIThread() {
      return SwingUtilities.isEventDispatchThread();
    }

  }
  
}
