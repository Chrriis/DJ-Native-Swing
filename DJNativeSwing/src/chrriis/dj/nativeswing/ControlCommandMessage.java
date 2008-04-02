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
 * A message that makes the link between a native component on the local side and its native peer control.
 * @author Christopher Deckers
 */
public abstract class ControlCommandMessage extends CommandMessage {

  /**
   * Construct a control command message.
   */
  public ControlCommandMessage() {
  }
  
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
    return (Control)NativeComponent.getRegistry().get(componentID);
  }
  
  /**
   * Get the native component, which is only valid when in the local context.
   * @return the native component, or null.
   */
  public NativeComponent getNativeComponent() {
    return (NativeComponent)NativeComponent.getRegistry().get(componentID);
  }
  
  /**
   * Execute that message asynchronously with the given arguments.
   * @param nativeComponent the native component.
   * @param args the arguments, which must be serializable.
   */
  public void asyncExec(NativeComponent nativeComponent, Object... args) {
    setNativeComponent(nativeComponent);
    asyncExec(args);
  }
  
  /**
   * Execute that message asynchronously with the given arguments.
   * @param control the native component.
   * @param args the arguments, which must be serializable.
   */
  public void asyncExec(Control control, Object... args) {
    setControl(control);
    asyncExec(args);
  }
  
  /**
   * Execute that message synchronously with the given arguments and return the result.
   * @param nativeComponent the native component.
   * @param args the arguments, which must be serializable.
   * @return the result of the execution.
   */
  public Object syncExec(NativeComponent nativeComponent, Object... args) {
    setNativeComponent(nativeComponent);
    return syncExec(args);
  }
  
  /**
   * Execute that message synchronously with the given arguments and return the result.
   * @param control the control.
   * @param args the arguments, which must be serializable.
   * @return the result of the execution.
   */
  public Object syncExec(Control control, Object... args) {
    setControl(control);
    return syncExec(args);
  }
  
  @Override
  public Object syncExec(Object... args) {
    if(componentID == 0) {
      if(NativeInterface.isNativeSide()) {
        throw new IllegalStateException("The control was not specified!");
      }
      throw new IllegalStateException("The native component was not specified!");
    }
    return super.syncExec(args);
  }
  
  @Override
  public void asyncExec(Object... args) {
    if(componentID == 0) {
      if(NativeInterface.isNativeSide()) {
        throw new IllegalStateException("The control was not specified!");
      }
      throw new IllegalStateException("The native component was not specified!");
    }
    super.asyncExec(args);
  }
  
  @Override
  protected boolean isValid() {
    if(NativeInterface.isNativeSide()) {
      return getControl() != null;
    }
    return getNativeComponent() != null;
  }
  
}