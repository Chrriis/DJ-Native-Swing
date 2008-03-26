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
import java.awt.Container;
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
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import chrriis.common.Disposable;
import chrriis.dj.nativeswing.NativeComponent.Options;
import chrriis.dj.nativeswing.NativeComponent.Options.DestructionTime;
import chrriis.dj.nativeswing.NativeComponent.Options.VisibilityConstraint;
import chrriis.dj.nativeswing.NativeComponentProxyWindow.EmbeddedWindow;

/**
 * @author Christopher Deckers
 */
abstract class NativeComponentProxy extends JComponent implements Disposable {

  NativeComponent nativeComponent;
  boolean isDestructionOnFinalization;
  boolean isVisibilityConstrained;

  private AWTEventListener shapeAdjustmentEventListener;

  protected NativeComponentProxy(NativeComponent nativeComponent) {
    Options options = nativeComponent.getOptions();
    DestructionTime destructionTime = options.getDestructionTime();
    isDestructionOnFinalization = destructionTime == DestructionTime.ON_FINALIZATION;
    isVisibilityConstrained = options.getVisibilityConstraint() != VisibilityConstraint.NONE;
    setFocusable(true);
    this.nativeComponent = nativeComponent;
    if(isVisibilityConstrained) {
      hierarchyListener = new HierarchyListener() {
        public void hierarchyChanged(HierarchyEvent e) {
          long changeFlags = e.getChangeFlags();
          if((changeFlags & (HierarchyEvent.SHOWING_CHANGED)) != 0) {
            adjustPeerShape();
          }
        }
      };
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
            if(NativeComponentProxy.this.nativeComponent.getComponentProxy() == NativeComponentProxy.this) {
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
    nativeComponent.setComponentProxy(this);
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
    nativeComponent.setComponentProxy(null);
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
        Component componentProxy = ((NativeComponent)canvas).getComponentProxy();
        if(componentProxy instanceof NativeComponentProxy) {
          NativeComponentProxy nativeComponentProxy = (NativeComponentProxy)componentProxy;
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
  
  protected Area computePeerShapeArea() {
    Window windowAncestor = SwingUtilities.getWindowAncestor(this);
    if(windowAncestor == null || !isShowing() || getWidth() == 0 || getHeight() == 0) {
      return new Area(new Rectangle(0, 0));
    }
    Rectangle tempRectangle = new Rectangle(0, 0, getWidth(), getHeight());
    Area area = new Area(tempRectangle);
    if(area.isEmpty()) {
      return area;
    }
    for(int i=getComponentCount()-1; i>=0; i--) {
      Component c = getComponent(i);
      if(c == this) {
        break;
      }
      if(c.isVisible()) {
        tempRectangle.setBounds(c.getX(), c.getY(), c.getWidth(), c.getHeight());
        area.subtract(new Area(tempRectangle));
      }
    }
    if(area.isEmpty()) {
      return area;
    }
    Container c = this;
    Container parent = c.getParent();
    while(parent != null && !(parent instanceof Window)) {
      tempRectangle.setBounds(0, 0, parent.getWidth(), parent.getHeight());
      area.intersect(new Area(SwingUtilities.convertRectangle(parent, tempRectangle, this)));
      if(parent instanceof JComponent && !((JComponent)parent).isOptimizedDrawingEnabled()) {
        Component[] children;
        if(parent instanceof JLayeredPane) {
          JLayeredPane layeredPane = (JLayeredPane)parent;
          List<Component> childList = new ArrayList<Component>(layeredPane.getComponentCount() - 1);
          for(int i=layeredPane.highestLayer(); i>=layeredPane.getLayer(c); i--) {
            Component[] components = layeredPane.getComponentsInLayer(i);
            for(Component child: components) {
              if(child == c) {
                break;
              }
              childList.add(child);
            }
          }
          children = childList.toArray(new Component[0]);
        } else {
          children = parent.getComponents();
        }
        for(int i=0; i<children.length; i++) {
          Component child = children[i];
          if(child == c) {
            break;
          }
          if(child.isVisible()) {
            if(parent instanceof JRootPane && ((JRootPane)parent).getGlassPane() == child) {
              if(child instanceof JComponent) {
                for(Component child2: ((JComponent)child).getComponents()) {
                  tempRectangle.setBounds(child2.getX(), child2.getY(), child2.getWidth(), child2.getHeight());
                  area.subtract(new Area(SwingUtilities.convertRectangle(child, tempRectangle, this)));
                }
              }
            } else {
              tempRectangle.setBounds(child.getX(), child.getY(), child.getWidth(), child.getHeight());
              area.subtract(new Area(SwingUtilities.convertRectangle(parent, tempRectangle, this)));
            }
          }
        }
      }
      if(area.isEmpty()) {
        return area;
      }
      c = parent;
      parent = c.getParent();
    }
    for(Window window: NativeInterface.getWindows()) {
      if(!(window instanceof EmbeddedWindow) && window.isVisible()) {
        for(Window owner = window; (owner=owner.getOwner()) != null; ) {
          if(owner == windowAncestor) {
            tempRectangle.setBounds(0, 0, window.getWidth(), window.getHeight());
            area.subtract(new Area(SwingUtilities.convertRectangle(window, tempRectangle, this)));
            break;
          }
        }
      }
    }
    return area;
  }

  private final Object backgroundBufferLock = new Object();
  private BufferedImage backgroundBuffer;
  
  public void createBackgroundBuffer() {
    Dimension size = getSize();
    if(size.width <= 0 || size.height <= 0) {
      backgroundBuffer = null;
      return;
    }
    BufferedImage backgroundBuffer = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
    nativeComponent.paintComponent(backgroundBuffer);
    synchronized(backgroundBufferLock) {
      this.backgroundBuffer = backgroundBuffer;
      repaint();
    }
  }
  
  public void releaseBackgroundBuffer() {
    backgroundBuffer = null;
  }
  
  @Override
  protected void printComponent(Graphics g) {
    BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
    nativeComponent.paintComponent(image);
    g.drawImage(image, 0, 0, null);
  }
  
  @Override
  protected void paintComponent(Graphics g) {
    synchronized(backgroundBufferLock) {
      if(backgroundBuffer != null) {
        g.drawImage(backgroundBuffer, 0, 0, this);
      }
    }
  }
  
  abstract void startCapture();
  
  abstract void stopCapture();
  
}
