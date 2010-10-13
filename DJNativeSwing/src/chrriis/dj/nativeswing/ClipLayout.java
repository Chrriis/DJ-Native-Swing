/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.io.Serializable;

/**
 * @author Christopher Deckers
 */
class ClipLayout implements LayoutManager2, Serializable {

  private Component component;

  public void addLayoutComponent(Component component, Object constraints) {
    synchronized(component.getTreeLock()) {
      if (constraints != null && !(constraints instanceof String)) {
        constraints = null;
      }
      addLayoutComponent((String)constraints, component);
    }
  }

  @Deprecated
  public void addLayoutComponent(String name, Component component) {
    synchronized(component.getTreeLock()) {
      this.component = component;
    }
  }

  public void removeLayoutComponent(Component component) {
    synchronized(component.getTreeLock()) {
      if (component == this.component) {
        this.component = null;
      }
    }
  }

  public void layoutContainer(Container target) {
    synchronized(target.getTreeLock()) {
      if(component == null) {
        return;
      }
      Insets insets = target.getInsets();
      int top = insets.top;
      int left = insets.left;
      int width;
      int height;
      if(clip != null) {
        left += clip.x;
        top += clip.y;
        width = clip.width;
        height = clip.height;
      } else {
        int right = target.getWidth() - insets.right;
        width = right - left;
        int bottom = target.getHeight() - insets.bottom;
        height = bottom - top;
      }
      component.setBounds(left, top, width, height);
    }
  }

  public Dimension minimumLayoutSize(Container target) {
    synchronized(target.getTreeLock()) {
      Insets insets = target.getInsets();
      Dimension size = new Dimension(insets.left + insets.right, insets.top + insets.bottom);
      if(component != null) {
        Dimension d = component.getMinimumSize();
        size.width += d.width;
        size.height += d.height;
      }
      return size;
    }
  }

  public Dimension preferredLayoutSize(Container target) {
    synchronized(target.getTreeLock()) {
      Insets insets = target.getInsets();
      Dimension size = new Dimension(insets.left + insets.right, insets.top + insets.bottom);
      if(component != null) {
        Dimension d = component.getPreferredSize();
        size.width += d.width;
        size.height += d.height;
      }
      return size;
    }
  }

  public Dimension maximumLayoutSize(Container target) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  public void invalidateLayout(Container target) {
  }

  public float getLayoutAlignmentX(Container parent) {
    return 0.5f;
  }

  public float getLayoutAlignmentY(Container parent) {
    return 0.5f;
  }

  private Rectangle clip;

  public void setClip(Rectangle clip) {
    this.clip = clip;
  }

  @Override
  public String toString() {
    return getClass().getName() + ",clip=" + (clip == null? "": clip.toString());
  }

}
