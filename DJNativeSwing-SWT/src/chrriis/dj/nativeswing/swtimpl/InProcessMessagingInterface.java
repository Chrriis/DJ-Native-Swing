/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.eclipse.swt.widgets.Display;

/**
 * @author Christopher Deckers
 */
abstract class InProcessMessagingInterface extends MessagingInterface {

  public InProcessMessagingInterface(boolean isNativeSide) {
    super(isNativeSide);
  }
  
  public void destroy() {
    setAlive(false);
  }
  
  @Override
  protected void openChannel() {
  }
  
  @Override
  protected void closeChannel() {
  }
  
  private volatile InProcessMessagingInterface mirrorMessagingInterface;
  
  protected void setMirrorMessagingInterface(InProcessMessagingInterface mirrorMessagingInterface) {
    this.mirrorMessagingInterface = mirrorMessagingInterface;
  }
  
  public InProcessMessagingInterface getMirrorMessagingInterface() {
    return mirrorMessagingInterface;
  }
  
  private List<Message> sentMessageList = new LinkedList<Message>();
  
  Message getNextMessage() {
    synchronized (sentMessageList) {
      while(sentMessageList.isEmpty()) {
        try {
          sentMessageList.wait();
        } catch(InterruptedException e) {
        }
      }
      return sentMessageList.remove(0);
    }
  }
  
  @Override
  protected Message readMessageFromChannel() throws IOException, ClassNotFoundException {
    return mirrorMessagingInterface.getNextMessage();
  }
  
  @Override
  protected void writeMessageToChannel(Message message) throws IOException {
    synchronized (sentMessageList) {
      sentMessageList.add(message);
      sentMessageList.notifyAll();
    }
  }
  
  static class SWTInProcessMessagingInterface extends InProcessMessagingInterface {
    
    private Display display;
    
    public SWTInProcessMessagingInterface(Display display) {
      super(true);
      this.display = display;
      setMirrorMessagingInterface(new SwingInProcessMessagingInterface(this));
      initialize(false);
    }
    
    @Override
    protected void asyncUIExec(Runnable runnable) {
      display.asyncExec(runnable);
    }
    
    @Override
    public boolean isUIThread() {
      return display.getThread() == Thread.currentThread();
    }
    
  }
  
  static class SwingInProcessMessagingInterface extends InProcessMessagingInterface {
    
    public SwingInProcessMessagingInterface(InProcessMessagingInterface mirrorMessagingInterface) {
      super(false);
      setMirrorMessagingInterface(mirrorMessagingInterface);
      initialize(false);
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
