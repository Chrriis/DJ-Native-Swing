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
import java.lang.reflect.Method;
import java.util.Hashtable;

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

    private NativeComponentWrapper nativeComponentWrapper;

    public EmbeddedPanel(NativeComponentWrapper nativeComponentWrapper) {
      super(new BorderLayout());
      this.nativeComponentWrapper = nativeComponentWrapper;
      enableEvents(MouseWheelEvent.MOUSE_WHEEL_EVENT_MASK);
    }

    private boolean isHiddenReparenting;
    private boolean isRemovingFromParent;

//    @Override
//    public void addNotify() {
//      super.addNotify();
//      if(isHiddenReparenting) {
//        isHiddenReparenting = false;
//        nativeComponentWrapper.restoreFromHiddenParent();
//      }
//    }

    @Override
    public void removeNotify() {
      super.removeNotify();
      if(!isRemovingFromParent) {
        Container parent = getParent();
        if(parent != null) {
          isRemovingFromParent = true;
          parent.remove(this);
          parent.invalidate();
          parent.validate();
          isRemovingFromParent = false;
        }
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

  }

  private EmbeddedPanel embeddedPanel;
  private boolean isProxied;

  @Override
  public void addNotify() {
    super.addNotify();
    // This call throws a runtime exception if the hierarchy is not compatible
    JLayeredPane layeredPane = findLayeredPane(this);
    if(embeddedPanel != null && embeddedPanel.isHiddenReparenting) {
      layeredPane.setLayer(embeddedPanel, Integer.MIN_VALUE);
      layeredPane.add(embeddedPanel);
      layeredPane.invalidate();
      layeredPane.validate();
      nativeComponentWrapper.restoreFromHiddenParent();
      embeddedPanel.isHiddenReparenting = false;
    }
    boolean isEmbeddedPanelCreated = embeddedPanel != null;
    if(isEmbeddedPanelCreated) {
      JLayeredPane oldLayeredPane = findLayeredPane(embeddedPanel);
      if(layeredPane != oldLayeredPane) {
        nativeComponentWrapper.storeInHiddenParent();
        Container oldParent = embeddedPanel.getParent();
        oldParent.remove(embeddedPanel);
        UIUtils.revalidate(oldParent);
        oldParent.repaint();
        layeredPane.setLayer(embeddedPanel, Integer.MIN_VALUE);
        layeredPane.add(embeddedPanel);
        nativeComponentWrapper.restoreFromHiddenParent();
        UIUtils.revalidate(layeredPane);
        layeredPane.repaint();
        revalidate();
        repaint();
      }
    } else {
      embeddedPanel = new EmbeddedPanel(nativeComponentWrapper);
      embeddedPanel.add(nativeComponentWrapper.getNativeComponent(), BorderLayout.CENTER);
    }
    isProxied = false;
    JComponent oldParent = (JComponent)embeddedPanel.getParent();
    if(oldParent != this) {
      if(oldParent == null) {
        add(embeddedPanel);
      } else {
        // Hack to reparent without the native component to be disposed
        setComponentZOrder(embeddedPanel, 0);
        if(oldParent instanceof JLayeredPane) {
          try {
            // setComponentZOrder calls removeDelicately, which does not clean up the componentToLayer Hashtable of a JLayeredPane for heavyweight containers like remove does.
            Method getComponentToLayerMethod = JLayeredPane.class.getDeclaredMethod("getComponentToLayer");
            getComponentToLayerMethod.setAccessible(true);
            ((Hashtable<?, ?>)getComponentToLayerMethod.invoke(oldParent)).remove(embeddedPanel);
          } catch(Throwable e) {
            // If it does not work, remain silent as it may not be a problem depending on the VM
          }
        }
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
    try {
      if(embeddedPanel != null) {
        // We try to support the case where a component is placed in a window that gets disposed, and then added to a different window.
        nativeComponentWrapper.storeInHiddenParent();
        embeddedPanel.isHiddenReparenting = true;
      }
    } catch(Exception e) {
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
        } catch(RuntimeException ex) {
          super.removeNotify();
          throw ex;
        }
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
      panel.isRemovingFromParent = true;
      parent.remove(panel);
      parent.invalidate();
      parent.validate();
      parent.repaint();
    }
  }

}
