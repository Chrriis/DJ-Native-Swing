/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.ui.NativeComponent.NativeComponentHolder;
import chrriis.dj.nativeswing.ui.NativeComponent.Options.FiliationType;

import com.sun.jna.examples.WindowUtils;


/**
 * @author Christopher Deckers
 */
class NativeComponentProxyPanel extends NativeComponentProxy {

  protected boolean isProxiedFiliation;

  protected NativeComponentProxyPanel(NativeComponent nativeComponent) {
    super(nativeComponent);
    isProxiedFiliation = NativeComponent.getNextInstanceOptions().getFiliationType() != FiliationType.DIRECT;
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        NativeComponentProxyPanel.this.nativeComponent.requestFocus();
      }
    });
  }

  protected static class EmbeddedPanel extends Panel implements NativeComponentHolder {
    
    public EmbeddedPanel() {
      super(new BorderLayout(0, 0));
    }
    
    @Override
    public boolean contains(int x, int y) {
      return false;
    }
    
    @Override
    public boolean contains(Point p) {
      return false;
    }
    
  }
  
  protected HierarchyBoundsListener hierarchyBoundsListener = new HierarchyBoundsListener() {
    public void ancestorMoved(HierarchyEvent e) {
      Component component = e.getChanged();
      if(component instanceof Window) {
        return;
      }
      adjustPeerBounds();
    }
    public void ancestorResized(HierarchyEvent e) {
      adjustPeerBounds();
    }
  };

  protected MouseAdapter mouseListener = new MouseAdapter() {
    @Override
    public void mousePressed(MouseEvent e) {
      adjustFocus();
    }
    protected void adjustFocus() {
      for(Component parent = NativeComponentProxyPanel.this; parent != null && !(parent instanceof Window); parent = parent.getParent()) {
        if(parent instanceof JInternalFrame) {
          Window windowAncestor = SwingUtilities.getWindowAncestor(NativeComponentProxyPanel.this);
          if(windowAncestor != null) {
            boolean focusableWindowState = windowAncestor.getFocusableWindowState();
            windowAncestor.setFocusableWindowState(false);
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
  };
  
  protected EmbeddedPanel panel;
  
  @Override
  protected Component createPeer() {
    panel = new EmbeddedPanel();
    panel.add(nativeComponent, BorderLayout.CENTER);
    return panel;
  }
  
  @Override
  protected void connectPeer() {
    addHierarchyBoundsListener(hierarchyBoundsListener);
    nativeComponent.addMouseListener(mouseListener);
  }
  
  @Override
  protected void disconnectPeer() {
    removeHierarchyBoundsListener(hierarchyBoundsListener);
    nativeComponent.removeMouseListener(mouseListener);
  }
  
  @Override
  protected void addPeer() {
    Window windowAncestor = SwingUtilities.getWindowAncestor(this);
    if(!(windowAncestor instanceof RootPaneContainer)) {
      throw new IllegalStateException("The window ancestor must be a root pane container!");
    }
    if(isProxiedFiliation) {
      JLayeredPane layeredPane = ((RootPaneContainer)windowAncestor).getLayeredPane();
      layeredPane.setLayer(panel, Integer.MIN_VALUE);
      layeredPane.add(panel);
    } else {
      add(panel, BorderLayout.CENTER);
    }
  }
  
  @Override
  protected void destroyPeer() {
    Container parent = panel.getParent();
    if(parent != null) {
      parent.remove(panel);
      parent.invalidate();
      parent.validate();
      parent.repaint();
    }
    panel = null;
  }
  
  protected volatile boolean isInvoking;
  
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
  
  @Override
  protected void adjustPeerBounds() {
    if(!isProxiedFiliation) {
      return;
    }
    super.adjustPeerBounds();
  }
  
  protected Area lastArea = new Area();
  
  protected void adjustPeerShape_() {
    if(panel == null) {
      return;
    }
    Area area = computePeerShapeArea();
    if(area == null) {
      return;
    }
    if(!lastArea.equals(area)) {
      lastArea = area;
      if(area.isEmpty()) {
        panel.setVisible(false);
      } else {
        if(!panel.isVisible()) {
          panel.setVisible(true);
        }
        WindowUtils.setComponentMask(panel, area);
        nativeComponent.repaintNativeControl();
      }
    }
  }
  
}
