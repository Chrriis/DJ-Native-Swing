/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.Arrays;

import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.NativeComponent.NativeComponentHolder;

import com.sun.jna.examples.WindowUtils;


/**
 * @author Christopher Deckers
 */
class NativeComponentProxyPanel extends NativeComponentProxy {

  private boolean isProxiedFiliation;

  protected NativeComponentProxyPanel(NativeComponent nativeComponent, boolean isVisibilityConstrained, boolean isDestructionOnFinalization, boolean isProxiedFiliation) {
    super(nativeComponent, isVisibilityConstrained, isDestructionOnFinalization);
    setLayout(new BorderLayout(0, 0));
    this.isProxiedFiliation = isProxiedFiliation;
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        NativeComponentProxyPanel.this.nativeComponent.requestFocus();
      }
    });
  }

  static class EmbeddedPanel extends Panel implements NativeComponentHolder {
    
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
  
  private HierarchyBoundsListener hierarchyBoundsListener = new HierarchyBoundsListener() {
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

  private MouseAdapter mouseListener = new MouseAdapter() {
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
  
  private EmbeddedPanel panel;
  
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
      add(panel);
    }
  }
  
  @Override
  protected void destroyPeer() {
    if(panel == null) {
      return;
    }
    Container parent = panel.getParent();
    if(parent != null) {
      parent.remove(panel);
      parent.invalidate();
      parent.validate();
      parent.repaint();
    }
    panel = null;
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
  
  private Rectangle[] lastArea = new Rectangle[] {new Rectangle(getSize())};
  
  protected Rectangle[] getPeerShapeArea() {
    return lastArea;
  }
  
  protected void adjustPeerShape_() {
    if(panel == null) {
      return;
    }
    Rectangle[] rectangles = computePeerShapeArea();
    if(Arrays.equals(lastArea, rectangles)) {
      return;
    }
    lastArea = rectangles;
    if(rectangles.length == 0) {
      panel.setVisible(false);
    } else {
      if(!panel.isVisible()) {
        panel.setVisible(true);
      }
      WindowUtils.setComponentMask(panel, rectangles);
//      nativeComponent.repaintNativeControl();
    }
  }

}
