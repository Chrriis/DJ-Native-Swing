/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Christopher Deckers
 */
public class NetworkURLClassLoader extends ClassLoader {

  private URL codeBaseURL;
  
  NetworkURLClassLoader(String codeBase) throws MalformedURLException {
    this.codeBaseURL = new URL(codeBase);
  }
  
  @Override
  protected URL findResource(String name) {
    try {
      return new URL(codeBaseURL, name);
    } catch(MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    String path = name.replace('.', '/') + ".class";
    Class<?> clazz = null;
    InputStream in = new BufferedInputStream(getResourceAsStream(path));
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] bytes = new byte[1024];
      for(int n; (n=in.read(bytes)) != -1; ) {
        baos.write(bytes, 0, n);
      }
      bytes = baos.toByteArray();
      clazz = defineClass(name, bytes, 0, bytes.length);
    } catch(Exception e) {
//      e.printStackTrace();
    }
    try {
      in.close();
    } catch(Exception e) {
    }
    if(clazz != null) {
      return clazz;
    }
    return super.findClass(name);
  }
  
  public static void main(String[] args) throws Exception {
    String codeBase = args[0];
    String mainClass = args[1];
    String[] newArgs = new String[args.length - 2];
    System.arraycopy(args, 2, newArgs, 0, newArgs.length);
    Class<?> clazz = new NetworkURLClassLoader(codeBase).loadClass(mainClass);
    clazz.getMethod("main", String[].class).invoke(null, new Object[] {newArgs});
  }
  
}
