/**
 * Copyright (c) 2009 Molindo GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 *
 * Note:
 *
 * A human-readable format of the "MIT License" in available at creativecommons.org:
 * http://creativecommons.org/licenses/MIT/
 *
 */
package chrriis.common;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This enum is supposed to be a complete list of available syste properties on
 * different JVM and Java versions. Use {@link #get()} or {@link #toString()} to
 * access the property values or {@link #set(String)} to update them - but only
 * those that aren't read-only ({@link #isReadOnly()}).
 *
 * If you discover some new properties in your environment, please run the
 * contained {@link #main(String[])} method and send the output to me:
 *
 * You may want to check for updates once in a while:
 * http://techblog.molindo.at/files/SystemProperty.java and
 * http://techblog.molindo.at/2009/11/java-system-properties.html
 *
 * $LastChangedRevision: 4287 $
 *
 * @author Stefan Fussenegger
 */
public enum SystemProperty {

  COM_IBM_VM_BITMODE("com.ibm.vm.bitmode"),

  FILE_ENCODING("file.encoding"),
  FILE_ENCODING_PKG("file.encoding.pkg"),
  FILE_SEPARATOR("file.separator"),

  JAVA_AWT_GRAPHICSENV("java.awt.graphicsenv"),
  JAVA_AWT_PRINTERJOB("java.awt.printerjob"),
  JAVA_AWT_SMARTINVALIDATE("java.awt.smartInvalidate"),

  JAVA_CLASS_PATH("java.class.path"),
  JAVA_CLASS_VERSION("java.class.version"),

  JAVA_ENDORSED_DIRS("java.endorsed.dirs"),
  JAVA_EXT_DIRS("java.ext.dirs"),

  JAVA_HOME("java.home"),

  /**
   * path to temporary directory including trailing file separator
   */
  JAVA_IO_TMPDIR("java.io.tmpdir", Type.READ_WRITE) {
    @Override
    public String get() {
      String value = super.get();
      if (value == null) {
        // you'll never know
        return null;
      }

      if (!value.endsWith(File.separator)) {
        // http://rationalpi.wordpress.com/2007/01/26/javaiotmpdir-inconsitency/
        value += File.separator;
      }

      // make sure dir exists
      try {
        new File(value).mkdirs();
      } catch (SecurityException e) {
        //log.warn("not allowed to create temporary directory: " + value, e);
      }
      return value;
    }
  },

  JAVA_LIBRARY_PATH("java.library.path"),
  JAVA_RUNTIME_NAME("java.runtime.name"),
  JAVA_RUNTIME_VERSION("java.runtime.version"),

  JAVA_SPECIFICATION_NAME("java.specification.name"),
  JAVA_SPECIFICATION_VENDOR("java.specification.vendor"),
  JAVA_SPECIFICATION_VERSION("java.specification.version"),

  JAVA_VERSION("java.version"),
  JAVA_VENDOR("java.vendor"),
  JAVA_VENDOR_URL("java.vendor.url"),
  JAVA_VENDOR_URL_BUG("java.vendor.url.bug"),

  JAVAWEBSTART_VERSION("javawebstart.version"),

  JAVA_VM_INFO("java.vm.info"),
  JAVA_VM_NAME("java.vm.name"),
  JAVA_VM_SPECIFICATION_NAME("java.vm.specification.name"),
  JAVA_VM_SPECIFICATION_VENDOR("java.vm.specification.vendor"),
  JAVA_VM_SPECIFICATION_VERSION("java.vm.specification.version"),
  JAVA_VM_VERSION("java.vm.version"),
  JAVA_VM_VENDOR("java.vm.vendor"),

  LINE_SEPARATOR("line.separator"),

  /**
   * see http://lopica.sourceforge.net/os.html for possible values
   */
  OS_NAME("os.name"),
  OS_ARCH("os.arch"),
  OS_VERSION("os.version"),

  PATH_SEPARATOR("path.separator"),

  SUN_ARCH_DATA_MODEL("sun.arch.data.model"),
  SUN_BOOT_CLASS_PATH("sun.boot.class.path"),
  SUN_BOOT_LIBRARY_PATH("sun.boot.library.path"),
  SUN_CPU_ENDIAN("sun.cpu.endian"),
  SUN_CPU_ISALIST("sun.cpu.isalist"),

  SUN_IO_UNICODE_ENCODING("sun.io.unicode.encoding"),
  SUN_JAVA_LAUNCHER("sun.java.launcher"),
  SUN_JNU_ENCODING("sun.jnu.encoding"),
  SUN_MANAGEMENT_COMPILER("sun.management.compiler"),
  SUN_OS_PATCH_LEVEL("sun.os.patch.level"),

  USER_COUNTRY("user.country"),
  USER_DIR("user.dir"),
  USER_HOME("user.home"),
  USER_LANGUAGE("user.language"),
  USER_NAME("user.name"),
  USER_TIMEZONE("user.timezone"),

  SUN_AWT_DISABLEMIXING("sun.awt.disableMixing", Type.READ_WRITE),
  SUN_AWT_NOERASEBACKGROUND("sun.awt.noerasebackground", Type.READ_WRITE),
  SUN_AWT_XEMBEDSERVER("sun.awt.xembedserver", Type.READ_WRITE),

  /*
   * Linux ONLY
   */
  /**
   * Linux only: known values: gnome
   */
  SUN_DESKTOP("sun.desktop"),

  /*
   * MAC ONLY
   */
  /**
   * Mac only: true or false
   */
  AWT_NATIVE_DOUBLE_BUFFERING("awt.nativeDoubleBuffering"),
  /**
   * Mac only: known values: apple.awt.CToolkit
   */
  AWT_TOOLKIT("awt.toolkit"),

  /**
   * Mac only: known values: local|*.local|169.254/16|*.169.254/16
   */
  FTP_NON_PROXY_HOSTS("ftp.nonProxyHosts"),

  /**
   * Mac only: true or false
   */
  GOPHER_PROXY_SET("gopherProxySet"),
  /**
   * Mac only: known values: local|*.local|169.254/16|*.169.254/16
   */
  HTTP_NON_PROXY_HOSTS("http.nonProxyHosts"),

  /**
   * Mac only: known values: 1060.1.6.0_15-219
   */
  MRJ_VERSION("mrj.version"),

  /**
   * Mac only: known values: local|*.local|169.254/16|*.169.254/16
   */
  SOCKS_NON_PROXY_HOSTS("socksNonProxyHosts"),

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

  private SystemProperty(String name) {
    this(name, Type.READ_ONLY);
  }

  private SystemProperty(String name, Type type) {
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
   * @param value
   *            the new value
   * @see System#setProperty(String, String)
   * @see AccessController#doPrivileged(PrivilegedAction)
   * @see #isReadOnly()
   * @exception UnsupportedOperationException
   *                if this property is read-only
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
   * @return a string representation of this object (e.g.
   *         "OS_NAME: os.name=Linux (read-only)")
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

  /**
   * a simple main method to list available system properties. additionally,
   * it generates Java code for yet unknown properties. If you find some of
   * them, please let us know:
   * http://techblog.molindo.at/2009/11/java-system-properties.html
   *
   * @param args
   *            arguments are ignored
   */
  public static void main(String[] args) {
    TreeMap<Object, Object> props = new TreeMap<Object, Object>();
    TreeSet<SystemProperty> unknown = new TreeSet<SystemProperty>();

    props.putAll(System.getProperties());

    for (SystemProperty p : SystemProperty.values()) {
      System.out.println(p.toDebugString());
      if (!props.containsKey(p.getName())) {
        unknown.add(p);
      } else {
        props.remove(p.getName());
      }

      checkNaming(p);
    }

    if (unknown.size() > 0) {
      System.out.println("\n\n### UNKNOWN");
      for (SystemProperty p : unknown) {
        System.out.println(p.toDebugString());
      }
    }

    if (props.size() > 0) {
      System.out.println("\n\n### MISSING");
      for (Map.Entry<Object, Object> e : props.entrySet()) {
        System.out.println(e);
      }

      System.out.println("\n\n### PLEASE POST THIS AT http://j.mp/props0 or http://j.mp/props1");
      for (Map.Entry<Object, Object> e : props.entrySet()) {
        System.out
            .println(String
                .format("\t/**\n\t * %s only: known values: %s\n\t */\n\t%s(\"%s\"),", OS_NAME, e
                    .getValue(), toEnumName((String) e.getKey()), e.getKey()));
      }
    }
  }

  /**
   * check naming of enums (mainly to spot possible typos)
   *
   * @param p
   *            the SytemProperty to check
   */
  private static void checkNaming(SystemProperty p) {
    String expected = toEnumName(p.getName());

    if (!p.name().equals(expected)) {
      System.err.println("name missmatch: " + p.toDebugString() + " (expected " + expected
          + ")");
    }
  }

  /**
   * generate a new enum name from a system property
   *
   * @param property
   *            property name (e.g. "os.name")
   *
   * @return the resulting enum name (e.g. "OS_NAME")
   */
  private static String toEnumName(String property) {
    StringBuilder buf = new StringBuilder();
    for (char c : property.toCharArray()) {
      if (Character.isUpperCase(c)) {
        buf.append('_').append(c);
      } else if (c == '.') {
        buf.append('_');
      } else {
        buf.append(Character.toUpperCase(c));
      }
    }
    return buf.toString();
  }
}
