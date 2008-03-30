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
 * A class loader that loads classes from a give codebase.
 * @author Christopher Deckers
 */
public class NetworkURLClassLoader extends ClassLoader {

  private URL codebaseURL;

  /**
   * Construct a network URL classloader, that will load resources from the give codebase.
   * @param codebase the codebase to load the resources from.
   */
  public NetworkURLClassLoader(String codebase) throws MalformedURLException {
    this.codebaseURL = new URL(codebase);
  }
  
  @Override
  protected URL findResource(String name) {
    try {
      return new URL(codebaseURL, name);
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
      for(int n; (n=in.read(bytes)) != -1; baos.write(bytes, 0, n));
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
