/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import org.eclipse.swt.widgets.Control;

/**
 * A message that make the link between a local component and a native peer control.
 * @author Christopher Deckers
 */
public abstract class ControlCommandMessage extends CommandMessage {

  private int componentID;
  
  int getComponentID() {
    return componentID;
  }
  
  /**
   * Set the control that is used to identify the control on the local side.
   * @param control the control.
   */
  public void setControl(Control control) {
    this.componentID = (Integer)control.getData("NS_ID");
  }
  
  /**
   * Set the native component that is used to identify the control on the native side.
   * @param nativeComponent the native component.
   */
  public void setNativeComponent(NativeComponent nativeComponent) {
    this.componentID = nativeComponent.getComponentID();
  }
  
  /**
   * Get the control, which is only valid when in the native context.
   * @return the control, or null.
   */
  public Control getControl() {
    return (Control)NativeComponent.registry.get(componentID);
  }
  
  /**
   * Get the native component, which is only valid when in the local context.
   * @return the native component, or null.
   */
  public NativeComponent getNativeComponent() {
    return (NativeComponent)NativeComponent.registry.get(componentID);
  }
  
  @Override
  protected boolean isValid() {
    if(NativeInterface.isNativeSide()) {
      return getControl() != null;
    }
    return getNativeComponent() != null;
  }

}