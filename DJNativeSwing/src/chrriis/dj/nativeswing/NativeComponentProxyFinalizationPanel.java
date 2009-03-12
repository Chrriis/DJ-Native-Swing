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

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.NativeComponentWrapper.NativeComponentHolder;

/**
 * @author Christopher Deckers
 */
public class NativeComponentProxyFinalizationPanel extends NativeComponentProxy {

  NativeComponentProxyFinalizationPanel(NativeComponentWrapper nativeComponentWrapper) {
    super(nativeComponentWrapper);
  }

  private static class EmbeddedPanel extends Panel implements NativeComponentHolder {

    public EmbeddedPanel() {
      super(new BorderLayout());
    }

  }

  private EmbeddedPanel embeddedPanel;
  private boolean isProxied;

  @Override
  public void addNotify() {
    super.addNotify();
    boolean isEmbeddedPanelCreated = embeddedPanel != null;
    if(!isEmbeddedPanelCreated) {
      embeddedPanel = new EmbeddedPanel();
      embeddedPanel.add(nativeComponentWrapper.getNativeComponent(), BorderLayout.CENTER);
    }
    isProxied = false;
    JComponent oldParent = (JComponent)embeddedPanel.getParent();
    if(oldParent != this) {
      if(oldParent == null) {
        add(embeddedPanel, BorderLayout.CENTER);
      } else {
        // Hack to reparent without the native component to be disposed
        setComponentZOrder(embeddedPanel, 0);
      }
      if(oldParent != null) {
        oldParent.revalidate();
        oldParent.repaint();
      }
      revalidate();
      repaint();
      embeddedPanel.setVisible(true);
    }
  }

  @Override
  public void removeNotify() {
    if(!isProxied) {
      embeddedPanel.setVisible(false);
      isProxied = true;
      for(Component parent = this; (parent = parent.getParent()) != null; ) {
        if(!parent.isLightweight() && parent instanceof RootPaneContainer) {
          JLayeredPane layeredPane = ((RootPaneContainer)parent).getLayeredPane();
          layeredPane.setLayer(embeddedPanel, Integer.MIN_VALUE);
          // Hack to reparent without the native component to be disposed
//          layeredPane.add(embeddedPanel);
          layeredPane.setComponentZOrder(embeddedPanel, 0);
          layeredPane.revalidate();
          layeredPane.repaint();
          revalidate();
          repaint();
          super.removeNotify();
          return;
        }
      }
      super.removeNotify();
      throw new IllegalStateException("The window ancestor must be a root pane container!");
    }
    super.removeNotify();
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
    embeddedPanel.removeNotify();
    embeddedPanel = null;
    Container parent = panel.getParent();
    if(parent != null) {
      parent.remove(panel);
      parent.invalidate();
      parent.validate();
      parent.repaint();
    }
  }

}
