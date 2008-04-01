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

  private NativeComponent nativeComponent;
  private Component paintingComponent;
  
  public BackBufferManager(NativeComponent nativeComponent, Component paintingComponent) {
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
    Rectangle[] nonOpaqueAreas = UIUtils.subtract(boundsArea, UIUtils.getComponentVisibleArea(paintingComponent, new Filter<Component>() {
      public boolean accept(Component c) {
        return !c.isOpaque();
      }
    }, false));
    return UIUtils.subtract(nonOpaqueAreas, UIUtils.subtract(boundsArea, UIUtils.getComponentVisibleArea(paintingComponent, new Filter<Component>() {
      public boolean accept(Component c) {
        return c.isOpaque();
      }
    }, true)));
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
    nativeComponent.paintComponent(image, rectangles);
    synchronized(backBufferLock) {
      if(backBuffer != null && backBuffer != image) {
        synchronized(backBuffer) {
          Graphics g = image.getGraphics();
          g.drawImage(backBuffer, 0, 0, null);
          g.dispose();
        }
        backBuffer.flush();
      }
      this.backBuffer = image;
    }
    if(paintingComponent != nativeComponent) {
      Rectangle bounds = UIUtils.getBounds(rectangles);
      paintingComponent.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
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
