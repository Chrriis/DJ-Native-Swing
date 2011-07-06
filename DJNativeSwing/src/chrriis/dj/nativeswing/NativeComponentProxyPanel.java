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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyVetoException;
import java.util.Arrays;

import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

import chrriis.common.Filter;
import chrriis.common.UIUtils;
import chrriis.common.UIUtils.TransparencyType;
import chrriis.common.Utils;
import chrriis.dj.nativeswing.NativeComponentWrapper.NativeComponentHolder;

import com.sun.jna.examples.WindowUtils;


/**
 * @author Christopher Deckers
 */
class NativeComponentProxyPanel extends NativeComponentProxy {

  private static final boolean IS_DEBUGGING_SHAPE = Boolean.parseBoolean(NSSystemProperty.COMPONENTS_DEBUG_PRINTSHAPECOMPUTING.get());

  private boolean isProxiedFiliation;

  private AWTEventListener shapeAdjustmentEventListener;

  private boolean isDestructionOnFinalization;
  private boolean isVisibilityConstrained;

  NativeComponentProxyPanel(NativeComponentWrapper nativeComponentWrapper, boolean isVisibilityConstrained, boolean isDestructionOnFinalization, boolean isProxiedFiliation) {
    super(nativeComponentWrapper);
    this.isDestructionOnFinalization = isDestructionOnFinalization;
    this.isVisibilityConstrained = isVisibilityConstrained;
    hierarchyListener = new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
        long changeFlags = e.getChangeFlags();
        if((changeFlags & (HierarchyEvent.SHOWING_CHANGED)) != 0) {
          if(NativeComponentProxyPanel.this.isVisibilityConstrained) {
            adjustEmbeddedPanelShape();
          } else {
            adjustEmbeddedPanelBounds();
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
            if(NativeComponentProxyPanel.this.nativeComponentWrapper.getNativeComponentProxy() == NativeComponentProxyPanel.this) {
              adjustEmbeddedPanelShape();
            }
          }
        }
      };
    }
    this.isProxiedFiliation = isProxiedFiliation;
  }

  private HierarchyBoundsListener hierarchyBoundsListener = new HierarchyBoundsListener() {
    public void ancestorMoved(HierarchyEvent e) {
      Component component = e.getChanged();
      if(component instanceof Window) {
        return;
      }
      adjustEmbeddedPanelBounds();
    }
    public void ancestorResized(HierarchyEvent e) {
      adjustEmbeddedPanelBounds();
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


  private void adjustEmbeddedPanelBounds() {
    if(embeddedPanel == null) {
      return;
    }
    if(!isVisibilityConstrained) {
      boolean isShowing = isShowing();
      if(isShowing != embeddedPanel.isVisible()) {
        embeddedPanel.setVisible(isShowing);
      }
    }
    Container parent = embeddedPanel.getParent();
    if(parent != null) {
      Point location = SwingUtilities.convertPoint(this, new Point(0, 0), parent);
      Dimension size = getSize();
      Rectangle bounds = new Rectangle(location.x, location.y, size.width, size.height);
      Rectangle clip = embeddedPanel.getRectangularClip();
      if(clip != null) {
        bounds.x += clip.x;
        bounds.y += clip.y;
        bounds.width = clip.width;
        bounds.height = clip.height;
      }
      if(!embeddedPanel.getBounds().equals(bounds)) {
        embeddedPanel.setBounds(bounds);
        embeddedPanel.invalidate();
        embeddedPanel.validate();
        embeddedPanel.repaint();
        if(isVisibilityConstrained) {
          adjustEmbeddedPanelShape();
        }
      }
    }
  }

  private volatile boolean isInvoking;

  private void adjustEmbeddedPanelShape() {
    if(isInvoking) {
      return;
    }
    isInvoking = true;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        isInvoking = false;
        adjustEmbeddedPanelShape_();
      }
    });
  }

  private Rectangle[] lastArea = new Rectangle[] {new Rectangle(getSize())};

  private void adjustEmbeddedPanelShape_() {
    if(embeddedPanel == null) {
      return;
    }
    Rectangle[] rectangles = computePeerShapeArea();
    if(Arrays.equals(lastArea, rectangles)) {
      embeddedPanel.nativeComponentWrapper.getNativeComponent().repaint();
      return;
    }
    lastArea = rectangles;
    if(rectangles.length == 0) {
      embeddedPanel.setVisible(false);
    } else {
      if(!embeddedPanel.isVisible()) {
        embeddedPanel.setVisible(true);
      }
      embeddedPanel.applyShape(rectangles);
    }
  }

  private Rectangle[] computePeerShapeArea() {
    if(IS_DEBUGGING_SHAPE) {
      System.err.println("Computing shape: [" + NativeComponentProxyPanel.this.getWidth() + "x" + NativeComponentProxyPanel.this.getHeight() + "] " + nativeComponentWrapper.getComponentDescription());
    }
    Rectangle[] shape = UIUtils.getComponentVisibleArea(this, new Filter<Component>() {
      public Acceptance accept(Component c) {
        if(c instanceof EmbeddedPanel) {
          return Acceptance.NO;
        }
        TransparencyType transparency = UIUtils.getComponentTransparency(c);
        switch(transparency) {
          case TRANSPARENT_WITH_OPAQUE_CHILDREN: return Acceptance.TEST_CHILDREN;
          case NOT_VISIBLE: return Acceptance.NO;
        }
        if(IS_DEBUGGING_SHAPE) {
          Rectangle intersectionRectangle = SwingUtilities.convertRectangle(c, new Rectangle(c.getSize()), NativeComponentProxyPanel.this).intersection(new Rectangle(NativeComponentProxyPanel.this.getSize()));
          if(!intersectionRectangle.isEmpty()) {
            System.err.println("  -> Subtracting [" + intersectionRectangle.x + "," + intersectionRectangle.y + "," + intersectionRectangle.width + "x" + intersectionRectangle.height + "] " + c);
          }
        }
        return Acceptance.YES;
      }
    });
    return shape;
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    // On Linux, a JInternalFrame brought to the front may generate a paint call only to that one.
    // We need to adjust the shape of the frames that go to the back as well.
    for(NativeComponentWrapper ncw: NativeSwing.getNativeComponentWrappers()) {
      NativeComponentProxy nativeComponentProxy = ncw.getNativeComponentProxy();
      if(nativeComponentProxy instanceof NativeComponentProxyPanel) {
        if(((NativeComponentProxyPanel)nativeComponentProxy).isVisibilityConstrained) {
          ((NativeComponentProxyPanel)nativeComponentProxy).adjustEmbeddedPanelShape();
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
    adjustEmbeddedPanelBounds();
  }

  private static class EmbeddedPanel extends Panel implements NativeComponentHolder {

    private NativeComponentWrapper nativeComponentWrapper;
    private boolean isDestructionOnFinalization;

    public EmbeddedPanel(NativeComponentWrapper nativeComponentWrapper, boolean isDestructionOnFinalization) {
      super(new ClipLayout());
      this.nativeComponentWrapper = nativeComponentWrapper;
      this.isDestructionOnFinalization = isDestructionOnFinalization;
      enableEvents(MouseWheelEvent.MOUSE_WHEEL_EVENT_MASK);
    }

    @Override
    public boolean contains(int x, int y) {
      return false;
    }

    @Override
    public boolean contains(Point p) {
      return false;
    }

    private boolean isCrossWindowReparenting;
    private boolean isHiddenReparenting;
    private boolean isRemovingFromParent;

    @Override
    public void removeNotify() {
      if(isRemovingFromParent) {
        super.removeNotify();
        return;
      }
      if(isDestructionOnFinalization && !isCrossWindowReparenting) {
        // We try to support the case where a component is placed in a window that gets disposed, and then added to a different window.
        try {
          nativeComponentWrapper.storeInHiddenParent();
          isHiddenReparenting = true;
        } catch(Exception e) {
          // Do nothing, not supported.
        }
        super.removeNotify();
        isRemovingFromParent = true;
        Container parent = getParent();
        if(parent != null) {
          parent.remove(this);
          parent.invalidate();
          parent.validate();
        }
        isRemovingFromParent = false;
      } else {
        super.removeNotify();
      }
    }

    @Override
    public void addNotify() {
      super.addNotify();
      if(isHiddenReparenting) {
        isHiddenReparenting = false;
        nativeComponentWrapper.restoreFromHiddenParent();
      }
    }

    @Override
    protected void finalize() throws Throwable {
      if(isHiddenReparenting) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            nativeComponentWrapper.restoreFromHiddenParent();
            // Remove will dispose the component only after the store/restore sequence is complete.
            nativeComponentWrapper.getNativeComponent().removeNotify();
          }
        });
      }
    }

    private Rectangle clip;
    private static final boolean RESTRICT_SHAPE_TO_SINGLE_RECTANGLE = Boolean.parseBoolean(NSSystemProperty.COMPONENTS_FORCESINGLERECTANGLESHAPES.get());

    public void applyShape(Rectangle[] rectangles) {
      if(!Utils.IS_MAC && !RESTRICT_SHAPE_TO_SINGLE_RECTANGLE) {
        WindowUtils.setComponentMask(this, rectangles);
        nativeComponentWrapper.getNativeComponent().repaint();
        return;
      }
      // Mac does not support shaping native components, so instead we are going to constrain the component to a single rectangle.
      // This works for simple cases like being placed in a scrollpane where viewport's intersection with the native component result in a rectangular visible area.
      // It does not allow placing Swing components on top of native components because this creates a hole resulting in a shape being an aggregation of rectangles.
      Rectangle clip;
      if(rectangles.length == 0) {
        clip = null;
      } else {
        clip = new Rectangle(rectangles[0]);
        if(rectangles.length > 1) {
          System.err.println("Non-rectangular clip detected on a system that does not support this feature.");
          for(int i=1; i<rectangles.length; i++) {
            clip = clip.union(rectangles[i]);
          }
        }
      }
      if(Utils.equals(this.clip, clip)) {
        return;
      }
      int oldOffsetX = this.clip == null? 0: this.clip.x;
      int oldOffsetY = this.clip == null? 0: this.clip.y;
      this.clip = clip;
      int offsetX = clip == null? 0: clip.x;
      int offsetY = clip == null? 0: clip.y;
      NativeComponentProxy nativeComponentProxy = nativeComponentWrapper.getNativeComponentProxy();
      if(nativeComponentProxy != null) {
        ((ClipLayout)getLayout()).setClip(clip == null? null: new Rectangle(-clip.x, -clip.y, nativeComponentProxy.getWidth(), nativeComponentProxy.getHeight()));
      }
      Container parent = getParent();
      if(parent != null) {
        LayoutManager layout = parent.getLayout();
        if(layout instanceof ClipLayout) {
          ((ClipLayout)layout).setClip(clip);
        } else {
          int diffX = offsetX - oldOffsetX;
          int diffY = offsetY - oldOffsetY;
          setBounds(getX() + diffX, getY() + diffY, getWidth() - diffX, getHeight() - diffY);
        }
        doLayout();
        UIUtils.revalidate(parent);
      }
    }

    public Rectangle getRectangularClip() {
      return clip;
    }

  }

  private EmbeddedPanel embeddedPanel;
  private HierarchyListener hierarchyListener;

  @Override
  public void addNotify() {
    super.addNotify();
    if(hierarchyListener != null) {
      addHierarchyListener(hierarchyListener);
    }
    if(shapeAdjustmentEventListener != null) {
      Toolkit.getDefaultToolkit().addAWTEventListener(shapeAdjustmentEventListener, AWTEvent.COMPONENT_EVENT_MASK | AWTEvent.CONTAINER_EVENT_MASK);
    }
    JLayeredPane layeredPane = null;
    if(isProxiedFiliation) {
      // This call throws a runtime exception if the hierarchy is not compatible
      layeredPane = findLayeredPane(this);
    }
    boolean isEmbeddedPanelCreated = embeddedPanel != null;
    if(isEmbeddedPanelCreated) {
      if(isProxiedFiliation) {
        if(embeddedPanel.getParent() == null) {
          layeredPane.setLayer(embeddedPanel, Integer.MIN_VALUE);
          layeredPane.add(embeddedPanel);
          layeredPane.invalidate();
          layeredPane.validate();
        }
        JLayeredPane oldLayeredPane = findLayeredPane(embeddedPanel);
        if(layeredPane != oldLayeredPane) {
          embeddedPanel.isCrossWindowReparenting = true;
          nativeComponentWrapper.storeInHiddenParent();
          Container oldParent = embeddedPanel.getParent();
          oldParent.remove(embeddedPanel);
          UIUtils.revalidate(oldParent);
          oldParent.repaint();
          layeredPane.setLayer(embeddedPanel, Integer.MIN_VALUE);
          layeredPane.add(embeddedPanel);
          nativeComponentWrapper.restoreFromHiddenParent();
          embeddedPanel.isCrossWindowReparenting = false;
          UIUtils.revalidate(layeredPane);
          layeredPane.repaint();
          revalidate();
          repaint();
        }
      }
    } else {
      embeddedPanel = new EmbeddedPanel(nativeComponentWrapper, isDestructionOnFinalization);
      embeddedPanel.add(nativeComponentWrapper.getNativeComponent(), BorderLayout.CENTER);
    }
    lastArea = null;
    adjustEmbeddedPanelBounds();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        addHierarchyBoundsListener(hierarchyBoundsListener);
        adjustEmbeddedPanelBounds();
      }
    });
    nativeComponentWrapper.getNativeComponent().addMouseListener(mouseListener);
    if(!isEmbeddedPanelCreated) {
      if(isProxiedFiliation) {
        layeredPane.setLayer(embeddedPanel, Integer.MIN_VALUE);
        layeredPane.add(embeddedPanel);
        UIUtils.revalidate(layeredPane);
        layeredPane.repaint();
      } else {
        add(embeddedPanel);
        revalidate();
        repaint();
      }
    }
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    if(hierarchyListener != null) {
      removeHierarchyListener(hierarchyListener);
    }
    if(shapeAdjustmentEventListener != null) {
      Toolkit.getDefaultToolkit().removeAWTEventListener(shapeAdjustmentEventListener);
    }
    if(isDestructionOnFinalization) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          removeHierarchyBoundsListener(hierarchyBoundsListener);
          adjustEmbeddedPanelBounds();
        }
      });
      nativeComponentWrapper.getNativeComponent().removeMouseListener(mouseListener);
      if(isVisibilityConstrained) {
        adjustEmbeddedPanelShape();
      } else {
        adjustEmbeddedPanelBounds();
      }
      return;
    }
    dispose();
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if(embeddedPanel != null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          dispose();
        }
      });
    }
  }

  @Override
  public void dispose() {
    if(embeddedPanel == null) {
      return;
    }
    EmbeddedPanel panel = embeddedPanel;
    embeddedPanel = null;
    Container parent = panel.getParent();
    if(parent != null) {
      panel.isRemovingFromParent = true;
      parent.remove(panel);
      parent.invalidate();
      parent.validate();
      parent.repaint();
    }
  }

  @Override
  public Dimension getPreferredSize() {
    if(embeddedPanel != null) {
      return embeddedPanel.getPreferredSize();
    }
    return super.getPreferredSize();
  }

}
