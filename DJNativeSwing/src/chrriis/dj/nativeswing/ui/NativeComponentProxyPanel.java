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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import com.sun.jna.examples.WindowUtils;

/**
 * @author Christopher Deckers
 */
class NativeComponentProxyPanel extends NativeComponentProxy {

  protected NativeComponentProxyPanel(NativeComponent nativeComponent) {
    super(nativeComponent);
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        NativeComponentProxyPanel.this.nativeComponent.requestFocus();
      }
    });
  }

  protected static class EmbeddedPanel extends Panel {
    
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
  
  protected EmbeddedPanel panel;
  
  @Override
  protected Component createPeer() {
    panel = new EmbeddedPanel();
    nativeComponent.addMouseListener(new MouseAdapter() {
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
      
    });
    panel.add(nativeComponent, BorderLayout.CENTER);
    return panel;
  }
  
  @Override
  protected void addPeer() {
    Window windowAncestor = SwingUtilities.getWindowAncestor(this);
    if(!(windowAncestor instanceof RootPaneContainer)) {
      throw new IllegalStateException("The window ancestor must be a root pane container!");
    }
    JLayeredPane layeredPane = ((RootPaneContainer)windowAncestor).getLayeredPane();
    layeredPane.setLayer(panel, Integer.MIN_VALUE);
    layeredPane.add(panel);
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
    if(panel == null) {
      return;
    }
    Area area = computePeerMaskArea();
    if(area == null) {
      return;
    }
    if(area.isEmpty()) {
      panel.setSize(0, 0);
    } else {
      panel.setSize(getSize());
      if(!lastArea.equals(area)) {
        lastArea = area;
        WindowUtils.setWindowMask(panel, area);
      }
    }
  }
  
}
