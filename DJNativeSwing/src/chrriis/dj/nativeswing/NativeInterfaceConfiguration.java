/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

/**
 * Configuration of the native interface. It is not constructed directly but obtained from the native interface.
 * @author Christopher Deckers
 */
public class NativeInterfaceConfiguration {
  
  NativeInterfaceConfiguration() {
  }
  
  private boolean isNativeSideRespawnedOnError = true;
  private Class<?>[] nativeClassPathReferenceClasses;
  private String[] nativeClassPathReferenceResources;
  private String[] peerVMParams;
  
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
  public void setNativeClassPathReferenceClasses(Class<?>... nativeClassPathReferenceClasses) {
    this.nativeClassPathReferenceClasses = nativeClassPathReferenceClasses;
  }
  
  /**
   * Get the reference classes that are considered to compute the classpath of the native side.
   * @return the classes used as references when computing the classpath of the native side.
   */
  public Class<?>[] getNativeClassPathReferenceClasses() {
    return nativeClassPathReferenceClasses;
  }
  
  /**
   * Set the reference resources that are considered to compute the classpath of the native side.
   * @param nativeClassPathReferenceResources the resources to use as references when computing the classpath of the native side.
   */
  public void setNativeClassPathReferenceResources(String... nativeClassPathReferenceResources) {
    this.nativeClassPathReferenceResources = nativeClassPathReferenceResources;
  }
  
  /**
   * Get the reference resources that are considered to compute the classpath of the native side.
   * @return the resources used as references when computing the classpath of the native side.
   */
  public String[] getNativeClassPathReferenceResources() {
    return nativeClassPathReferenceResources;
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
  public String[] getPeerVMParams() {
    return peerVMParams;
  }
  
}