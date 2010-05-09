/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Panel;
import java.awt.event.MouseWheelEvent;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.NativeComponentWrapper.NativeComponentHolder;

/**
 * @author Christopher Deckers
 */
class NativeComponentProxyFinalizationPanel extends NativeComponentProxy {

  NativeComponentProxyFinalizationPanel(NativeComponentWrapper nativeComponentWrapper) {
    super(nativeComponentWrapper);
  }

  private static class EmbeddedPanel extends Panel implements NativeComponentHolder {

    public EmbeddedPanel() {
      super(new BorderLayout());
      enableEvents(MouseWheelEvent.MOUSE_WHEEL_EVENT_MASK);
    }

  }

  private EmbeddedPanel embeddedPanel;
  private boolean isProxied;

  @Override
  public void addNotify() {
    super.addNotify();
    // This call throws a runtime exception if the hierarchy is not compatible
    JLayeredPane layeredPane = findLayeredPane(this);
    boolean isEmbeddedPanelCreated = embeddedPanel != null;
    if(isEmbeddedPanelCreated) {
      JLayeredPane oldLayeredPane = findLayeredPane(embeddedPanel);
      if(layeredPane != oldLayeredPane) {
        nativeComponentWrapper.prepareCrossWindowReparenting();
        Container oldParent = embeddedPanel.getParent();
        oldParent.remove(embeddedPanel);
        UIUtils.revalidate(oldParent);
        oldParent.repaint();
        layeredPane.setLayer(embeddedPanel, Integer.MIN_VALUE);
        layeredPane.add(embeddedPanel);
        nativeComponentWrapper.commitCrossWindowReparenting();
        UIUtils.revalidate(layeredPane);
        layeredPane.repaint();
        revalidate();
        repaint();
      }
    } else {
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
      try {
        // This call throws a runtime exception if the hierarchy is not compatible
        JLayeredPane layeredPane = findLayeredPane(this);
        layeredPane.setLayer(embeddedPanel, Integer.MIN_VALUE);
        // Hack to reparent without the native component to be disposed
//        layeredPane.add(embeddedPanel);
        layeredPane.setComponentZOrder(embeddedPanel, 0);
        layeredPane.revalidate();
        layeredPane.repaint();
        revalidate();
        repaint();
      } catch(RuntimeException e) {
        super.removeNotify();
        throw e;
      }
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
