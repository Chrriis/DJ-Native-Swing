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
public interface NSComponentOptions {

  /**
   * An option to defer the destruction of the component until finalization or explicit disposal, rather than when the component is removed from its component tree. This options activates the component hierarchy proxying option.
   */
  public static final NSOption DESTROY_ON_FINALIZATION = new NSOption("Destruction Time");
  
  /**
   * An option to proxy the component hierarchy, which allows to change the component Z-order.
   */
  public static final NSOption PROXY_COMPONENT_HIERARCHY = new NSOption("Filiation Type");
  
  /**
   * An option to apply visibility constraints to the component, which allows mixing lightweight and heavyweight components to a certain extent.
   */
  public static final NSOption CONSTRAIN_VISIBILITY = new NSOption("Visibility Constraint");
  
}
