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
 * A convenience class for Swing wrappers of native components.
 * @author Christopher Deckers
 */
public class NSPanelComponent extends JPanel implements NSComponent {

  private NativeComponent nativeComponent;
  
  public NSPanelComponent() {
    super(new BorderLayout(0, 0));
  }
  
  protected void setNativeComponent(NativeComponent nativeComponent) {
    this.nativeComponent = nativeComponent;
  }
  
  public NativeComponent getNativeComponent() {
    return nativeComponent;
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
  
}
