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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JLayeredPane;
import javax.swing.RootPaneContainer;

/**
 * @author Christopher Deckers
 */
abstract class NativeComponentProxy extends EmbeddableComponent {

  private BackBufferManager backBufferManager;
  protected NativeComponentWrapper nativeComponentWrapper;

  protected NativeComponentProxy(NativeComponentWrapper nativeComponentWrapper) {
    this.nativeComponentWrapper = nativeComponentWrapper;
    nativeComponentWrapper.setNativeComponentProxy(this);
    backBufferManager = new BackBufferManager(nativeComponentWrapper, this);
    setFocusable(true);
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        NativeComponentProxy.this.nativeComponentWrapper.getNativeComponent().requestFocus();
      }
    });
  }

  @Override
  protected void printComponent(Graphics g) {
    nativeComponentWrapper.getNativeComponent().print(g);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    backBufferManager.paintBackBuffer(g);
  }

  public BackBufferManager getBackBufferManager() {
    return backBufferManager;
  }

  protected abstract void dispose();

  protected static JLayeredPane findLayeredPane(Component c) {
    for(Component parent = c; (parent = parent.getParent()) != null; ) {
      if(!parent.isLightweight() && parent instanceof RootPaneContainer) {
        return ((RootPaneContainer)parent).getLayeredPane();
      }
    }
    throw new IllegalStateException("The window ancestor must be a root pane container!");
  }

}
