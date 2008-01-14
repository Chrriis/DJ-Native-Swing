/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo;

import java.io.InputStream;

import chrriis.common.WebServer;
import chrriis.common.WebServer.WebServerContent;

/**
 * @author Christopher Deckers
 */
public class DemoUtils {

  public static String getResourceURL(Class<?> clazz, String resource) {
    return WebServer.getDefaultWebServer().getResourcePath(DemoUtils.class.getName(), clazz.getName() + "/" + resource);
  }
  
  protected static WebServerContent getWebServerContent(String resourcePath) {
    int index = resourcePath.indexOf('/');
    final String className = resourcePath.substring(0, index);
    final String resource = resourcePath.substring(index + 1);
    return new WebServerContent() {
      @Override
      public String getContentType() {
        int index = resource.lastIndexOf('.');
        return getDefaultMimeType(index == -1? null: resource.substring(index));
      }
      @Override
      public InputStream getInputStream() {
        try {
          return Class.forName(className).getResourceAsStream(resource);
        } catch(Exception e) {
          e.printStackTrace();
          return null;
        }
      }
    };
  }
  
}
