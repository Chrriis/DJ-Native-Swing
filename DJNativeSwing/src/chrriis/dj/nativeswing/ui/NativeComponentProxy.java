/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.AWTEvent;
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
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.Disposable;
import chrriis.dj.nativeswing.NativeInterfaceHandler;
import chrriis.dj.nativeswing.ui.NativeComponentProxyWindow.EmbeddedWindow;

/**
 * @author Christopher Deckers
 */
abstract class NativeComponentProxy extends JComponent implements Disposable {

  protected NativeComponent nativeComponent;
  protected boolean isDestroyOnFinalize;

  protected AWTEventListener maskAdjustmentEventListener = new AWTEventListener() {
    public void eventDispatched(AWTEvent e) {
      boolean isAdjustingMask = false;
      switch(e.getID()) {
        case ComponentEvent.COMPONENT_RESIZED:
        case ComponentEvent.COMPONENT_MOVED:
          isAdjustingMask = true;
          break;
        case ComponentEvent.COMPONENT_SHOWN:
        case ComponentEvent.COMPONENT_HIDDEN:
          if(e.getSource() instanceof Window) {
            isAdjustingMask = true;
          }
          break;
      }
      if(isAdjustingMask) {
        NativeComponentProxy componentEmbedder = nativeComponent.getComponentProxy();
        if(componentEmbedder != null) {
          componentEmbedder.adjustPeerMask();
        }
      }
    }
  };

  protected NativeComponentProxy(NativeComponent nativeComponent) {
    isDestroyOnFinalize = NativeComponent.getNextInstancePreferences().isDestroyOnFinalize();
    nativeComponent.setComponentProxy(this);
    setFocusable(true);
    this.nativeComponent = nativeComponent;
  }
  
  protected HierarchyListener hierarchyListener = new HierarchyListener() {
    public void hierarchyChanged(HierarchyEvent e) {
      long changeFlags = e.getChangeFlags();
      if((changeFlags & (HierarchyEvent.SHOWING_CHANGED)) != 0) {
        adjustPeerMask();
      }
    }
  };
  
  @Override
  public void addNotify() {
    super.addNotify();
    nativeComponent.setComponentProxy(this);
    addHierarchyListener(hierarchyListener);
    Toolkit.getDefaultToolkit().addAWTEventListener(maskAdjustmentEventListener, AWTEvent.COMPONENT_EVENT_MASK);
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
    removeHierarchyListener(hierarchyListener);
    Toolkit.getDefaultToolkit().removeAWTEventListener(maskAdjustmentEventListener);
    if(isDestroyOnFinalize) {
      disconnectPeer();
      adjustPeerMask();
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

  protected Component peer;
  
  protected abstract Component createPeer();

  protected abstract void addPeer();
  
  protected abstract void destroyPeer();

  protected void adjustPeerBounds() {
    if(peer == null) {
      return;
    }
    peer.setSize(getSize());
    Point location = new Point(0, 0);
    if(peer instanceof Window) {
      SwingUtilities.convertPointToScreen(location, this);
    } else {
      location = SwingUtilities.convertPoint(this, location, peer.getParent());
    }
    peer.setLocation(location.x, location.y);
    peer.invalidate();
    peer.validate();
    peer.repaint();
    adjustPeerMask();
  }

  public abstract void adjustPeerMask();
  
  @Override
  public void paint(Graphics g) {
    super.paint(g);
    adjustPeerMask();
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
  
  protected Area computePeerMaskArea() {
    Window windowAncestor = SwingUtilities.getWindowAncestor(this);
    if(windowAncestor == null || !isShowing()) {
      return new Area(new Rectangle(0, 0));
    }
    Area area = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
    for(int i=getComponentCount()-1; i>=0; i--) {
      Component c = getComponent(i);
      if(c == this) {
        break;
      }
      if(c.isVisible()) {
        area.subtract(new Area(c.getBounds()));
      }
    }
    if(area.isEmpty()) {
      return area;
    }
    Container c = this;
    Container parent = c.getParent();
    while(parent != null && !(parent instanceof Window)) {
      area.intersect(new Area(new Rectangle(SwingUtilities.convertPoint(parent, new Point(0, 0), this), new Dimension(parent.getWidth(), parent.getHeight()))));
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
                  area.subtract(new Area(SwingUtilities.convertRectangle(child, child2.getBounds(), this)));
                }
              }
            } else {
              area.subtract(new Area(SwingUtilities.convertRectangle(parent, child.getBounds(), this)));
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
    for(Window window: NativeInterfaceHandler.getWindows()) {
      if(!(window instanceof EmbeddedWindow) && window.isVisible()) {
        for(Window owner = window; (owner=owner.getOwner()) != null; ) {
          if(owner == windowAncestor) {
            area.subtract(new Area(SwingUtilities.convertRectangle(window, new Rectangle(new Point(0, 0),  window.getSize()), this)));
            break;
          }
        }
      }
    }
    return area;
  }
  
}
