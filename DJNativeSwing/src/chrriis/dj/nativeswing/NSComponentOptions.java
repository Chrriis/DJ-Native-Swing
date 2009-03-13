/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;


/**
 * A collection of options that can be used for components.
 * @author Christopher Deckers
 */
public abstract class NSComponentOptions {

  static final String DEACTIVATE_NATIVE_INTEGRATION_OPTION_KEY = "Deactivate Native Integration";
  private static final NSOption DEACTIVATE_NATIVE_INTEGRATION_OPTION = new NSOption(DEACTIVATE_NATIVE_INTEGRATION_OPTION_KEY);

  /**
   * Create an option to deactivate the native integration provided by the library. This option can be useful when the library is used but when its handling should be turned off for certain call paths.
   * @return the option to deactivate the native integration.
   */
  public static NSOption deactivateNativeIntegration() {
    return DEACTIVATE_NATIVE_INTEGRATION_OPTION;
  }

  static final String DESTROY_ON_FINALIZATION_OPTION_KEY = "Destroy On Finalization";
  private static final NSOption DESTROY_ON_FINALIZATION_OPTION = new NSOption(DESTROY_ON_FINALIZATION_OPTION_KEY);

  /**
   * Create an option to defer the destruction of the component until finalization or explicit disposal, rather than when the component is removed from its component tree. This options activates the component hierarchy proxying option.
   * @return the option to destroy on finalization.
   */
  public static NSOption destroyOnFinalization() {
    return DESTROY_ON_FINALIZATION_OPTION;
  }

  static final String PROXY_COMPONENT_HIERARCHY_OPTION_KEY = "Proxy Component Hierarchy";
  private static final NSOption PROXY_COMPONENT_HIERARCHY_OPTION = new NSOption(PROXY_COMPONENT_HIERARCHY_OPTION_KEY);

  /**
   * Create an option to proxy the component hierarchy, which allows to change the component Z-order.
   * @return the option to proxy the component hierarchy.
   */
  public static NSOption proxyComponentHierarchy() {
    return PROXY_COMPONENT_HIERARCHY_OPTION;
  }

  static final String CONSTRAIN_VISIBILITY_OPTION_KEY = "Constrain Visibility";
  private static final NSOption CONSTRAIN_VISIBILITY_OPTION = new NSOption(CONSTRAIN_VISIBILITY_OPTION_KEY);

  /**
   * Create an option to apply visibility constraints to the component, which allows mixing lightweight and heavyweight components to a certain extent.
   * @return the option to constrain the visibility.
   */
  public static NSOption constrainVisibility() {
    return CONSTRAIN_VISIBILITY_OPTION;
  }

}
