/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import chrriis.common.Filter;
import chrriis.common.UIUtils;

/**
 * @author Christopher Deckers
 */
class BackBufferManager {

  private NativeComponentWrapper nativeComponent;
  private Component paintingComponent;

  public BackBufferManager(NativeComponentWrapper nativeComponent, Component paintingComponent) {
    this.nativeComponent = nativeComponent;
    this.paintingComponent = paintingComponent;
  }

  private final Object backBufferLock = new Object();
  private BufferedImage backBuffer;

  public void updateBackBufferOnVisibleTranslucentAreas() {
    int width = paintingComponent.getWidth();
    int height = paintingComponent.getHeight();
    if(width <= 0 || height <= 0) {
      if(backBuffer != null) {
        backBuffer.flush();
      }
      backBuffer = null;
      return;
    }
    updateBackBuffer(getTranslucentOverlays());
  }

  protected Rectangle[] getTranslucentOverlays() {
    Rectangle[] boundsArea = new Rectangle[] {new Rectangle(0, 0, paintingComponent.getWidth(), paintingComponent.getHeight())};
    boundsArea = UIUtils.subtract(boundsArea, UIUtils.getComponentVisibleArea(paintingComponent, new Filter<Component>() {
      public Acceptance accept(Component c) {
        if(c.isOpaque()) {
          return Acceptance.YES;
        }
        return Acceptance.TEST_CHILDREN;
      }
    }));
    return UIUtils.subtract(boundsArea, UIUtils.getComponentVisibleArea(paintingComponent, new Filter<Component>() {
      public Acceptance accept(Component c) {
        if(!c.isOpaque()) {
          return Acceptance.YES;
        }
        return Acceptance.NO;
      }
    }));
  }

  public void createBackBuffer() {
    updateBackBuffer(new Rectangle[] {new Rectangle(paintingComponent.getWidth(), paintingComponent.getHeight())});
  }

  public void updateBackBuffer(Rectangle[] rectangles) {
    if(rectangles == null || rectangles.length == 0) {
      return;
    }
    int width = paintingComponent.getWidth();
    int height = paintingComponent.getHeight();
    if(width <= 0 || height <= 0) {
      if(backBuffer != null) {
        backBuffer.flush();
      }
      backBuffer = null;
      return;
    }
    BufferedImage image;
    if(backBuffer != null && backBuffer.getWidth() == width && backBuffer.getHeight() == height) {
      image = backBuffer;
    } else {
      image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
    nativeComponent.paintNativeComponent(image, rectangles);
    synchronized(backBufferLock) {
      if(backBuffer != null && backBuffer != image) {
        synchronized(backBuffer) {
          Graphics g = image.getGraphics();
          g.drawImage(backBuffer, 0, 0, null);
          g.dispose();
        }
        backBuffer.flush();
      }
      backBuffer = image;
    }
    if(paintingComponent != nativeComponent.getNativeComponent()) {
      Rectangle bounds = UIUtils.getBounds(rectangles);
      paintingComponent.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
    }
  }

  public boolean hasBackBuffer() {
    synchronized(backBufferLock) {
      return backBuffer != null;
    }
  }

  public void destroyBackBuffer() {
    synchronized(backBufferLock) {
      if(backBuffer != null) {
        backBuffer.flush();
      }
      backBuffer = null;
    }
  }

  public void paintBackBuffer(Graphics g) {
    synchronized(backBufferLock) {
      if(backBuffer != null) {
        synchronized(backBuffer) {
          g.drawImage(backBuffer, 0, 0, paintingComponent);
        }
      }
    }
  }

}
