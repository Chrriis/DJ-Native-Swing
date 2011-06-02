/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.security.AccessController;
import java.security.PrivilegedAction;

import chrriis.common.Utils;

/**
 * A class that exposes all the system properties used by DJ Native Swing.
 * @author Christopher Deckers
 * @author Stefan Fussenegger
 */
public enum NSSystemProperty {

  /**
   * nativeswing.localhostAddress
   * = &lt;String&gt; or "_localhost_" (default: auto-detect, usually 127.0.0.1)<br/>
   * Set the address that is used as the local host address for all the internal
   * communication channels that require a sockets (local web server, etc.).
   */
  LOCALHOSTADDRESS("nativeswing.localhostAddress", Type.READ_WRITE),

  /**
   * nativeswing.localhostAddress.debug.printDetection
   * = true/false (default: false)<br/>
   * Set whether to print the steps of local host address detection.
   */
  LOCALHOSTADDRESS_DEBUG_PRINTDETECTION("nativeswing.localhostAddress.debug.printDetection", Type.READ_WRITE),

  /**
   * nativeswing.localhostAddress.debug.print
   * = true/false (default: false)<br/>
   * Set whether the address found as the local host address should be printed.
   */
  LOCALHOSTADDRESS_DEBUG_PRINT("nativeswing.localhostAddress.debug.print", Type.READ_WRITE),


  /**
   * nativeswing.webserver.debug.printPort
   * = true/false (default: false)<br/>
   * Set whether the port that is used by the embedded web server should be
   * printed.
   */
  WEBSERVER_DEBUG_PRINTPORT("nativeswing.webserver.debug.printPort", Type.READ_WRITE),

  /**
   * nativeswing.webserver.debug.printRequests
   * = true/false (default: false)<br/>
   * Set whether the web server should print the requests it receives, along with
   * the result (200 or 404).
   */
  WEBSERVER_DEBUG_PRINTREQUESTS("nativeswing.webserver.debug.printRequests", Type.READ_WRITE),

  /**
   * nativeswing.webserver.debug.printData
   * = true/false/<long> (default: false)<br/>
   * Set whether the web server should print the data it sends (or how many bytes to
   * print).
   */
  WEBSERVER_DEBUG_PRINTDATA("nativeswing.webserver.debug.printData", Type.READ_WRITE),

  WEBSERVER_ACTIVATEOLDRESOURCEMETHOD("nativeswing.webserver.activateOldResourceMethod", Type.READ_WRITE),


  /**
   * nativeswing.components.debug.printOptions
   * = true/false (default: false)<br/>
   * Set whether the options used to create a component should be printed.
   */
  COMPONENTS_DEBUG_PRINTOPTIONS("nativeswing.components.debug.printOptions", Type.READ_WRITE),

  /**
   * nativeswing.components.debug.printShapeComputing
   * = true/false (default: false)<br/>
   * Set whether the computation of the shape applied to the native component (when
   * the visibility constraint option is active) should be printed.
   */
  COMPONENTS_DEBUG_PRINTSHAPECOMPUTING("nativeswing.components.debug.printShapeComputing", Type.READ_WRITE),

  /**
   * nativeswing.components.forceSingleRectangleShapes
   * = true/false (default: false)
   * Certain platforms only support shapes for native components that resolve to
   * single rectangles. Developers who want to develop on a different platform with
   * the same constraint can force that behavior for consistency.
   */
  COMPONENTS_FORCESINGLERECTANGLESHAPES("nativeswing.components.forceSingleRectangleShapes", Type.READ_WRITE),


  /**
   * nativeswing.integration.active
   * = true/false (default: true)<br/>
   * Set whether native integration should be active.
   */
  INTEGRATION_ACTIVE("nativeswing.integration.active", Type.READ_WRITE),


  /**
   * nativeswing.dependencies.checkVersions
   * = true/false (default: true)<br/>
   * Set whether the versions of the dependencies should be checked when possible.
   */
  DEPENDENCIES_CHECKVERSIONS("nativeswing.dependencies.checkVersions", Type.READ_WRITE),


  JNA_FORCE_HW_POPUP("jna.force_hw_popups", Type.READ_WRITE),
  DEPLOYMENT_TYPE("nativeswing.deployment.type", Type.READ_WRITE),
  INTEGRATION_USEDEFAULTCLIPPING("nativeswing.integration.useDefaultClipping", Type.READ_WRITE),

  ;

  //private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
  //    .getLogger(SystemProperty.class);

  /**
   * Type of property (only used in constructor for readability)
   */
  private enum Type {
    READ_WRITE, READ_ONLY;
  }

  private final String _name;
  private final boolean _readOnly;

  private NSSystemProperty(String name) {
    this(name, Type.READ_ONLY);
  }

  private NSSystemProperty(String name, Type type) {
    if (name == null) {
      throw new NullPointerException("name");
    }
    name = name.trim();
    if ("".equals(name)) {
      throw new IllegalArgumentException();
    }

    _name = name;
    _readOnly = type == Type.READ_ONLY;
  }

  /**
   * @see System#getProperty(String)
   * @see AccessController#doPrivileged(PrivilegedAction)
   * @return the string value of the system property, or <code>null</code> if there is no property with that key.
   */
  public String get() {
    return get(null);
  }

  /**
   * @param defaultValue the default value to return if the property is not defined.
   * @see System#getProperty(String)
   * @see AccessController#doPrivileged(PrivilegedAction)
   * @return the string value of the system property, or the default value if there is no property with that key.
   */
  public String get(final String defaultValue) {
    return AccessController.doPrivileged(new PrivilegedAction<String>() {
      public String run() {
        String name = getName();
        String value = System.getProperty(name);
        if(value != null) {
          return value;
        }
        if(Utils.IS_WEBSTART) {
          value = System.getProperty("jnlp." + name);
          if(value != null) {
            return value;
          }
        }
        return defaultValue;
      }
    });
  }

  /**
   * @param value the new value
   * @see System#setProperty(String, String)
   * @see AccessController#doPrivileged(PrivilegedAction)
   * @see #isReadOnly()
   * @exception UnsupportedOperationException if this property is read-only
   */
  public String set(final String value) {
    if (isReadOnly()) {
      throw new UnsupportedOperationException(getName() + " is a read-only property");
    }

    return AccessController.doPrivileged(new PrivilegedAction<String>() {
      public String run() {
        return System.setProperty(getName(), value);
      }
    });
  }

  /**
   * @return name of this property
   */
  public String getName() {
    return _name;
  }

  /**
   * whether this property should not be modified (i.e. "read-only"). Note
   * that it is possible to use {@link System#setProperty(String, String)}
   * directly. It's use is discouraged though (as it might not have the
   * desired effect)
   *
   * @return <code>true</code> if this property should not be modified
   */
  public boolean isReadOnly() {
    return _readOnly;
  }

  /**
   * @return property value (same as {@link #get()}
   * @see #get()
   */
  @Override
  public String toString() {
    return get();
  }

  /**
   * @return a string representation of this object (e.g. "OS_NAME: os.name=Linux (read-only)")
   */
  public String toDebugString() {
    StringBuilder buf = new StringBuilder();
    buf.append(name()).append(": ");
    buf.append(getName()).append("=");
    buf.append(get());
    if (isReadOnly()) {
      buf.append(" (read-only)");
    }
    return buf.toString();
  }

}
