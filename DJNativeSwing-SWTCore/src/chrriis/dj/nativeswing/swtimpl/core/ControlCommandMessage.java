/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.core;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Control;

import chrriis.common.ObjectRegistry;
import chrriis.dj.nativeswing.swtimpl.CommandMessage;
import chrriis.dj.nativeswing.swtimpl.NativeComponent;

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
    componentID = (Integer)control.getData("NS_ID");
    setTargetNativeSide(false);
  }

  /**
   * Set the native component that is used to identify the control on the native side.
   * @param nativeComponent the native component.
   */
  public void setNativeComponent(NativeComponent nativeComponent) {
    componentID = ((SWTNativeComponent)nativeComponent).getComponentID();
    setTargetNativeSide(true);
  }

  private transient Boolean isTargetNativeSide;

  private boolean isTargetNativeSide() {
    if(isTargetNativeSide == null) {
      throw new IllegalStateException("The target must be specified!");
    }
    return isTargetNativeSide;
  }

  /**
   * Indicate whether the message is to be sent to the native side or the Swing side.
   * @param isTargetNativeSide true if the target is the native side, false otherwise.
   */
  private void setTargetNativeSide(boolean isTargetNativeSide) {
    this.isTargetNativeSide = isTargetNativeSide;
  }

  /**
   * Get the control, which is only valid when in the native context.
   * @return the control, or null.
   */
  public Control getControl() {
    ObjectRegistry controlRegistry = SWTNativeComponent.getControlRegistry();
    return controlRegistry == null? null: (Control)controlRegistry.get(componentID);
  }

  /**
   * Get the native component, which is only valid when in the local context.
   * @return the native component, or null.
   */
  public NativeComponent getNativeComponent() {
    ObjectRegistry nativeComponentRegistry = SWTNativeComponent.getNativeComponentRegistry();
    return nativeComponentRegistry == null? null: (NativeComponent)nativeComponentRegistry.get(componentID);
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

  private Object syncExec(Object... args) {
    return syncExec(isTargetNativeSide(), args);
  }

  @Override
  public Object syncExec(boolean isTargetNativeSide, Object... args) {
    checkComponentID();
    return super.syncExec(isTargetNativeSide, args);
  }

  private void asyncExec(Object... args) {
    super.asyncExec(isTargetNativeSide(), args);
  }

  @Override
  public void asyncExec(boolean isTargetNativeSide, Object... args) {
    checkComponentID();
    super.asyncExec(isTargetNativeSide, args);
  }

  private void checkComponentID() {
    if(componentID == 0) {
      throw new IllegalStateException("The component was not specified!");
    }
  }

  static class DisposedControlException extends IllegalStateException {
    public DisposedControlException(Throwable t) {
      super("Widget is disposed", t);
    }
  }

  @Override
  protected Object runCommand() throws Exception {
    try {
      return super.runCommand();
    } catch(RuntimeException e) {
      SWTNativeInterface nativeInterface = SWTNativeInterface.getInstance();
      if(nativeInterface.isInProcess_() || nativeInterface.isOutProcessNativeSide_()) {
        for(Throwable ex=e; ex != null; ex = ex.getCause()) {
          if(ex instanceof SWTException && ((SWTException)ex).code == SWT.ERROR_WIDGET_DISPOSED) {
            throw new DisposedControlException(ex);
          }
        }
      }
      throw e;
    }
  }

  @Override
  protected boolean isValid() {
    SWTNativeInterface nativeInterface = SWTNativeInterface.getInstance();
    if(nativeInterface.isInProcess_()) {
      return getControl() != null || getNativeComponent() != null;
    }
    if(nativeInterface.isOutProcessNativeSide_()) {
      return getControl() != null;
    }
    return getNativeComponent() != null;
  }

}