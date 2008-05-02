/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * A convenience Swing component superclass, for Swing wrappers of native components.
 * @author Christopher Deckers
 */
public abstract class NSPanelComponent extends JPanel implements NSComponent {

  private NativeComponent nativeComponent;
  
  /**
   * Construct an NativeSwing panel-based component, which by default has a border layout with no margins.
   */
  public NSPanelComponent() {
    super(new BorderLayout(0, 0));
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
