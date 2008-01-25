/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Area;
import java.beans.PropertyVetoException;

import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

import chrriis.common.Utils;

import com.sun.jna.examples.WindowUtils;

/**
 * @author Christopher Deckers
 */
class NativeComponentProxyWindow extends NativeComponentProxy {

  protected static volatile boolean isNonFocusable = true;
  
  static {
    Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
      public void eventDispatched(AWTEvent e) {
        if(e.getSource() instanceof NativeComponent) {
          return;
        }
        switch (e.getID()) {
          case MouseEvent.MOUSE_ENTERED:
          case MouseEvent.MOUSE_MOVED:
          case MouseEvent.MOUSE_DRAGGED:
            isNonFocusable = false;
            break;
          case MouseEvent.MOUSE_EXITED:
            isNonFocusable = true;
            break;
        }
      }
    }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
  }
  
  protected NativeComponentProxyWindow(NativeComponent nativeComponent) {
    super(nativeComponent);
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        isNonFocusable = false;
//        if(!NativeComponentEmbedder.this.window.isFocused()) {
//          System.err.println("REQ");
//          NativeComponentEmbedder.this.window.toFront();
//        }
        NativeComponentProxyWindow.this.nativeComponent.requestFocus();
      }
    });
  }

  protected static class EmbeddedWindow extends JDialog {
    
    protected NativeComponentProxyWindow nativeComponentEmbedder;
    
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
      this.nativeComponentEmbedder = nativeComponentEmbedder;
      setUndecorated(true);
    }
    
    @Override
    public boolean getFocusableWindowState() {
//      System.err.println(isNonFocusable + ", " + nativeComponentEmbedder.isFocusOwner() + ", " + nativeComponentEmbedder.nativeComponent.isFocusOwner());
//      System.err.println((!isNonFocusable || nativeComponentEmbedder.isFocusOwner()) && super.getFocusableWindowState());
      return (!isNonFocusable || nativeComponentEmbedder.isFocusOwner() || nativeComponentEmbedder.nativeComponent.isFocusOwner());
    }
  }
  
  protected EmbeddedWindow window;
  
  @Override
  public Component createPeer() {
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
    window.addWindowFocusListener(new WindowFocusListener() {
      public void windowGainedFocus(WindowEvent e) {
        for(Component parent = NativeComponentProxyWindow.this; parent != null && !(parent instanceof Window); parent = parent.getParent()) {
          if(parent instanceof JInternalFrame) {
            Window windowAncestor = SwingUtilities.getWindowAncestor(NativeComponentProxyWindow.this);
            if(windowAncestor != null) {
              boolean focusableWindowState = windowAncestor.getFocusableWindowState();
              windowAncestor.setFocusableWindowState(false);
//              ((JInternalFrame)parent).moveToFront();
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
    });
    window.getContentPane().add(nativeComponent, BorderLayout.CENTER);
    return window;
  }
  
  @Override
  protected void addPeer() {
    window.setVisible(true);
  }
  
  @Override
  protected void destroyPeer() {
    window.dispose();
    window = null;
  }
  
  protected volatile boolean isInvoking;
  
  public void adjustPeerMask() {
    if(isInvoking) {
      return;
    }
    isInvoking = true;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        isInvoking = false;
        adjustPeerMask_();
      }
    });
  }
  
  protected Area lastArea = new Area();
  
  protected void adjustPeerMask_() {
    if(window == null) {
      return;
    }
    Area area = computePeerMaskArea();
    if(area == null) {
      return;
    }
    if(area.isEmpty()) {
      window.setSize(0, 0);
    } else {
      window.setSize(getSize());
      if(!lastArea.equals(area)) {
        lastArea = area;
        WindowUtils.setWindowMask(window, area);
      }
    }
  }
  
}
