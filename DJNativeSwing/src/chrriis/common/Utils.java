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
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @author Christopher Deckers
 */
public class Utils {

  public static final boolean IS_JAVA_6_OR_GREATER = System.getProperty("java.version").compareTo("1.6") >= 0;

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
  
  public static File getClassPathFile(Class<?> clazz) {
    File file = getJARFile(clazz);
    return file != null? file: getDirectory(clazz);
  }
  
  public static File getJARFile(Class<?> clazz) {
    String classResourcePath = "/" + clazz.getName().replace('.', '/') + ".class";
    String classResourceURL = clazz.getResource(classResourcePath).toExternalForm();
    if(classResourceURL != null && classResourceURL.startsWith("jar:file:")) {
      classResourceURL = classResourceURL.substring("jar:file:".length());
      if(classResourceURL.endsWith("!" + classResourcePath)) {
        return new File(decodeURL(classResourceURL.substring(0, classResourceURL.length() - 1 - classResourcePath.length())));
      }
    }
    return null;
  }
  
  public static File getDirectory(Class<?> clazz) {
    String className = clazz.getName();
    String classResourcePath = "/" + className.replace('.', '/') + ".class";
    String classResourceURL = clazz.getResource(classResourcePath).toExternalForm();
    if(classResourceURL != null && classResourceURL.startsWith("file:")) {
      File dir = new File(decodeURL(classResourceURL.substring("file:".length()))).getParentFile();
      for(int i=0; i<className.length(); i++) {
        if(className.charAt(i) == '.') {
          dir = dir.getParentFile();
        }
      }
      return dir;
    }
    return null;
  }
  
}
