/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.NativeInterfaceHandler;
import chrriis.dj.nativeswing.ui.NativeComponentProxyWindow.EmbeddedWindow;

/**
 * @author Christopher Deckers
 */
public abstract class NativeComponentProxy extends JComponent {

  protected NativeComponent nativeComponent;

  protected NativeComponentProxy(NativeComponent nativeComponent) {
    nativeComponent.setComponentEmbedder(this);
    setFocusable(true);
    this.nativeComponent = nativeComponent;
  }
  
  @Override
  public void addNotify() {
    super.addNotify();
    peer = createPeer();
    adjustPeerBounds();
    addHierarchyListener(new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
        long changeFlags = e.getChangeFlags();
        if((changeFlags & (HierarchyEvent.SHOWING_CHANGED)) != 0) {
          adjustPeerMask();
        }
      }
    });
    addPeer();
  }
  
  @Override
  public void removeNotify() {
    super.removeNotify();
    if(peer != null) {
      destroyPeer();
      peer = null;
    }
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
    if(windowAncestor == null) {
      return null;
    }
    if(!isShowing()) {
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
