/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

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
import chrriis.dj.nativeswing.ui.NativeComponent.Preferences;
import chrriis.dj.nativeswing.ui.NativeComponent.Preferences.Destruction;
import chrriis.dj.nativeswing.ui.NativeComponent.Preferences.Shaping;
import chrriis.dj.nativeswing.ui.NativeComponentProxyWindow.EmbeddedWindow;

/**
 * @author Christopher Deckers
 */
abstract class NativeComponentProxy extends JComponent implements Disposable {

  protected NativeComponent nativeComponent;
  protected boolean isDestructionOnFinalization;
  protected boolean isShaping;

  protected AWTEventListener shapeAdjustmentEventListener;

  protected NativeComponentProxy(NativeComponent nativeComponent) {
    Preferences preferences = NativeComponent.getNextInstancePreferences();
    Destruction destruction = preferences.getDestruction();
    isDestructionOnFinalization = destruction == Destruction.ON_FINALIZATION;
    Shaping shaping = preferences.getShaping();
    isShaping = shaping == Shaping.DEFAULT || shaping == Shaping.ENABLED;
    boolean isJNAPresent = isJNAPresent();
    if(!isJNAPresent) {
      if(shaping == Shaping.DEFAULT) {
        isShaping = false;
      } else {
        throw new IllegalStateException("The JNA libraries are required to use the shaping functionality!");
      }
    }
    setFocusable(true);
    this.nativeComponent = nativeComponent;
    if(isShaping) {
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
  
  protected HierarchyListener hierarchyListener;
  
  @Override
  public void addNotify() {
    super.addNotify();
    nativeComponent.setComponentProxy(this);
    if(hierarchyListener != null) {
      addHierarchyListener(hierarchyListener);
    }
    if(shapeAdjustmentEventListener != null) {
      Toolkit.getDefaultToolkit().addAWTEventListener(shapeAdjustmentEventListener, AWTEvent.COMPONENT_EVENT_MASK);
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
      if(isShaping) {
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

  protected Component peer;
  
  protected abstract Component createPeer();

  protected abstract void addPeer();
  
  protected abstract void destroyPeer();

  protected void adjustPeerBounds() {
    if(peer == null) {
      return;
    }
    if(!isShaping) {
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
      if(isShaping) {
        adjustPeerShape();
      }
    }
  }
  
  protected Dimension getPeerSize() {
    return getSize();
  }

  protected boolean isJNAPresent() {
    try {
      Class.forName("com.sun.jna.examples.WindowUtils");
      Class.forName("com.sun.jna.Platform");
      return true;
    } catch(Exception e) {
    }
    return false;
  }
  
  protected abstract void adjustPeerShape();
  
  @Override
  public void paint(Graphics g) {
    super.paint(g);
    // On Linux, a JInternalFrame brought to the front may generate a paint call only to that one.
    // We need to adjust the shape of the frames that go to the back as well.
    for(Canvas canvas: NativeInterfaceHandler.getCanvas()) {
      if(canvas instanceof NativeComponent) {
        Component componentProxy = ((NativeComponent)canvas).getComponentProxy();
        if(componentProxy instanceof NativeComponentProxy) {
          NativeComponentProxy nativeComponentProxy = (NativeComponentProxy)componentProxy;
          if(nativeComponentProxy.isShaping) {
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
