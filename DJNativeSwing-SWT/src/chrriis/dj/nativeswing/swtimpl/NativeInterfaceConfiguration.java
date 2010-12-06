/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration of the native interface. It is not constructed directly but obtained from the native interface.
 * @author Christopher Deckers
 */
public class NativeInterfaceConfiguration {

  NativeInterfaceConfiguration() {
  }

  private boolean isNativeSideRespawnedOnError = true;
  private List<Class<?>> nativeClassPathReferenceClassList = new ArrayList<Class<?>>();
  private List<String> nativeClassPathReferenceResourceList = new ArrayList<String>();
  private String[] peerVMParams;
  private PeerVMProcessFactory peerVMProcessFactory;

  /**
   * Set the peer VM process factory which allows to override the default peer VM creation mechanism.
   * This method can be used when the library is compiled to native and a different binary with alternate parameters needs to be called.
   * @param peerVMProcessFactory The factory to set.
   */
  public void setPeerVMProcessFactory(PeerVMProcessFactory peerVMProcessFactory) {
    this.peerVMProcessFactory = peerVMProcessFactory;
  }

  /**
   * Get the peer VM process factory.
   * @return the peer VM process factory or null if it is not set (in which case the default mechanism is used).
   */
  public PeerVMProcessFactory getPeerVMProcessFactory() {
    return peerVMProcessFactory;
  }

  /**
   * Set whether the native side respawns on error. The default is true.
   * @param isNativeSideRespawnedOnError true if the native side should respawn in case of error, false otherwise.
   */
  public void setNativeSideRespawnedOnError(boolean isNativeSideRespawnedOnError) {
    this.isNativeSideRespawnedOnError = isNativeSideRespawnedOnError;
  }

  /**
   * Indicate if the native side respawns on error.
   * @return true if the native side respawns on error.
   */
  public boolean isNativeSideRespawnedOnError() {
    return isNativeSideRespawnedOnError;
  }

  /**
   * Set the reference classes that are considered to compute the classpath of the native side.
   * @param nativeClassPathReferenceClasses the classes to use as references when computing the classpath of the native side.
   */
  public void addNativeClassPathReferenceClasses(Class<?>... nativeClassPathReferenceClasses) {
    nativeClassPathReferenceClassList.addAll(Arrays.asList(nativeClassPathReferenceClasses));
  }

  /**
   * Get the reference classes that are considered to compute the classpath of the native side.
   * @return the classes used as references when computing the classpath of the native side.
   */
  Class<?>[] getNativeClassPathReferenceClasses() {
    return nativeClassPathReferenceClassList.toArray(new Class<?>[0]);
  }

  /**
   * Set the reference resources that are considered to compute the classpath of the native side.
   * @param nativeClassPathReferenceResources the resources to use as references when computing the classpath of the native side.
   */
  public void addNativeClassPathReferenceResources(String... nativeClassPathReferenceResources) {
    nativeClassPathReferenceResourceList.addAll(Arrays.asList(nativeClassPathReferenceResources));
  }

  /**
   * Get the reference resources that are considered to compute the classpath of the native side.
   * @return the resources used as references when computing the classpath of the native side.
   */
  String[] getNativeClassPathReferenceResources() {
    return nativeClassPathReferenceResourceList.toArray(new String[0]);
  }

  /**
   * Set the parameters to add when creating the virtual machine for the native side.
   * @param peerVMParams the parameters to add when creating the virtual machine for the native side.
   */
  public void setPeerVMParams(String... peerVMParams) {
    this.peerVMParams = peerVMParams;
  }

  /**
   * Get the parameters added when creating the virtual machine for the native side.
   * @return the parameters added when creating the virtual machine for the native side.
   */
  String[] getPeerVMParams() {
    return peerVMParams;
  }

}