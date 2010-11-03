/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chrriis.common.ObjectRegistry;

/**
 * A native component that gets connected to a native peer.
 * @author Christopher Deckers
 */
public abstract class NativeComponent extends Canvas {

  /**
   * Run a command in sequence with other calls from this class. Calls are performed only when the component is initialized, and this method adds to the queue of calls in case it is not.
   * @param runnable the command to run in sequence with other method calls.
   */
  public abstract void runInSequence(Runnable runnable);

  /**
   * Run the given command if the control is created, or store it to run it when the creation occurs.
   * If the component is disposed before the command has a chance to run, it is ignored silently.
   * @param commandMessage the command message to run.
   * @param args the arguments to pass to the command message.
   * @return the result of running the message, or null if the message is queued.
   */
  public abstract Object runSync(CommandMessage commandMessage, Object... args);

  /**
   * Run the given command if the control is created, or store it to run it when the creation occurs.
   * If the component is disposed before the command has a chance to run, it is ignored silently.
   * @param commandMessage the command message to run.
   * @param args the arguments to pass to the command message.
   */
  public abstract void runAsync(CommandMessage commandMessage, Object... args);

  private static ObjectRegistry nativeComponentRegistry;
  private static ObjectRegistry controlRegistry;

  static {
    if(NativeInterface.isInProcess()) {
      nativeComponentRegistry = new ObjectRegistry();
      controlRegistry = new ObjectRegistry();
    } else {
      if(NativeInterface.isOutProcessNativeSide()) {
        controlRegistry = new ObjectRegistry();
      } else {
        nativeComponentRegistry = new ObjectRegistry();
      }
    }
  }

  /**
   * Get the native components that are currently registered, which may have an invalid native peer.
   * @return The currently registered native components, which may have an invalid native peer.
   */
  public static NativeComponent[] getNativeComponents() {
    List<NativeComponent> nativeComponentList = new ArrayList<NativeComponent>();
    for(int instanceID: nativeComponentRegistry.getInstanceIDs()) {
      NativeComponent nativeComponent = (NativeComponent)nativeComponentRegistry.get(instanceID);
      if(nativeComponent != null) {
        nativeComponentList.add(nativeComponent);
      }
    }
    return nativeComponentList.toArray(new NativeComponent[0]);
  }

  /**
   * Get the registry of the components, which references created components using the component ID.
   * @return the registry.
   */
  protected static ObjectRegistry getNativeComponentRegistry() {
    return nativeComponentRegistry;
  }

  /**
   * Get the registry of the controls, which references created controls using the component ID.
   * @return the registry.
   */
  protected static ObjectRegistry getControlRegistry() {
    return controlRegistry;
  }

  /**
   * Get the unique identifier of this native component, used as a reference to communicate with the native peer.
   * @return the component ID.
   */
  protected abstract int getComponentID();

  /**
   * Force the component to initialize. All method calls will then be synchronous instead of being queued waiting for the componant to be initialized.
   * This call fails if the component is not in a component hierarchy with a Window ancestor.
   */
  public abstract void initializeNativePeer();

  /**
   * Get the parameters that are passed to the creation method. This method can be overriden by subclasses to pass additional information necessary for the native peer creation.
   * @return the parameters.
   */
  protected abstract Object[] getNativePeerCreationParameters();

  /**
   * Explicitely dispose the native resources. This is particularly useful if deferred destruction is used (cf native component options) and the component is not going to be used anymore.
   */
  protected abstract void disposeNativePeer();

  /**
   * Indicate whether the native peer is disposed.
   * @return true if the native peer is disposed. This method returns false if the native peer is not initialized.
   */
  public abstract boolean isNativePeerDisposed();

  /**
   * Indicate whether the native peer initialization phase has happened. This method returns true even if the native peer is disposed or if the creation of the peer failed.
   * @return true if the native peer is initialized.
   */
  public abstract boolean isNativePeerInitialized();

  /**
   * Indicate if the native peer is valid, which means initialized, not disposed, and alive (communication channel is alive).
   * @return true if the native peer is valid.
   */
  public abstract boolean isNativePeerValid();

  /**
   * A native component instance cannot be added directly to a component hierarchy. This method needs to be called to get a component that will add the native component.
   * @param optionMap the options to configure the behavior of this component.
   * @return the component that contains the native component and that can be added to the component hierarchy.
   */
  protected abstract Component createEmbeddableComponent(Map<Object, Object> optionMap);

  /**
   * Paint the native component including its native peer in an image. This method can be called from a non-UI thread.
   * @param image the image to paint to.
   */
  public abstract void paintComponent(BufferedImage image);

  /**
   * Paint the native component including its native peer in an image, in the areas that are specified. This method can be called from a non-UI thread.
   * @param image the image to paint to.
   * @param rectangles the area in which the component should be painted.
   */
  public abstract void paintComponent(BufferedImage image, Rectangle[] rectangles);

  /**
   * Create an image of the native peer as a back buffer, which can be used when painting the component, or to simulate alpha blending.
   */
  public abstract void createBackBuffer();

  /**
   * Indicate whether a back buffer is (still) stored in the component.
   * @return true if a back buffer is still held, false otherwise.
   */
  public abstract boolean hasBackBuffer();

  /**
   * Update the back buffer on the areas that have non opaque overlays and that are not covered by opaque components.
   */
  public abstract void updateBackBufferOnVisibleTranslucentAreas();

  /**
   * Update (eventually creating an empty one if it does not exist) the back buffer on the area specified by the rectangles.
   * @param rectangles the area to update.
   */
  public abstract void updateBackBuffer(Rectangle[] rectangles);

  /**
   * Destroy the back buffer.
   */
  public abstract void destroyBackBuffer();

}
