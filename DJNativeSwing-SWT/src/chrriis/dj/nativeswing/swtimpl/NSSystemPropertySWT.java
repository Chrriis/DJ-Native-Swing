/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import java.security.AccessController;
import java.security.PrivilegedAction;

import chrriis.dj.nativeswing.NSSystemProperty;

/**
 * @author Christopher Deckers
 * @author Stefan Fussenegger
 */
public enum NSSystemPropertySWT {

  /* ----------------- NativeSwing properties ----------------- */

  /**
   * nativeswing.localhostaddress
   * = &lt;String&gt; or "_localhost_" (default: auto-detect, usually 127.0.0.1)<br/>
   * Set the address that is used as the local host address for all the internal
   * communication channels that require a sockets (local web server, etc.).
   */
  NATIVESWING_LOCALHOSTADDRESS(NSSystemProperty.NATIVESWING_LOCALHOSTADDRESS),

  /**
   * nativeswing.debug.printlocalhostaddressdetection
   * = true/false (default: false)<br/>
   * Set whether to print the steps of local host address detection.
   */
  NATIVESWING_DEBUG_PRINTLOCALHOSTADDRESSDETECTION(NSSystemProperty.NATIVESWING_DEBUG_PRINTLOCALHOSTADDRESSDETECTION),

  /**
   * nativeswing.debug.printlocalhostaddress
   * = true/false (default: false)<br/>
   * Set whether the address found as the local host address should be printed.
   */
  NATIVESWING_DEBUG_PRINTLOCALHOSTADDRESS(NSSystemProperty.NATIVESWING_DEBUG_PRINTLOCALHOSTADDRESS),


  /**
   * nativeswing.webserver.debug.printport
   * = true/false (default: false)<br/>
   * Set whether the port that is used by the embedded web server should be
   * printed.
   */
  NATIVESWING_WEBSERVER_DEBUG_PRINTREQUESTS(NSSystemProperty.NATIVESWING_WEBSERVER_DEBUG_PRINTREQUESTS),

  /**
   * nativeswing.webserver.debug.printrequests
   * = true/false (default: false)<br/>
   * Set whether the web server should print the requests it receives, along with
   * the result (200 or 404).
   */
  NATIVESWING_WEBSERVER_DEBUG_PRINTPORT(NSSystemProperty.NATIVESWING_WEBSERVER_DEBUG_PRINTPORT),


  /**
   * nativeswing.components.debug.printoptions
   * = true/false (default: false)<br/>
   * Set whether the options used to create a component should be printed.
   */
  NATIVESWING_COMPONENTS_DEBUG_PRINTSHAPECOMPUTING(NSSystemProperty.NATIVESWING_COMPONENTS_DEBUG_PRINTSHAPECOMPUTING),

  /**
   * nativeswing.components.debug.printshapecomputing
   * = true/false (default: false)<br/>
   * Set whether the computation of the shape applied to the native component (when
   * the visibility constraint option is active) should be printed.
   */
  NATIVESWING_COMPONENTS_DEBUG_PRINTOPTIONS(NSSystemProperty.NATIVESWING_COMPONENTS_DEBUG_PRINTOPTIONS),


  /**
   * nativeswing.integration.active
   * = true/false (default: true)<br/>
   * Set whether native integration should be active.
   */
  NATIVESWING_INTEGRATION_ACTIVE(NSSystemProperty.NATIVESWING_INTEGRATION_ACTIVE),


  /* ----------------- NativeSwing-SWT properties ----------------- */

  /**
   * nativeswing.interface.port
   * = &lt;integer&gt; (default: -1)<br/>
   * Force the port to use to communicate with the spawned VM.
   */
  NATIVESWING_INTERFACE_PORT("nativeswing.interface.port", Type.READ_WRITE),

  /**
   * nativeswing.interface.streamresetthreshold
   * = &lt;integer&gt; (default: 500000)<br/>
   * Set the number of bytes that need to be exchanged to trigger a reset of the
   * Object communication stream.
   */
  NATIVESWING_INTERFACE_STREAMRESETTHRESHOLD("nativeswing.interface.streamresetthreshold", Type.READ_WRITE),

  /**
   * nativeswing.interface.syncmessages
   * = true/false (default: false)<br/>
   * Set whether all asynchronous messages should be sent synchronously.
   */
  NATIVESWING_INTERFACE_SYNCMESSAGES("nativeswing.interface.syncmessages", Type.READ_WRITE),

  /**
   * nativeswing.interface.debug.printmessages
   * = true/false (default: false)<br/>
   * Set whether to print the messages that are exchanged.
   */
  NATIVESWING_INTERFACE_DEBUG_PRINTMESSAGES("nativeswing.interface.debug.printmessages", Type.READ_WRITE),

  /**
   * nativeswing.interface.inprocess
   * = true/false (default: platform-dependant)<br/>
   * Set whether the interface should be in-process or out-process. Platforms which
   * use out-process may be switched to in-process, but this may affect stability.
   * Change this setting only when you need to debug sequences in a single VM.
   */
  NATIVESWING_INTERFACE_INPROCESS("nativeswing.interface.inprocess", Type.READ_WRITE),

  /**
   * nativeswing.interface.outprocess.communication
   * = sockets/processio (default: sockets)<br/>
   * Set whether the communication interface should use sockets or the process IO.
   */
  NATIVESWING_INTERFACE_OUTPROCESS_COMMUNICATION("nativeswing.interface.outprocess.communication", Type.READ_WRITE),

  /**
   * nativeswing.interface.inprocess.printnonserializablemessages
   * = true/false (default: false)<br/>
   * When in-process, set whether messages should be artificially serialized to
   * check whether they would be compatible with the out-process mode.
   */
  NATIVESWING_INTERFACE_INPROCESS_PRINTNONSERIALIZABLEMESSAGES("nativeswing.interface.inprocess.printnonserializablemessages", Type.READ_WRITE),


  /**
   * nativeswing.peervm.create
   * = true/false (default: true)<br/>
   * Set whether the peer VM should be created. Setting it to false and forcing the
   * port allows to connect to a separately launched peer VM.
   */
  NATIVESWING_PEERVM_CREATE("nativeswing.peervm.create", Type.READ_WRITE),

  /**
   * nativeswing.peervm.keepalive
   * = true/false (default: false)<br/>
   * Set whether the peer VM should die after a timeout. This is useful when
   * launching the peer VM separately.
   */
  NATIVESWING_PEERVM_KEEPALIVE("nativeswing.peervm.keepalive", Type.READ_WRITE),

  /**
   * nativeswing.peervm.forceproxyclassloader
   * = true/false (default: false)<br/>
   * Force the use of the proxy class loader. This specific class loader is
   * automatically used in the peer VM when some resources cannot be located when
   * spawning the VM.
   */
  NATIVESWING_PEERVM_FORCEPROXYCLASSLOADER("nativeswing.peervm.forceproxyclassloader", Type.READ_WRITE),

  /**
   * nativeswing.peervm.debug.printstartmessage
   * = true/false (default: false)<br/>
   * Set whether a message should be printed when the peer VM is launched, in order
   * to check that the peer VM could be created.
   */
  NATIVESWING_PEERVM_DEBUG_PRINTSTARTMESSAGE("nativeswing.peervm.debug.printstartmessage", Type.READ_WRITE),

  /**
   * nativeswing.peervm.debug.printcommandline
   * = true/false (default: false)<br/>
   * Set whether the command line that is used to spawn the peer VM should be
   * printed.
   */
  NATIVESWING_PEERVM_DEBUG_PRINTCOMMANDLINE("nativeswing.peervm.debug.printcommandline", Type.READ_WRITE),


  /**
   * nativeswing.components.debug.printfailedmessages
   * = true/false (default: false)<br/>
   * Set whether the messages that did not reach the native component should be
   * printed.
   */
  NATIVESWING_COMPONENTS_DEBUG_PRINTFAILEDMESSAGES("nativeswing.components.debug.printfailedmessages", Type.READ_WRITE),

  /**
   * nativeswing.components.swallowruntimeexceptions
   * = true/false (default: false)<br/>
   * Set whether synchronous component methods should swallow runtime
   * exceptions and return dummy results. This is a sort of paranoid mode since
   * such exceptions are not supposed to happen. Moreover, there is no guarantee
   * that user code can recover from all dummy results.
   */
  NATIVESWING_COMPONENTS_SWALLOWRUNTIMEEXCEPTIONS("nativeswing.components.swallowruntimeexceptions", Type.READ_WRITE),


  /**
   * nativeswing.webbrowser.runtime
   * = xulrunner (default: none)<br/>
   * Set the runtime of the web browser. Currently, only XULRunner is supported.
   */
  NATIVESWING_WEBBROWSER_RUNTIME("nativeswing.webbrowser.runtime", Type.READ_WRITE),

  /**
   * nativeswing.webbrowser.xulrunner.home
   * = &lt;path to XULRunner&gt;<br/>
   * Set which XULRunner installation is used. This property is taken into account
   * when using the XULRunner runtime.
   */
  NATIVESWING_WEBBROWSER_XULRUNNER_HOME("nativeswing.webbrowser.xulrunner.home", Type.READ_WRITE),


  /**
   * nativeswing.swt.debug.device
   * = true/false (default: false)<br/>
   * Set whether the SWT Device debugging is active.
   */
  NATIVESWING_SWT_DEBUG_DEVICE("nativeswing.swt.debug.device", Type.READ_WRITE),

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

  private NSSystemPropertySWT(NSSystemProperty property) {
    _name = property.getName();
    _readOnly = property.isReadOnly();
  }

  private NSSystemPropertySWT(String name) {
    this(name, Type.READ_ONLY);
  }

  private NSSystemPropertySWT(String name, Type type) {
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
        return System.getProperty(getName(), defaultValue);
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
