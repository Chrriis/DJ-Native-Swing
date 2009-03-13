/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import chrriis.dj.nativeswing.NSComponentOptions;
import chrriis.dj.nativeswing.NSOption;

/**
 * A convenience Swing component superclass, for Swing wrappers of native components.
 * @author Christopher Deckers
 */
public abstract class NSPanelComponent extends JPanel implements NSComponent {

  /**
   * Create an option to defer the destruction of the component until finalization or explicit disposal, rather than when the component is removed from its component tree. This options activates the component hierarchy proxying option.
   * @return the option to destroy on finalization.
   */
  public static NSOption destroyOnFinalization() {
    return NSComponentOptions.destroyOnFinalization();
  }

  /**
   * Create an option to proxy the component hierarchy, which allows to change the component Z-order.
   * @return the option to proxy the component hierarchy.
   */
  public static NSOption proxyComponentHierarchy() {
    return NSComponentOptions.proxyComponentHierarchy();
  }

  /**
   * Create an option to apply visibility constraints to the component, which allows mixing lightweight and heavyweight components to a certain extent.
   * @return the option to constrain the visibility.
   */
  public static NSOption constrainVisibility() {
    return NSComponentOptions.constrainVisibility();
  }

  private NativeComponent nativeComponent;

  /**
   * Construct an NativeSwing panel-based component, which by default has a border layout with no margins.
   */
  public NSPanelComponent() {
    super(new BorderLayout());
  }

  /**
   * Initialize this class with its native component.
   * @param nativeComponent the native component that this component relates to.
   */
  protected void initialize(NativeComponent nativeComponent) {
    if(this.nativeComponent != null) {
      throw new IllegalStateException("The native component is already initialized!");
    }
    this.nativeComponent = nativeComponent;
  }

  public void initializeNativePeer() {
    nativeComponent.initializeNativePeer();
  }

  public void disposeNativePeer() {
    nativeComponent.disposeNativePeer();
  }

  public boolean isNativePeerDisposed() {
    return nativeComponent.isNativePeerDisposed();
  }

  public boolean isNativePeerInitialized() {
    return nativeComponent.isNativePeerInitialized();
  }

  public boolean isNativePeerValid() {
    return nativeComponent.isNativePeerValid();
  }

  public void runInSequence(Runnable runnable) {
    nativeComponent.runInSequence(runnable);
  }

  /**
   * Get the native component.
   * @return the native component.
   */
  public NativeComponent getNativeComponent() {
    return nativeComponent;
  }

}
