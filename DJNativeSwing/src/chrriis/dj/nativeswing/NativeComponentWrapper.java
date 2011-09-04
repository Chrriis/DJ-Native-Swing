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
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;

import javax.swing.SwingUtilities;

/**
 * A wrapper for a native component, so that it becomes part of the Native Swing framework.
 * This class should be used by developers who want to have some native component be properly integrated in Swing applications.
 * @author Christopher Deckers
 */
public class NativeComponentWrapper {

  private static final boolean IS_DEBUGGING_OPTIONS = Boolean.parseBoolean(NSSystemProperty.COMPONENTS_DEBUG_PRINTOPTIONS.get());

  private boolean isRegistered;

  private void checkParent() {
    Container parent = nativeComponent.getParent();
    if(parent != null && !(parent instanceof NativeComponentHolder)) {
      throw new IllegalStateException("The native component cannot be added directly! Use the createEmbeddableComponent() method to get a component that can be added.");
    }
    if(parent != null && SwingUtilities.getWindowAncestor(parent) != null) {
      // This condition should always be true, unless integration is turned off.
      if(!isRegistered) {
        NativeSwing.addNativeComponentWrapper(this);
        isRegistered = true;
      }
    } else if(isRegistered) {
      // the component may be marked as registered but not being really registered if integration is turned off.
      if(NativeSwing.removeNativeComponentWrapper(this)) {
        isRegistered = false;
      }
    }
  }

  /**
   * Construct a NativeComponentWrapper.
   * @param nativeComponent the native component to wrap.
   */
  public NativeComponentWrapper(Component nativeComponent) {
    this.nativeComponent = nativeComponent;
    // Check that it does not have already a parent
    checkParent();
    // Add a check to ensure that its parent is an embeddable component.
    nativeComponent.addHierarchyListener(new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
        long changeFlags = e.getChangeFlags();
        if((changeFlags & HierarchyEvent.PARENT_CHANGED) != 0) {
          checkParent();
        }
      }
    });
  }

  private Component nativeComponent;

  Component getNativeComponent() {
    return nativeComponent;
  }

  /**
   * Paint the native component including its native peer in an image, in the areas that are specified. This method can be called from a non-UI thread.
   * @param image the image to paint to.
   * @param rectangles the area in which the component should be painted, or null to paint everything.
   */
  protected void paintNativeComponent(BufferedImage image, Rectangle[] rectangles) {
  }

  static interface NativeComponentHolder {}

  private Reference<NativeComponentProxy> nativeComponentProxy;

  void setNativeComponentProxy(NativeComponentProxy nativeComponentProxy) {
    if(nativeComponentProxy == null) {
      this.nativeComponentProxy = null;
    } else {
      this.nativeComponentProxy = new WeakReference<NativeComponentProxy>(nativeComponentProxy);
    }
  }

  NativeComponentProxy getNativeComponentProxy() {
    return nativeComponentProxy == null? null: nativeComponentProxy.get();
  }

  static class SimpleNativeComponentHolder extends EmbeddableComponent implements NativeComponentHolder {

    private NativeComponentWrapper nativeComponent;

    public SimpleNativeComponentHolder(NativeComponentWrapper nativeComponent) {
      this.nativeComponent = nativeComponent;
      add(nativeComponent.getNativeComponent());
      enableEvents(MouseWheelEvent.MOUSE_WHEEL_EVENT_MASK);
    }

    @Override
    protected void printComponent(Graphics g) {
      nativeComponent.getNativeComponent().print(g);
    }

  }

  /**
   * This method is invoked to enable or disable the native component at its native level, to prevent focus and input problems. It does nothing by default but should be implemented when possible.
   * @param isEnabled true if the native component should be enabled, false otherwise.
   */
  protected void setNativeComponentEnabled(boolean isEnabled) {
    // Do nothing by default, though it is desirable that subclasses implement it.
  }

  protected boolean isNativeComponentEnabled() {
    return true;
  }

  /**
   * Get a description of this component wrapper, which is used for example to improve debug messages.
   * @return a description of this component wrapper.
   */
  protected String getComponentDescription() {
    return getClass().getName() + "[" + hashCode() + "]";
  }

  /**
   * A native component instance cannot be added directly to a component hierarchy: this method creates a component that will host the native component but which can be added to the component hierarchy.
   * @param options the options to configure the behavior of this component.
   * @return the component that contains the native component and that can be added to the component hierarchy.
   */
  public Component createEmbeddableComponent(NSOption... options) {
    return createEmbeddableComponent(NSOption.createOptionMap(options));
  }

  /**
   * A native component instance cannot be added directly to a component hierarchy: this method creates a component that will host the native component but which can be added to the component hierarchy.
   * @param optionMap the options to configure the behavior of this component.
   * @return the component that contains the native component and that can be added to the component hierarchy.
   */
  public Component createEmbeddableComponent(Map<Object, Object> optionMap) {
    if(IS_DEBUGGING_OPTIONS) {
      StringBuilder sb = new StringBuilder();
      sb.append("NativeComponent ").append(getComponentDescription()).append(" options: ");
      boolean isFirst = true;
      for(Object key: optionMap.keySet()) {
        if(isFirst) {
          isFirst = false;
        } else {
          sb.append(", ");
        }
        Object value = optionMap.get(key);
        if(value instanceof NSOption) {
          sb.append(value);
        } else {
          sb.append(key).append('=').append(value);
        }
      }
      if(isFirst) {
        sb.append("<none>");
      }
      System.err.println(sb);
    }
    String isActive = NSSystemProperty.INTEGRATION_ACTIVE.get();
    if(optionMap.get(NSComponentOptions.DEACTIVATE_NATIVE_INTEGRATION_OPTION_KEY) != null || isActive != null && !Boolean.parseBoolean(isActive)) {
      isRegistered = true;
      return new SimpleNativeComponentHolder(this);
    }
    Boolean deferredDestruction = optionMap.get(NSComponentOptions.DESTROY_ON_FINALIZATION_OPTION_KEY) != null? Boolean.TRUE: null;
    Boolean componentHierarchyProxying = optionMap.get(NSComponentOptions.PROXY_COMPONENT_HIERARCHY_OPTION_KEY) != null? Boolean.TRUE: null;
    Boolean visibilityConstraint = optionMap.get(NSComponentOptions.CONSTRAIN_VISIBILITY_OPTION_KEY) != null? Boolean.TRUE: null;
    if(Boolean.valueOf(NSSystemProperty.INTEGRATION_USEDEFAULTCLIPPING.get()) || visibilityConstraint == null && componentHierarchyProxying == null) {
      if(deferredDestruction != null && componentHierarchyProxying == null) {
        componentHierarchyProxying = true;
      }
      if(Boolean.TRUE.equals(componentHierarchyProxying)) {
        return new NativeComponentProxyFinalizationPanel(this);
      }
      return new SimpleNativeComponentHolder(this);
    }
    boolean isJNAPresent = isJNAPresent();
    if(visibilityConstraint == null) {
      if(isJNAPresent && componentHierarchyProxying != null) {
        visibilityConstraint = true;
      }
    }
    if(visibilityConstraint != null && !isJNAPresent) {
      throw new IllegalStateException("The JNA libraries are required to use the visibility constraints!");
    }
    if(deferredDestruction != null && componentHierarchyProxying == null) {
      componentHierarchyProxying = true;
    }
    if(componentHierarchyProxying != null) {
      return new NativeComponentProxyPanel(this, Boolean.TRUE.equals(visibilityConstraint), Boolean.TRUE.equals(deferredDestruction), Boolean.TRUE.equals(componentHierarchyProxying));
    }
    if(visibilityConstraint == null) {
      return new SimpleNativeComponentHolder(this);
    }
    return new NativeComponentProxyPanel(this, Boolean.TRUE.equals(visibilityConstraint), Boolean.TRUE.equals(deferredDestruction), Boolean.TRUE.equals(componentHierarchyProxying));
  }

  private static boolean isJNAPresent() {
    try {
      Class.forName("com.sun.jna.examples.WindowUtils");
      Class.forName("com.sun.jna.Platform");
      return true;
    } catch(Exception e) {
    }
    return false;
  }

  /**
   * Explicitely dispose the native resources. This is particularly useful if deferred destruction is used (cf native component options) and the component is not going to be used anymore.
   */
  public void disposeNativeComponent() {
    NativeComponentProxy nativeComponentProxy = getNativeComponentProxy();
    if(nativeComponentProxy != null) {
      nativeComponentProxy.dispose();
    }
  }

  private BackBufferManager backBufferManager;

  BackBufferManager getBackBufferManager() {
    NativeComponentProxy nativeComponentProxy = getNativeComponentProxy();
    if(nativeComponentProxy != null) {
      return nativeComponentProxy.getBackBufferManager();
    }
    if(backBufferManager == null) {
      backBufferManager = new BackBufferManager(this, getNativeComponent());
    }
    return backBufferManager;
  }

  /**
   * Paint the back buffer, if one was created, to a graphic context.
   * @param g the graphic context to paint to.
   * @param isPaintingProxy true to paint even if the buffer is handled by a proxy.
   */
  public void paintBackBuffer(Graphics g, boolean isPaintingProxy) {
    NativeComponentProxy nativeComponentProxy = getNativeComponentProxy();
    if(nativeComponentProxy != null) {
      BackBufferManager backBufferManager = nativeComponentProxy.getBackBufferManager();
      if(backBufferManager != null) {
        backBufferManager.paintBackBuffer(g);
      }
      return;
    }
    if(backBufferManager != null) {
      backBufferManager.paintBackBuffer(g);
    }
  }

  /**
   * Indicate whether a back buffer is (still) stored in the component.
   * @return true if a back buffer is still held, false otherwise.
   */
  public boolean hasBackBuffer() {
    NativeComponentProxy nativeComponentProxy = getNativeComponentProxy();
    if(nativeComponentProxy != null) {
      BackBufferManager backBufferManager = nativeComponentProxy.getBackBufferManager();
      if(backBufferManager != null) {
        return backBufferManager.hasBackBuffer();
      }
      return false;
    }
    return backBufferManager != null && backBufferManager.hasBackBuffer();
  }

  /**
   * Create an image of the native peer as a back buffer, which can be used when painting the component, or to simulate alpha blending.
   */
  public void createBackBuffer() {
    getBackBufferManager().createBackBuffer();
  }

  /**
   * Update the back buffer on the areas that have non opaque overlays and that are not covered by opaque components.
   */
  public void updateBackBufferOnVisibleTranslucentAreas() {
    getBackBufferManager().updateBackBufferOnVisibleTranslucentAreas();
  }

  /**
   * Update (eventually creating an empty one if it does not exist) the back buffer on the area specified by the rectangles.
   * @param rectangles the area to update.
   */
  public void updateBackBuffer(Rectangle[] rectangles) {
    getBackBufferManager().updateBackBuffer(rectangles);
  }

  /**
   * Destroy the back buffer.
   */
  public void destroyBackBuffer() {
    getBackBufferManager().destroyBackBuffer();
  }

  /**
   * This method should be invoked by the native component when it wants to transfer the focus.
   * @param isForward true if the focus should be transfered forward, false if it should be backward.
   */
  public void transferFocus(boolean isForward) {
    Component c = getNativeComponentProxy();
    if(c == null) {
      c = getNativeComponent();
    }
    if(isForward) {
      c.transferFocus();
    } else {
      c.transferFocusBackward();
    }
  }

  protected void storeInHiddenParent() {
    throw new IllegalStateException("Storing to a hidden parent is not supported!");
  }

  protected void restoreFromHiddenParent() {
  }

}
