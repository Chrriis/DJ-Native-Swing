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
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Area;
import java.beans.PropertyVetoException;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.NativeInterfaceHandler;

import com.sun.jna.examples.WindowUtils;

/**
 * @author Christopher Deckers
 */
public class NativeComponentEmbedder extends JComponent {

  protected NativeComponent nativeComponent;
  protected static volatile int mouseButtonDownCount;
  static {
    Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
      public void eventDispatched(AWTEvent e) {
        if(e.getSource() instanceof NativeComponent) {
          return;
        }
        switch (e.getID()) {
          case MouseEvent.MOUSE_PRESSED:
            mouseButtonDownCount++;
            break;
          case MouseEvent.MOUSE_RELEASED:
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                    mouseButtonDownCount--;
                  }
                });
              }
            });
            break;
        }
      }
    }, AWTEvent.MOUSE_EVENT_MASK);
  }
  
  protected NativeComponentEmbedder(NativeComponent nativeComponent) {
    nativeComponent.setComponentEmbedder(this);
    setFocusable(true);
    this.nativeComponent = nativeComponent;
//    JButton button = new JButton("Some Test Component");
//    button.setLocation(100, 100);
//    button.setSize(button.getPreferredSize());
//    add(button);
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        NativeComponentEmbedder.this.nativeComponent.requestFocus();
      }
    });
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    adjustWindowMask();
  }
  
  protected static class EmbeddedWindow extends JDialog {
    public EmbeddedWindow(Window w) {
      super(w);
      init();
    }
    
    public EmbeddedWindow(Frame w) {
      super(w);
      init();
    }
    
    public EmbeddedWindow(Dialog w) {
      super(w);
      init();
    }

    protected void init() {
      setUndecorated(true);
    }
    
    @Override
    public boolean getFocusableWindowState() {
//      System.err.println(hashCode() + ", " + mouseButtonDownCount + ", " + super.getFocusableWindowState());
      return mouseButtonDownCount == 0 && super.getFocusableWindowState();
    }
  }
  
  protected EmbeddedWindow window;
  
  @Override
  public void addNotify() {
    super.addNotify();
    Window windowAncestor = SwingUtilities.getWindowAncestor(this);
    if(Utils.IS_JAVA_6_OR_GREATER) {
      window = new EmbeddedWindow(windowAncestor);
    } else {
      if(windowAncestor instanceof Dialog) {
        window = new EmbeddedWindow((Dialog)windowAncestor);
      } else {
        window = new EmbeddedWindow((Frame)windowAncestor);
      }
    }
    window.addWindowFocusListener(new WindowFocusListener() {
      public void windowGainedFocus(WindowEvent e) {
        for(Component parent = NativeComponentEmbedder.this; parent != null && !(parent instanceof Window); parent = parent.getParent()) {
          if(parent instanceof JInternalFrame) {
            Window windowAncestor = SwingUtilities.getWindowAncestor(NativeComponentEmbedder.this);
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
    adjustWindowBounds();
//    window.setFocusableWindowState(true);
    window.setVisible(true);
    addHierarchyBoundsListener(new HierarchyBoundsListener() {
      public void ancestorMoved(HierarchyEvent e) {
        adjustWindowBounds();
      }
      public void ancestorResized(HierarchyEvent e) {
        adjustWindowBounds();
      }
    });
  }
  
  @Override
  public void removeNotify() {
    super.removeNotify();
    if(window != null) {
      window.dispose();
      window = null;
    }
  }
  
  @SuppressWarnings("deprecation")
  @Override
  public void reshape(int x, int y, int w, int h) {
    super.reshape(x, y, w, h);
    adjustWindowBounds();
  }
  
  protected void adjustWindowBounds() {
    if(window == null) {
      return;
    }
    window.setSize(getSize());
    Point location = new Point(0, 0);
    SwingUtilities.convertPointToScreen(location, this);
    window.setLocation(location.x, location.y);
    adjustWindowMask();
  }

  protected volatile boolean isInvoking;
  
  public void adjustWindowMask() {
    if(isInvoking) {
      return;
    }
    isInvoking = true;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        isInvoking = false;
        adjustWindowMask_();
      }
    });
  }
  
  protected void adjustWindowMask_() {
    if(window == null) {
      return;
    }
    Area area = new Area();
    Window windowAncestor = SwingUtilities.getWindowAncestor(this);
    if(windowAncestor == null) {
      return;
    }
    for(Component c: windowAncestor.getComponents()) {
      area.add(new Area(new Rectangle(SwingUtilities.convertPoint(c, new Point(0, 0), this), new Dimension(c.getWidth(), c.getHeight()))));
    }
    area.intersect(new Area(new Rectangle(0, 0, getWidth(), getHeight())));
    for(Component c: getComponents()) {
      if(c.isVisible()) {
        area.subtract(new Area(c.getBounds()));
      }
    }
    Container c = this;
    Container parent = c.getParent();
    while(parent != null && !(parent instanceof Window)) {
      area.intersect(new Area(new Rectangle(SwingUtilities.convertPoint(parent, new Point(0, 0), this), new Dimension(parent.getWidth(), parent.getHeight()))));
      if(parent instanceof JComponent && !((JComponent)parent).isOptimizedDrawingEnabled()) {
        boolean isFound = false;
        for(int i=parent.getComponentCount()-1; i>=0; i--) {
          Component child = parent.getComponent(i);
          if(child == c) {
            isFound = true;
          } else if(isFound && child.isVisible()) {
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
  
  protected Area lastArea = new Area();
  
  protected static boolean isLayeringPreferred;
  
  public static void setLayeringPreferred(boolean isLayeringPreferred) {
    NativeComponentEmbedder.isLayeringPreferred = isLayeringPreferred;
  }
  
  public static boolean isLayeringEnabled() {
    if(!isLayeringPreferred && !Boolean.parseBoolean(System.getProperty("dj.nativeswing.layering"))) {
      return false;
    }
    try {
      Class.forName("com.sun.jna.examples.WindowUtils");
      return true;
    } catch(Exception e) {
    }
    return false;
  }
  
  public static Component getEmbeddedComponent(NativeComponent nativeComponent) {
    return isLayeringEnabled()? new NativeComponentEmbedder(nativeComponent): nativeComponent;
  }
  
}
