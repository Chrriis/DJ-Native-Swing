/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyVetoException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.NativeComponent.SimpleNativeComponentHolder;

import com.sun.jna.examples.WindowUtils;


/**
 * @author Christopher Deckers
 */
class NativeComponentProxyWindow extends NativeComponentProxy {

  private static int instanceCount;
  private static volatile boolean isFocusBlocked;
  
  private static AWTEventListener focusAdjustmentEventListener = new AWTEventListener() {
    public void eventDispatched(AWTEvent e) {
      if(e.getSource() instanceof NativeComponent) {
        return;
      }
      switch (e.getID()) {
        case MouseEvent.MOUSE_ENTERED:
        case MouseEvent.MOUSE_MOVED:
        case MouseEvent.MOUSE_RELEASED:
        case MouseEvent.MOUSE_EXITED:
          isFocusBlocked = false;
          break;
        case MouseEvent.MOUSE_PRESSED:
        case MouseEvent.MOUSE_DRAGGED:
          isFocusBlocked = true;
          break;
      }
    }
  };

  protected NativeComponentProxyWindow(NativeComponent nativeComponent, boolean isVisibilityConstrained, boolean isDestructionOnFinalization) {
    super(nativeComponent, isVisibilityConstrained, isDestructionOnFinalization);
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        if(isFocusBlocked) {
          return;
        }
        if(!window.isFocused()) {
          window.toFront();
        }
        NativeComponentProxyWindow.this.nativeComponent.requestFocus();
      }
    });
  }

  static class EmbeddedWindow extends JDialog {
    
    protected Reference<NativeComponentProxyWindow> nativeComponentEmbedder;
    
    public EmbeddedWindow(NativeComponentProxyWindow nativeComponentEmbedder, Window w) {
      super(w);
      init(nativeComponentEmbedder);
    }
    
    public EmbeddedWindow(NativeComponentProxyWindow nativeComponentEmbedder, Frame w) {
      super(w);
      init(nativeComponentEmbedder);
    }
    
    public EmbeddedWindow(NativeComponentProxyWindow nativeComponentEmbedder, Dialog w) {
      super(w);
      init(nativeComponentEmbedder);
    }

    protected void init(NativeComponentProxyWindow nativeComponentEmbedder) {
      this.nativeComponentEmbedder = new WeakReference<NativeComponentProxyWindow>(nativeComponentEmbedder);
      setUndecorated(true);
    }
    
    @Override
    public boolean getFocusableWindowState() {
      NativeComponentProxyWindow nativeComponentEmbedder = this.nativeComponentEmbedder.get();
      if(nativeComponentEmbedder == null) {
        return false;
      }
//      System.err.println(isNonFocusable + ", " + nativeComponentEmbedder.isFocusOwner() + ", " + nativeComponentEmbedder.nativeComponent.isFocusOwner());
//      System.err.println((!isNonFocusable || nativeComponentEmbedder.isFocusOwner()) && super.getFocusableWindowState());
      return (!isFocusBlocked || nativeComponentEmbedder.isFocusOwner() || nativeComponentEmbedder.nativeComponent.isFocusOwner());
    }
  }
  
  private HierarchyBoundsListener hierarchyBoundsListener = new HierarchyBoundsListener() {
    public void ancestorMoved(HierarchyEvent e) {
      adjustPeerBounds();
    }
    public void ancestorResized(HierarchyEvent e) {
      adjustPeerBounds();
    }
  };

  private static class NWindowFocusListener implements WindowFocusListener {
    protected Reference<NativeComponentProxyWindow> nativeComponentEmbedder;
    protected NWindowFocusListener(NativeComponentProxyWindow nativeComponentEmbedder) {
      this.nativeComponentEmbedder = new WeakReference<NativeComponentProxyWindow>(nativeComponentEmbedder);
    }
    public void windowGainedFocus(WindowEvent e) {
      NativeComponentProxyWindow nativeComponentEmbedder = this.nativeComponentEmbedder.get();
      if(nativeComponentEmbedder == null) {
        return;
      }
      for(Component parent = nativeComponentEmbedder; parent != null && !(parent instanceof Window); parent = parent.getParent()) {
        if(parent instanceof JInternalFrame) {
          Window windowAncestor = SwingUtilities.getWindowAncestor(nativeComponentEmbedder);
          if(windowAncestor != null) {
            boolean focusableWindowState = windowAncestor.getFocusableWindowState();
            windowAncestor.setFocusableWindowState(false);
//            ((JInternalFrame)parent).moveToFront();
            try {
              ((JInternalFrame)parent).setSelected(true);
            } catch (PropertyVetoException e1) {
            }
            windowAncestor.setFocusableWindowState(focusableWindowState);
          }
          break;
        }
      }
    }
    public void windowLostFocus(WindowEvent e) {
    }
  }
  
  private EmbeddedWindow window;
  
  @Override
  protected Component createPeer() {
    if(instanceCount == 0) {
      isFocusBlocked = true;
      Toolkit.getDefaultToolkit().addAWTEventListener(focusAdjustmentEventListener, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
    instanceCount++;
    Window windowAncestor = SwingUtilities.getWindowAncestor(this);
    if(Utils.IS_JAVA_6_OR_GREATER) {
      window = new EmbeddedWindow(this, windowAncestor);
    } else {
      if(windowAncestor instanceof Dialog) {
        window = new EmbeddedWindow(this, (Dialog)windowAncestor);
      } else {
        window = new EmbeddedWindow(this, (Frame)windowAncestor);
      }
    }
    window.addWindowFocusListener(new NWindowFocusListener(this));
    window.getContentPane().add(new SimpleNativeComponentHolder(nativeComponent), BorderLayout.CENTER);
    return window;
  }
  
  @Override
  protected void connectPeer() {
    addHierarchyBoundsListener(hierarchyBoundsListener);
  }
  
  @Override
  protected void disconnectPeer() {
    removeHierarchyBoundsListener(hierarchyBoundsListener);
  }
  
  @Override
  protected void addPeer() {
    window.setVisible(true);
  }
  
  @Override
  protected void destroyPeer() {
    window.dispose();
    window = null;
    instanceCount--;
    if(instanceCount == 0) {
      Toolkit.getDefaultToolkit().removeAWTEventListener(focusAdjustmentEventListener);
    }
  }
  
  private volatile boolean isInvoking;
  
  protected void adjustPeerShape() {
    if(isInvoking) {
      return;
    }
    isInvoking = true;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        isInvoking = false;
        adjustPeerShape_();
      }
    });
  }
  
  private Rectangle[] lastArea = new Rectangle[0];
  
  protected Rectangle[] getPeerShapeArea() {
    return lastArea;
  }
  
  protected void adjustPeerShape_() {
    if(window == null) {
      return;
    }
    Rectangle[] area = computePeerShapeArea();
    if(Arrays.equals(lastArea, area)) {
      return;
    }
    lastArea = area;
    Rectangle[] s;
    Dimension size;
    if(area.length == 0) {
      size = new Dimension(1, 1);
      s = new Rectangle[] {new Rectangle(1, 1, 1, 1)};
    } else {
      size = getSize();
      s = area;
    }
    if(!window.getSize().equals(size)) {
      window.setSize(size);
    }
    WindowUtils.setWindowMask(window, s);
//    nativeComponent.repaintNativeControl();
  }
  
  @Override
  protected Dimension getPeerSize() {
    if(!isVisibilityConstrained) {
      return super.getPeerSize();
    }
    if(lastArea.length == 0) {
      return new Dimension(1, 1);
    }
    return super.getPeerSize();
  }
  
}
