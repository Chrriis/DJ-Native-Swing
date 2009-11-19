/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * A class loader that loads classes from a given codebase.
 * @author Christopher Deckers
 */
public class NetworkURLClassLoader extends ClassLoader {

  private final URL codebaseURL;

  /**
   * Construct a network URL classloader, that will load resources from the given codebase.
   * @param codebase the codebase to load the resources from.
   */
  public NetworkURLClassLoader(String codebase) throws MalformedURLException {
    codebaseURL = new URL(codebase);
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
    Exception exception = null;
    // Let's retry twice
    for(int i=0; i<2; i++) {
      String path = name.replace('.', '/') + ".class";
      URL resourceURL = getResource(path);
      InputStream in = null;
      Class<?> clazz = null;
      exception = null;
      try {
        URLConnection connection = resourceURL.openConnection();
        connection.setReadTimeout(4000);
        in = connection.getInputStream();
//      InputStream in = new BufferedInputStream(getResourceAsStream(path));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        for(int n; (n=in.read(bytes)) != -1; baos.write(bytes, 0, n)) {
        }
        bytes = baos.toByteArray();
        clazz = defineClass(name, bytes, 0, bytes.length);
      } catch(Exception e) {
        exception = e;
      }
      try {
        if(in != null) {
          in.close();
        }
      } catch(Exception e) {
      }
      if(clazz != null) {
        return clazz;
      }
    }
    throw new ClassNotFoundException(name, exception);
  }

  public static void main(String[] args) throws Exception {
    String codeBase = args[0];
    String mainClass = args[1];
    String[] newArgs = new String[args.length - 2];
    System.arraycopy(args, 2, newArgs, 0, newArgs.length);
    Class<?> clazz;
    Method method;
    try {
      clazz = new NetworkURLClassLoader(codeBase).loadClass(mainClass);
      method = clazz.getDeclaredMethod("main", String[].class);
      method.setAccessible(true);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
      return;
    }
    method.invoke(null, new Object[] {newArgs});
  }

}
