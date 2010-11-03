/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import chrriis.common.ObjectRegistry;
import chrriis.dj.nativeswing.swtimpl.Message;
import chrriis.dj.nativeswing.swtimpl.NSSystemPropertySWT;

/**
 * @author Christopher Deckers
 */
abstract class InProcessMessagingInterface extends MessagingInterface {

  private static final boolean IS_PRINTING_NON_SERIALIZABLE_MESSAGES = Boolean.parseBoolean(NSSystemPropertySWT.INTERFACE_INPROCESS_PRINTNONSERIALIZABLEMESSAGES.get());

  public InProcessMessagingInterface(boolean isNativeSide, int pid) {
    super(isNativeSide, pid);
  }

  @Override
  public void destroy() {
    // Dispose all SWT controls (simulate dead peer).
    ObjectRegistry controlRegistry = SWTNativeComponent.getControlRegistry();
    for(int instanceID: controlRegistry.getInstanceIDs()) {
      final Control control = (Control)controlRegistry.get(instanceID);
      controlRegistry.remove(instanceID);
      control.getDisplay().asyncExec(new Runnable() {
        public void run() {
          control.getShell().dispose();
        }
      });
    }
    InProcessMessagingInterface mirrorMessagingInterface = getMirrorMessagingInterface();
    setAlive(false);
    mirrorMessagingInterface.setAlive(false);
    synchronized (sentMessageList) {
      sentMessageList.notifyAll();
    }
    synchronized (mirrorMessagingInterface.sentMessageList) {
      mirrorMessagingInterface.sentMessageList.notifyAll();
    }
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
    boolean isAlive = isAlive();
    synchronized (sentMessageList) {
      while(sentMessageList.isEmpty()) {
        try {
          sentMessageList.wait();
        } catch(InterruptedException e) {
        }
        isAlive = isAlive();
        if(!isAlive) {
          break;
        }
      }
      if(!isAlive) {
        sentMessageList.clear();
        throw new IllegalStateException("The interface is closed.");
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
    if(IS_PRINTING_NON_SERIALIZABLE_MESSAGES && !(message instanceof NoSerializationTestMessage)) {
      ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());
      try {
        oos.writeObject(message);
      } catch(Exception e) {
        System.err.println("Non-serializable message: " + message);
      }
      oos.close();
    }
    synchronized (sentMessageList) {
      sentMessageList.add(message);
      sentMessageList.notifyAll();
    }
  }

  static class SWTInProcessMessagingInterface extends InProcessMessagingInterface {

    private Display display;

    public SWTInProcessMessagingInterface(Display display, int pid) {
      super(true, pid);
      this.display = display;
      setMirrorMessagingInterface(new SwingInProcessMessagingInterface(this, pid));
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

    public SwingInProcessMessagingInterface(InProcessMessagingInterface mirrorMessagingInterface, int pid) {
      super(false, pid);
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
