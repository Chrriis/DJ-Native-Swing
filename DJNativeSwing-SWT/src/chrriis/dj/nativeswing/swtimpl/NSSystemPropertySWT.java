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

import chrriis.common.Utils;
import chrriis.dj.nativeswing.NSSystemProperty;

/**
 * A class that exposes all the system properties used by the DJ Native Swing SWT implementation.
 * @author Christopher Deckers
 * @author Stefan Fussenegger
 */
public enum NSSystemPropertySWT {

  /* ----------------- NativeSwing properties ----------------- */

  /**
   * nativeswing.localhostAddress
   * = &lt;String&gt; or "_localhost_" (default: auto-detect, usually 127.0.0.1)<br/>
   * Set the address that is used as the local host address for all the internal
   * communication channels that require a socket (local web server, etc.).
   */
  LOCALHOSTADDRESS(NSSystemProperty.LOCALHOSTADDRESS),

  /**
   * nativeswing.localhostAddress.debug.printDetection
   * = true/false (default: false)<br/>
   * Set whether to print the steps of local host address detection.
   */
  LOCALHOSTADDRESS_DEBUG_PRINTDETECTION(NSSystemProperty.LOCALHOSTADDRESS_DEBUG_PRINTDETECTION),

  /**
   * nativeswing.localhostAddress.debug.print
   * = true/false (default: false)<br/>
   * Set whether the address found as the local host address should be printed.
   */
  LOCALHOSTADDRESS_DEBUG_PRINT(NSSystemProperty.LOCALHOSTADDRESS_DEBUG_PRINT),


  /**
   * nativeswing.webserver.debug.printPort
   * = true/false (default: false)<br/>
   * Set whether the port that is used by the embedded web server should be
   * printed.
   */
  WEBSERVER_DEBUG_PRINTPORT(NSSystemProperty.WEBSERVER_DEBUG_PRINTPORT),

  /**
   * nativeswing.webserver.debug.printRequests
   * = true/false (default: false)<br/>
   * Set whether the web server should print the requests it receives, along with
   * the result (200 or 404).
   */
  WEBSERVER_DEBUG_PRINTREQUESTS(NSSystemProperty.WEBSERVER_DEBUG_PRINTREQUESTS),


  /**
   * nativeswing.components.debug.printOptions
   * = true/false (default: false)<br/>
   * Set whether the options used to create a component should be printed.
   */
  COMPONENTS_DEBUG_PRINTOPTIONS(NSSystemProperty.COMPONENTS_DEBUG_PRINTOPTIONS),

  /**
   * nativeswing.components.debug.printShapeComputing
   * = true/false (default: false)<br/>
   * Set whether the computation of the shape applied to the native component (when
   * the visibility constraint option is active) should be printed.
   */
  COMPONENTS_DEBUG_PRINTSHAPECOMPUTING(NSSystemProperty.COMPONENTS_DEBUG_PRINTSHAPECOMPUTING),


  /**
   * nativeswing.integration.active
   * = true/false (default: true)<br/>
   * Set whether native integration should be active.
   */
  INTEGRATION_ACTIVE(NSSystemProperty.INTEGRATION_ACTIVE),


  /**
   * nativeswing.dependencies.checkVersions
   * = true/false (default: true)<br/>
   * Set whether the versions of the dependencies should be checked when possible.
   */
  DEPENDENCIES_CHECKVERSIONS(NSSystemProperty.DEPENDENCIES_CHECKVERSIONS),


  /* ----------------- NativeSwing-SWT properties ----------------- */

  /**
   * nativeswing.interface.port
   * = &lt;integer&gt; (default: -1)<br/>
   * Force the port to use to communicate with the spawned VM.
   */
  INTERFACE_PORT("nativeswing.interface.port", Type.READ_WRITE),

  /**
   * nativeswing.interface.streamResetThreshold
   * = &lt;integer&gt; (default: 500000)<br/>
   * Set the number of bytes that need to be exchanged to trigger a reset of the
   * Object communication stream.
   */
  INTERFACE_STREAMRESETTHRESHOLD("nativeswing.interface.streamResetThreshold", Type.READ_WRITE),

  /**
   * nativeswing.interface.syncMessages
   * = true/false (default: false)<br/>
   * Set whether all asynchronous messages should be sent synchronously.
   */
  INTERFACE_SYNCMESSAGES("nativeswing.interface.syncMessages", Type.READ_WRITE),

  /**
   * nativeswing.interface.debug.printMessages
   * = true/false (default: false)<br/>
   * Set whether to print the messages that are exchanged.
   */
  INTERFACE_DEBUG_PRINTMESSAGES("nativeswing.interface.debug.printMessages", Type.READ_WRITE),

  /**
   * nativeswing.interface.inProcess
   * = true/false (default: platform-dependant)<br/>
   * Set whether the interface should be in-process or out-process. Platforms which
   * use out-process may be switched to in-process, but this may affect stability.
   * Change this setting only when you need to debug sequences in a single VM.
   */
  INTERFACE_INPROCESS("nativeswing.interface.inProcess", Type.READ_WRITE),

  /**
   * nativeswing.interface.outProcess.communication
   * = sockets/processio (default: sockets)<br/>
   * Set whether the communication interface should use sockets or the process IO.
   */
  INTERFACE_OUTPROCESS_COMMUNICATION("nativeswing.interface.outProcess.communication", Type.READ_WRITE),

  /**
   * nativeswing.interface.inProcess.printNonSerializableMessages
   * = true/false (default: false)<br/>
   * When in-process, set whether messages should be artificially serialized to
   * check whether they would be compatible with the out-process mode.
   */
  INTERFACE_INPROCESS_PRINTNONSERIALIZABLEMESSAGES("nativeswing.interface.inProcess.printNonSerializableMessages", Type.READ_WRITE),


  /**
   * nativeswing.peervm.create
   * = true/false (default: true)<br/>
   * Set whether the peer VM should be created. Setting it to false and forcing the
   * port allows to connect to a separately launched peer VM.
   */
  PEERVM_CREATE("nativeswing.peervm.create", Type.READ_WRITE),

  /**
   * nativeswing.peervm.keepAlive
   * = true/false (default: false)<br/>
   * Set whether the peer VM should die after a timeout. This is useful when
   * launching the peer VM separately.
   */
  PEERVM_KEEPALIVE("nativeswing.peervm.keepAlive", Type.READ_WRITE),

  /**
   * nativeswing.peervm.forceProxyClassLoader
   * = true/false (default: false)<br/>
   * Force the use of the proxy class loader. This specific class loader is
   * automatically used in the peer VM when some resources cannot be located when
   * spawning the VM.
   */
  PEERVM_FORCEPROXYCLASSLOADER("nativeswing.peervm.forceProxyClassLoader", Type.READ_WRITE),

  /**
   * nativeswing.peervm.debug.printStartMessage
   * = true/false (default: false)<br/>
   * Set whether a message should be printed when the peer VM is launched, in order
   * to check that the peer VM could be created.
   */
  PEERVM_DEBUG_PRINTSTARTMESSAGE("nativeswing.peervm.debug.printStartMessage", Type.READ_WRITE),

  /**
   * nativeswing.peervm.debug.printStopMessage
   * = true/false (default: false)<br/>
   * Set whether a message should be printed when the peer VM stops, in order to
   * know that it stopped on purpose and not because of a crash.
   */
  PEERVM_DEBUG_PRINTSTOPMESSAGE("nativeswing.peervm.debug.printStopMessage", Type.READ_WRITE),

  /**
   * nativeswing.peervm.debug.printCommandLine
   * = true/false (default: false)<br/>
   * Set whether the command line that is used to spawn the peer VM should be
   * printed.
   */
  PEERVM_DEBUG_PRINTCOMMANDLINE("nativeswing.peervm.debug.printCommandLine", Type.READ_WRITE),


  /**
   * nativeswing.components.debug.printFailedMessages
   * = true/false (default: false)<br/>
   * Set whether the messages that did not reach the native component should be
   * printed.
   */
  COMPONENTS_DEBUG_PRINTFAILEDMESSAGES("nativeswing.components.debug.printFailedMessages", Type.READ_WRITE),

  /**
   * nativeswing.components.debug.printCreation
   * = true/false (default: false)<br/>
   * Set whether the creation of a native component should be printed.
   */
  COMPONENTS_DEBUG_PRINTCREATION("nativeswing.components.debug.printCreation", Type.READ_WRITE),

  /**
   * nativeswing.components.debug.printDisposal
   * = true/false (default: false)<br/>
   * Set whether the disposal of a native component should be printed.
   */
  COMPONENTS_DEBUG_PRINTDISPOSAL("nativeswing.components.debug.printDisposal", Type.READ_WRITE),

  /**
   * nativeswing.components.swallowRuntimeExceptions
   * = true/false (default: false)<br/>
   * Set whether synchronous component methods should swallow runtime
   * exceptions and return dummy results. This is a sort of paranoid mode since
   * such exceptions are not supposed to happen. Moreover, there is no guarantee
   * that user code can recover from all dummy results.
   */
  COMPONENTS_SWALLOWRUNTIMEEXCEPTIONS("nativeswing.components.swallowRuntimeExceptions", Type.READ_WRITE),


  /**
   * nativeswing.webbrowser.runtime
   * = xulrunner/webkit (default: none)<br/>
   * Set the runtime of the web browser.
   */
  WEBBROWSER_RUNTIME("nativeswing.webbrowser.runtime", Type.READ_WRITE),

  /**
   * nativeswing.webbrowser.xulrunner.home
   * = &lt;path to XULRunner&gt;<br/>
   * Set which XULRunner installation is used. This property is taken into account
   * when using the XULRunner runtime.
   */
  WEBBROWSER_XULRUNNER_HOME("nativeswing.webbrowser.xulrunner.home", Type.READ_WRITE),


  /**
   * nativeswing.htmleditor.getHTMLContent.timeout
   * = &lt;integer&gt; (default: 1500)<br/>
   * Under heavy load, getHTLMContent may not complete in time, so for such systems
   * it is desirable to increase the timeout.
   */
  HTMLEDITOR_GETHTMLCONTENT_TIMEOUT("nativeswing.htmleditor.getHTMLContent.timeout", Type.READ_WRITE),


  /**
   * nativeswing.vlcplayer.fixPlaylistAutoPlayNext
   * = true/false (default: true)<br/>
   * VLC seems to have a bug: it does not automatically play the next queued item.
   * The fix that was added can be deactivated with this system property.
   */
  VLCPLAYER_FIXPLAYLISTAUTOPLAYNEXT("nativeswing.vlcplayer.fixPlaylistAutoPlayNext", Type.READ_WRITE),


  /**
   * nativeswing.swt.device.debug
   * = true/false (default: false)<br/>
   * Set whether the SWT Device debugging is active. This property can be useful
   * because it outputs the web browser runtime effectively detected for the OS.
   */
  SWT_DEVICE_DEBUG("nativeswing.swt.device.debug", Type.READ_WRITE),


  SWT_LIBRARY_PATH("swt.library.path", Type.READ_WRITE),
  SWT_DEVICEDATA_DEBUG("nativeswing.swt.devicedata.debug", Type.READ_WRITE),
  SWT_DEVICEDATA_TRACKING("nativeswing.swt.devicedata.tracking", Type.READ_WRITE),
  ORG_ECLIPSE_SWT_BROWSER_XULRUNNERPATH("org.eclipse.swt.browser.XULRunnerPath", Type.READ_WRITE),
  COMPONENTS_DISABLEHIDDENPARENTREPARENTING("nativeswing.components.disableHiddenParentReparenting", Type.READ_WRITE),
  COMPONENTS_PRINTINGHACK("nativeswing.components.printingHack", Type.READ_WRITE),
  COMPONENTS_USECOMPONENTIMAGECLOSINGTHREAD("nativeswing.components.useComponentImageClosingThread", Type.READ_WRITE),
  INTERFACE_INPROCESS_FORCESHUTDOWNHOOK("nativeswing.interface.inprocess.forceShutdownHook", Type.READ_WRITE),
  INTERFACE_INPROCESS_USEEXTERNALSWTDISPLAY("nativeswing.interface.inprocess.useExternalSWTDisplay", Type.READ_WRITE),
  INTERFACE_OUTPROCESS_SYNCCLOSING("nativeswing.interface.outprocess.syncclosing", Type.READ_WRITE),
  INTERFACE_SYNCSEND_LOCAL_TIMEOUT("nativeswing.interface.syncsend.local.timeout", Type.READ_WRITE),
  INTERFACE_SYNCSEND_NATIVE_TIMEOUT("nativeswing.interface.syncsend.native.timeout", Type.READ_WRITE),

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
