/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import chrriis.common.Filter.Acceptance;

/**
 * @author Christopher Deckers
 */
public class UIUtils {

  private UIUtils() {}

  /**
   * Subtracts the area specified by the rectangle rect from each of the areas specified by the rectangles in rects.
   * @param rects The rectangles to substract from.
   * @param rect The rectangle to substract.
   * @return an array of rectangles, which may not have the same number of rectangles as in rects.
   */
  public static Rectangle[] subtract(Rectangle[] rects, Rectangle rect) {
    return subtract(rects, new Rectangle[] {rect});
  }

  /**
   * Subtracts the area specified by the rectangles in rects2 from each of the areas specified by the rectangles in rects1.
   * @param rects1 The rectangles to substract from.
   * @param rects2 The rectangles to substract.
   * @return an array of rectangles, which may not have the same number of rectangles as in rects1.
   */
  public static Rectangle[] subtract(Rectangle[] rects1, Rectangle[] rects2) {
    List<Rectangle> rectangleList = new ArrayList<Rectangle>(Arrays.asList(rects1));
    List<Rectangle> newRectangleList = new ArrayList<Rectangle>();
    for(int i=0; i<rects2.length; i++) {
      Rectangle r2 = rects2[i];
      for(Rectangle r1: rectangleList) {
        if(r1.intersects(r2)) {
          subtract(r1, r2, newRectangleList);
        } else {
          newRectangleList.add((Rectangle)r1.clone());
        }
      }
      rectangleList.clear();
      if(newRectangleList.isEmpty()) {
        break;
      }
      rectangleList.addAll(newRectangleList);
      newRectangleList.clear();
    }
    return rectangleList.toArray(new Rectangle[0]);
  }

  /**
   * Subtracts the area of r2 from r1, appending the new rectangle(s) to the
   * list of results.
   */
  private static void subtract(Rectangle r1, Rectangle r2, List<Rectangle> resultList) {
    // discover the edges of r1 that are covered by r2
    boolean left = r2.x <= r1.x && r2.x + r2.width > r1.x;
    boolean right = r2.x < r1.x + r1.width && r2.x + r2.width >= r1.x + r1.width;
    boolean top = r2.y <= r1.y && r2.y + r2.height > r1.y;
    boolean bottom = r2.y < r1.y + r1.height && r2.y + r2.height >= r1.y + r1.height;
    if(left && right && top && bottom) {
      // r2 fully obscures r1; no results
    } else if(left && right && top) {
      // r2 across top edge
      int y = r2.y + r2.height;
      int height = r1.y + r1.height - y;
      resultList.add(new Rectangle(r1.x, y, r1.width, height));
    } else if(left && right && bottom) {
      // r2 across bottom edge
      resultList.add(new Rectangle(r1.x, r1.y, r1.width, r2.y - r1.y));
    } else if(top && bottom && left) {
      // r2 across left edge
      int x = r2.x + r2.width;
      int width = r1.x + r1.width - x;
      resultList.add(new Rectangle(x, r1.y, width, r1.height));
    } else if(top && bottom && right) {
      // r2 across right edge
      resultList.add(new Rectangle(r1.x, r1.y, r2.x - r1.x, r1.height));
    } else if(left && top) {
      // r2 covers top-left corner
      int x = r2.x + r2.width;
      int y = r2.y + r2.height;
      resultList.add(new Rectangle(x, r1.y, r1.x + r1.width - x, y - r1.y));
      resultList.add(new Rectangle(r1.x, y, r1.width, r1.y + r1.height - y));
    } else if(left && bottom) {
      // r2 covers bottom-left corner
      resultList.add(new Rectangle(r1.x, r1.y, r1.width, r2.y - r1.y));
      int x = r2.x + r2.width;
      resultList.add(new Rectangle(x, r2.y, r1.x + r1.width - x, r1.y + r1.height - r2.y));
    } else if(right && top) {
      // r2 covers top-right corner
      int y = r2.y + r2.height;
      resultList.add(new Rectangle(r1.x, r1.y, r2.x - r1.x, y - r1.y));
      resultList.add(new Rectangle(r1.x, y, r1.width, r1.y + r1.height - y));
    } else if(right && bottom) {
      // r2 covers bottom-right corner
      resultList.add(new Rectangle(r1.x, r1.y, r1.width, r2.y - r1.y));
      resultList.add(new Rectangle(r1.x, r2.y, r2.x - r1.x, r1.y + r1.height - r2.y));
    } else if(left && right) {
      // r2 divides r1 into 2 horizontal rectangles
      resultList.add(new Rectangle(r1.x, r1.y, r1.width, r2.y - r1.y));
      int y = r2.y + r2.height;
      resultList.add(new Rectangle(r1.x, y, r1.width, r1.y + r1.height - y));
    } else if(top && bottom) {
      // r2 divides r1 into 2 vertical rectangles
      resultList.add(new Rectangle(r1.x, r1.y, r2.x - r1.x, r1.height));
      int x = r2.x + r2.width;
      resultList.add(new Rectangle(x, r1.y, r1.x + r1.width - x, r1.height));
    } else if(left) {
      // r2 crosses left edge only, dividing r1 into 3 rectangles
      resultList.add(new Rectangle(r1.x, r1.y, r1.width, r2.y - r1.y));
      int y = r2.y + r2.height;
      resultList.add(new Rectangle(r1.x, y, r1.width, r1.y + r1.height - y));
      int x = r2.x + r2.width;
      resultList.add(new Rectangle(x, r2.y, r1.x + r1.width - x, r2.height));
    } else if(right) {
      // r2 crosses right edge only, dividing r1 into 3 rectangles
      resultList.add(new Rectangle(r1.x, r1.y, r1.width, r2.y - r1.y));
      int y = r2.y + r2.height;
      resultList.add(new Rectangle(r1.x, y, r1.width, r1.y + r1.height - y));
      resultList.add(new Rectangle(r1.x, r2.y, r2.x - r1.x, r2.height));
    } else if(top) {
      // r2 crosses top edge only, dividing r1 into 3 rectangles
      resultList.add(new Rectangle(r1.x, r1.y, r2.x - r1.x, r1.height));
      int x = r2.x + r2.width;
      resultList.add(new Rectangle(x, r1.y, r1.x + r1.width - x, r1.height));
      int y = r2.y + r2.height;
      resultList.add(new Rectangle(r2.x, y, r2.width, r1.y + r1.height - y));
    } else if(bottom) {
      // r2 crosses bottom edge only, dividing r1 into 3 rectangles
      resultList.add(new Rectangle(r1.x, r1.y, r1.width, r2.y - r1.y));
      int height = r1.y + r1.height - r2.y;
      resultList.add(new Rectangle(r1.x, r2.y, r2.x - r1.x, height));
      int x = r2.x + r2.width;
      resultList.add(new Rectangle(x, r2.y, r1.x + r1.width - x, height));
    } else {
      // r2 is completely contained within r1, dividing r1 into 4 rectangles
      resultList.add(new Rectangle(r1.x, r1.y, r1.width, r2.y - r1.y));
      int y = r2.y + r2.height;
      resultList.add(new Rectangle(r1.x, y, r1.width, r1.y + r1.height - y));
      resultList.add(new Rectangle(r1.x, r2.y, r2.x - r1.x, r2.height));
      int x = r2.x + r2.width;
      resultList.add(new Rectangle(x, r2.y, r1.x + r1.width - x, r2.height));
    }
  }

  /**
   * Get the area that is not covered by components obeying the condition imposed by the visitor. Usually, the filter focuses on all components, or opaque components.
   * @param component the component for which to find the visible areas.
   * @param filter the filter to consider when determining if an area is hidden.
   * @return an array of rectangles specify the visible area.
   */
  public static Rectangle[] getComponentVisibleArea(Component component, Filter<Component> filter) {
    Window windowAncestor = SwingUtilities.getWindowAncestor(component);
    int width = component.getWidth();
    int height = component.getHeight();
    if(windowAncestor == null || !component.isShowing() || width <= 0 || height <= 0) {
      return new Rectangle[0];
    }
    Rectangle tempRectangle = new Rectangle(0, 0, width, height);
    Rectangle[] shape = new Rectangle[] {new Rectangle(width, height)};
    if(component instanceof Container) {
      Container container = (Container)component;
      for(int i=container.getComponentCount()-1; i>=0; i--) {
        Component c = container.getComponent(i);
        if(c.isVisible()) {
          switch(filter.accept(c)) {
            case YES: {
              tempRectangle.setBounds(c.getX(), c.getY(), c.getWidth(), c.getHeight());
              shape = UIUtils.subtract(shape, tempRectangle);
              break;
            }
            case TEST_CHILDREN: {
              if(c instanceof Container) {
                shape = getChildrenVisibleArea(component, filter, shape, (Container)c, null);
              }
              break;
            }
          }
        }
      }
    }
    if(shape.length == 0) {
      return shape;
    }
    Component c = component;
    Container parent = c.getParent();
    while(parent != null && !(parent instanceof Window)) {
      // I was using parent.getWidth() and parent.getHeight(), but they return wrong value for applet Panel containers.
      // parent.getSize() returns the right value though...
      Dimension parentSize = parent.getSize();
      tempRectangle.setBounds(0, 0, parentSize.width, parentSize.height);
      Rectangle parentBounds = SwingUtilities.convertRectangle(parent, tempRectangle, component);
      List<Rectangle> newRectangleList = new ArrayList<Rectangle>();
      for(Rectangle rectangle: shape) {
        Rectangle r = rectangle.intersection(parentBounds);
        if(!r.isEmpty()) {
          newRectangleList.add(r);
        }
      }
      shape = newRectangleList.toArray(new Rectangle[0]);
      if(parent instanceof JComponent && !((JComponent)parent).isOptimizedDrawingEnabled()) {
        shape = getChildrenVisibleArea(component, filter, shape, parent, c);
      }
      if(shape.length == 0) {
        return shape;
      }
      c = parent;
      parent = c.getParent();
    }
    return shape;
  }

  private static String COMPONENT_TRANSPARENT_CLIENT_PROPERTY = "nsTransparent";

  public static enum TransparencyType {
    OPAQUE,
    TRANSPARENT_WITH_OPAQUE_CHILDREN,
    NOT_VISIBLE,
  }

  /**
   * Set a transparency hint for the getComponentVisibleArea(xxx) method to decide whether a component is visible.
   * @param c The component for which to set the transparency hint.
   * @param transparencyType which can be null to remove any hint; glass panes of JRootPanes are considered transparent by default for example.
   */
  public static void setComponentTransparencyHint(Component c, TransparencyType transparencyType) {
    // If it is not a JComponent it can only be opaque and thus isComponentTransparent will return false.
    if(c instanceof JComponent) {
      ((JComponent)c).putClientProperty(COMPONENT_TRANSPARENT_CLIENT_PROPERTY, transparencyType);
    }
  }

  /**
   * Get the actual transparent state, which considers the opacity and the transparency hint that may have been set with the setComponentTransparencyHint(xxx) method.
   */
  public static TransparencyType getComponentTransparency(Component c) {
    if(!(c instanceof JComponent) || c.isOpaque()) {
      return TransparencyType.OPAQUE;
    }
    TransparencyType transparencyType = (TransparencyType)((JComponent)c).getClientProperty(COMPONENT_TRANSPARENT_CLIENT_PROPERTY);
    if(transparencyType != null) {
      return transparencyType;
    }
    Container parent = c.getParent();
    if(parent instanceof JRootPane && ((JRootPane)parent).getGlassPane() == c) {
      return TransparencyType.TRANSPARENT_WITH_OPAQUE_CHILDREN;
    }
    return TransparencyType.OPAQUE;
  }

  private static Rectangle[] getChildrenVisibleArea(Component component, Filter<Component> filter, Rectangle[] shape, Container parent, Component c) {
    Component[] children;
    if(parent instanceof JLayeredPane) {
      JLayeredPane layeredPane = (JLayeredPane)parent;
      List<Component> childList = new ArrayList<Component>(layeredPane.getComponentCount() - 1);
      int layer = c == null? Integer.MIN_VALUE: layeredPane.getLayer(c);
      for(int i=layeredPane.highestLayer(); i>=layer; i--) {
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
    Rectangle tempRectangle = new Rectangle();
    for(int i=0; i<children.length; i++) {
      Component child = children[i];
      if(child == c) {
        break;
      }
      if(child.isVisible()) {
        Acceptance accept = filter.accept(child);
        if(accept == Acceptance.YES) {
          tempRectangle.setBounds(child.getX(), child.getY(), child.getWidth(), child.getHeight());
          shape = UIUtils.subtract(shape, SwingUtilities.convertRectangle(parent, tempRectangle, component));
        } else if(accept == Acceptance.TEST_CHILDREN && child instanceof Container) {
          shape = getChildrenVisibleArea(component, filter, shape, (Container)child, null);
        }
      }
    }
    return shape;
  }

  /**
   * Get the bounds containing all the rectangles.
   * @param rectangles the rectangles to get the bounds for.
   * @return a rectangle that contains all the rectangles, potentially an empty rectangle.
   */
  public static Rectangle getBounds(Rectangle[] rectangles) {
    Rectangle bounds = new Rectangle();
    if(rectangles.length > 0) {
      bounds.setBounds(rectangles[0]);
      for(int i=1; i<rectangles.length; i++) {
        Rectangle.union(bounds, rectangles[i], bounds);
      }
    }
    return bounds;
  }

  public static void setPreferredLookAndFeel() {
    try {
      String systemLookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
      if(!"com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(systemLookAndFeelClassName)) {
        UIManager.setLookAndFeel(systemLookAndFeelClassName);
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public static void revalidate(Component c) {
    if(c instanceof JComponent) {
      ((JComponent) c).revalidate();
    } else {
      c.invalidate();
      c.validate();
    }
  }

}
