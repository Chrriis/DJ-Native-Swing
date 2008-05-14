/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.awt.AWTEvent;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import chrriis.common.Filter;
import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.NativeComponentProxyPanel.EmbeddedPanel;
import chrriis.dj.nativeswing.NativeComponentProxyWindow.EmbeddedWindow;

/**
 * @author Christopher Deckers
 */
abstract class NativeComponentProxy extends JComponent {

  private BackBufferManager backBufferManager;
  protected NativeComponent nativeComponent;
  protected boolean isDestructionOnFinalization;
  protected boolean isVisibilityConstrained;

  private AWTEventListener shapeAdjustmentEventListener;

  protected NativeComponentProxy(NativeComponent nativeComponent, boolean isVisibilityConstrained, boolean isDestructionOnFinalization) {
    this.isDestructionOnFinalization = isDestructionOnFinalization;
    this.isVisibilityConstrained = isVisibilityConstrained;
    setFocusable(true);
    this.nativeComponent = nativeComponent;
    backBufferManager = new BackBufferManager(nativeComponent, this);
    hierarchyListener = new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
        long changeFlags = e.getChangeFlags();
        if((changeFlags & (HierarchyEvent.SHOWING_CHANGED)) != 0) {
          if(NativeComponentProxy.this.isVisibilityConstrained) {
            adjustPeerShape();
          } else {
            adjustPeerBounds();
          }
        }
      }
    };
    if(isVisibilityConstrained) {
      shapeAdjustmentEventListener = new AWTEventListener() {
        public void eventDispatched(AWTEvent e) {
          boolean isAdjustingShape = false;
          switch(e.getID()) {
            case ContainerEvent.COMPONENT_ADDED:
            case ContainerEvent.COMPONENT_REMOVED:
            case ComponentEvent.COMPONENT_RESIZED:
            case ComponentEvent.COMPONENT_MOVED:
              isAdjustingShape = true;
              break;
            case ComponentEvent.COMPONENT_SHOWN:
            case ComponentEvent.COMPONENT_HIDDEN:
              if(e.getSource() instanceof Window) {
                isAdjustingShape = true;
              }
              break;
          }
          if(isAdjustingShape) {
            if(NativeComponentProxy.this.nativeComponent.getNativeComponentProxy() == NativeComponentProxy.this) {
              adjustPeerShape();
            }
          }
        }
      };
    }
  }
  
  private HierarchyListener hierarchyListener;
  
  @Override
  public void addNotify() {
    super.addNotify();
    nativeComponent.setNativeComponentProxy(this);
    if(hierarchyListener != null) {
      addHierarchyListener(hierarchyListener);
    }
    if(shapeAdjustmentEventListener != null) {
      Toolkit.getDefaultToolkit().addAWTEventListener(shapeAdjustmentEventListener, AWTEvent.COMPONENT_EVENT_MASK | AWTEvent.CONTAINER_EVENT_MASK);
    }
    if(peer != null) {
      adjustPeerBounds();
      connectPeer();
      return;
    }
    peer = createPeer();
    adjustPeerBounds();
    connectPeer();
    addPeer();
  }
  
  protected void connectPeer() {
    
  }
  
  protected void disconnectPeer() {
    
  }
  
  @Override
  public void removeNotify() {
    super.removeNotify();
    nativeComponent.setNativeComponentProxy(null);
    if(hierarchyListener != null) {
      removeHierarchyListener(hierarchyListener);
    }
    if(shapeAdjustmentEventListener != null) {
      Toolkit.getDefaultToolkit().removeAWTEventListener(shapeAdjustmentEventListener);
    }
    if(isDestructionOnFinalization) {
      disconnectPeer();
      if(isVisibilityConstrained) {
        adjustPeerShape();
      } else {
        adjustPeerBounds();
      }
      return;
    }
    dispose();
  }
  
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        dispose();
      }
    });
  }
  
  public void dispose() {
    if(peer == null) {
      return;
    }
    destroyPeer();
    peer = null;
  }

  private Component peer;
  
  protected abstract Component createPeer();

  protected abstract void addPeer();
  
  protected abstract void destroyPeer();

  protected void adjustPeerBounds() {
    if(peer == null) {
      return;
    }
    if(!isVisibilityConstrained) {
      boolean isShowing = isShowing();
      if(isShowing != peer.isVisible()) {
        peer.setVisible(isShowing);
      }
    }
    Point location = new Point(0, 0);
    if(peer instanceof Window) {
      SwingUtilities.convertPointToScreen(location, this);
    } else {
      location = SwingUtilities.convertPoint(this, location, peer.getParent());
    }
    Dimension size = getPeerSize();
    Rectangle bounds = new Rectangle(location.x, location.y, size.width, size.height);
    if(!peer.getBounds().equals(bounds)) {
      peer.setBounds(bounds);
      peer.invalidate();
      peer.validate();
      peer.repaint();
      if(isVisibilityConstrained) {
        adjustPeerShape();
      }
    }
  }
  
  protected Dimension getPeerSize() {
    return getSize();
  }

  protected abstract void adjustPeerShape();
  
  @Override
  public void paint(Graphics g) {
    super.paint(g);
    // On Linux, a JInternalFrame brought to the front may generate a paint call only to that one.
    // We need to adjust the shape of the frames that go to the back as well.
    for(Canvas canvas: NativeInterface.getCanvas()) {
      if(canvas instanceof NativeComponent) {
        NativeComponentProxy nativeComponentProxy = ((NativeComponent)canvas).getNativeComponentProxy();
        if(nativeComponentProxy != null) {
          if(nativeComponentProxy.isVisibilityConstrained) {
            nativeComponentProxy.adjustPeerShape();
          }
        }
      }
    }
  }
  
  @SuppressWarnings("deprecation")
  @Override
  public void reshape(int x, int y, int w, int h) {
    if(x == getX() && y == getY() && w == getWidth() && h == getHeight()) {
      return;
    }
    super.reshape(x, y, w, h);
    adjustPeerBounds();
  }
  
  protected abstract Rectangle[] getPeerShapeArea();
  
  protected Rectangle[] computePeerShapeArea() {
    Rectangle[] shape = UIUtils.getComponentVisibleArea(this, new Filter<Component>() {
      public boolean accept(Component c) {
        return !(c instanceof EmbeddedPanel);
      }
    }, false);
    if(shape.length == 0) {
      return shape;
    }
    Window windowAncestor = SwingUtilities.getWindowAncestor(this);
    Rectangle tempRectangle = new Rectangle();
    for(Window window: NativeInterface.getWindows()) {
      if(!(window instanceof EmbeddedWindow) && window.isVisible()) {
        for(Window owner = window; (owner=owner.getOwner()) != null; ) {
          if(owner == windowAncestor) {
            tempRectangle.setBounds(0, 0, window.getWidth(), window.getHeight());
            shape = UIUtils.subtract(shape, SwingUtilities.convertRectangle(window, tempRectangle, this));
            break;
          }
        }
      }
    }
    return shape;
  }

  @Override
  protected void printComponent(Graphics g) {
    nativeComponent.print(g);
  }
  
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    backBufferManager.paintBackBuffer(g);
  }
  
  public BackBufferManager getBackBufferManager() {
    return backBufferManager;
  }
  
}
