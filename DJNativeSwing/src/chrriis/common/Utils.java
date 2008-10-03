/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Christopher Deckers
 */
public class Utils {

  private Utils() {}
  
  public static final boolean IS_JAVA_6_OR_GREATER = System.getProperty("java.version").compareTo("1.6") >= 0;

  public static final boolean IS_MAC;
  
  static {
    String os = System.getProperty("os.name");
    IS_MAC = os.startsWith("Mac") || os.startsWith("Darwin");
  }
  
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");
  
  public static String decodeURL(String s) {
    try {
      return URLDecoder.decode(s, "UTF-8");
    } catch(UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  @SuppressWarnings("deprecation")
  public static String encodeURL(String s) {
    String encodedString;
    try {
      encodedString = URLEncoder.encode(s, "UTF-8");
    } catch(Exception e) {
      encodedString = URLEncoder.encode(s);
    }
    return encodedString.replaceAll("\\+", "%20");
  }
  
  public static String escapeXML(String s) {
    if(s == null || s.length() == 0) {
      return s;
    }
    StringBuffer sb = new StringBuffer((int)(s.length() * 1.1));
    for(int i=0; i<s.length(); i++) {
      char c = s.charAt(i);
      switch(c) {
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        case '&':
          sb.append("&amp;");
          break;
        case '\'':
          sb.append("&apos;");
          break;
        case '\"':
          sb.append("&quot;");
          break;
        default:
          sb.append(c);
        break;
      }
    }
    return sb.toString();
  }

  /**
   * @return null or a valid File if the path is a URL or path to a valid local file (that exists).
   */
  public static File getLocalFile(String path) {
    if(path == null) {
      return null;
    }
    if(path.startsWith("file:")) {
      File file = new File(path.substring("file:".length()));
      if(file.exists()) {
        return file;
      }
    }
    File file = new File(path);
    if(file.exists()) {
      return file;
    }
    return null;
  }
  
  public static File getClassPathFile(String resourcePath) {
    File file = getJARFile(resourcePath);
    return file != null? file: getDirectory(resourcePath);
  }
  
  public static File getClassPathFile(Class<?> clazz) {
    File file = getJARFile(clazz);
    return file != null? file: getDirectory(clazz);
  }
  
  public static File getJARFile(String resourcePath) {
    if(!resourcePath.startsWith("/")) {
      resourcePath = '/' + resourcePath;
    }
    return getJARFile(Utils.class, resourcePath);
  }
  
  public static File getJARFile(Class<?> clazz) {
    return getJARFile(clazz, "/" + clazz.getName().replace('.', '/') + ".class");
  }
  
  private static File getJARFile(Class<?> clazz, String resourcePath) {
    URL resource = clazz.getResource(resourcePath);
    if(resource == null) {
      return null;
    }
    String classResourceURL = resource.toExternalForm();
    if(classResourceURL != null && classResourceURL.startsWith("jar:file:")) {
      classResourceURL = classResourceURL.substring("jar:file:".length());
      if(classResourceURL.endsWith("!" + resourcePath)) {
        return new File(decodeURL(classResourceURL.substring(0, classResourceURL.length() - 1 - resourcePath.length())));
      }
    }
    return null;
  }
  
  public static File getDirectory(String resourcePath) {
    if(!resourcePath.startsWith("/")) {
      resourcePath = '/' + resourcePath;
    }
    return getDirectory(Utils.class, resourcePath);
  }
  
  public static File getDirectory(Class<?> clazz) {
    return getDirectory(clazz, "/" + clazz.getName().replace('.', '/') + ".class");
  }
  
  private static File getDirectory(Class<?> clazz, String resourcePath) {
    String resourceName = resourcePath;
    if(resourceName.startsWith("/")) {
      resourceName = resourceName.substring(1);
    }
    URL resource = clazz.getResource(resourcePath);
    if(resource == null) {
      return null;
    }
    String classResourceURL = resource.toExternalForm();
    if(classResourceURL != null && classResourceURL.startsWith("file:")) {
      File dir = new File(decodeURL(classResourceURL.substring("file:".length()))).getParentFile();
      for(int i=0; i<resourceName.length(); i++) {
        if(resourceName.charAt(i) == '/') {
          dir = dir.getParentFile();
        }
      }
      return dir;
    }
    return null;
  }
  
  /**
   * Delete the file, or delete the directtory and all its children recursively.
   * @param fileOrDir a file or a directory to delete.
   */
  public static void deleteAll(File fileOrDir) {
    if(!fileOrDir.delete()) {
      if(fileOrDir.isDirectory()) {
        for(File file: fileOrDir.listFiles()) {
          deleteAll(file);
        }
        fileOrDir.delete();
      }
    }
  }
  
  /**
   * Test the equality of 2 objects, with a check on nullity.
   * @param o1 the first object.
   * @param o2 the second object.
   * @return true if both object are equal, which includes the case where both are null.
   */
  public static boolean equals(Object o1, Object o2) {
    return o1 == null? o2 == null: o1.equals(o2);
  }
  
  /**
   * Produce the deep string value of an array, whatever the actual class type of the array is.
   * @param array the array to get the deep string value of.
   * @return the deep string value of the array, or null if invalid.
   */
  public static String arrayDeepToString(Object array) {
    if(array == null) {
      return null;
    }
    Class<?> clazz = array.getClass();
    if(!clazz.isArray()) {
      return null;
    }
    if(clazz == boolean[].class) {
      return Arrays.toString((boolean[])array);
    }
    if(clazz == byte[].class) {
      return Arrays.toString((byte[])array);
    }
    if(clazz == short[].class) {
      return Arrays.toString((short[])array);
    }
    if(clazz == char[].class) {
      return Arrays.toString((char[])array);
    }
    if(clazz == int[].class) {
      return Arrays.toString((int[])array);
    }
    if(clazz == long[].class) {
      return Arrays.toString((long[])array);
    }
    if(clazz == float[].class) {
      return Arrays.toString((float[])array);
    }
    if(clazz == double[].class) {
      return Arrays.toString((double[])array);
    }
    return Arrays.deepToString((Object[])array);
  }
  
  /**
   * Simplify a path, where separators are '/', by resolving "." and "..".
   * @return the simplified path.
   */
  public static String simplifyPath(String path) {
    if(path.indexOf("//") != -1) {
      throw new IllegalArgumentException("The path is invalid: " + path);
    }
    String[] crumbs = path.split("/");
    List<String> crumbList = new ArrayList<String>(crumbs.length);
    for(String crumb: crumbs) {
      if("".equals(crumb) || ".".equals(crumb)) {
        // do nothing
      } else if("..".equals(crumb)) {
        int index = crumbList.size() - 1;
        if(index == -1) {
          throw new IllegalArgumentException("The path is invalid: " + path);
        }
        crumbList.remove(index);
      } else {
        crumbList.add(crumb);
      }
    }
    StringBuilder sb = new StringBuilder(path.length());
    if(path.startsWith("/")) {
      sb.append('/');
    }
    int crumbCount = crumbList.size();
    for(int i=0; i<crumbCount; i++) {
      if(i > 0) {
        sb.append('/');
      }
      sb.append(crumbList.get(i));
    }
    if(path.length() > 1 && path.endsWith("/")) {
      sb.append('/');
    }
    return sb.toString();
  }
  
}
