/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Christopher Deckers
 */
public class DemoUtils {

  public static String extractFileURL(Class<?> clazz, String resource) {
    File file = null;
    try {
      file = File.createTempFile("nsfe", ".swf");
    } catch(Exception e) {
      e.printStackTrace();
      return null;
    }
    file.deleteOnExit();
    BufferedInputStream in = new BufferedInputStream(clazz.getResourceAsStream(resource));
    try {
      BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
      byte[] bytes = new byte[1024];
      for(int i; (i=in.read(bytes)) != -1; ) {
        out.write(bytes, 0, i);
      }
      in.close();
      out.close();
      return file.toURI().toURL().toExternalForm();
    } catch(Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
}
