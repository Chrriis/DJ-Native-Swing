/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.internal;

import java.lang.reflect.Constructor;

/**
 * @author Christopher Deckers
 */
public class CoreClassFactory {

  private static CoreClassFactory classFactory;

  public static void setDefaultClassFactory(CoreClassFactory classFactory) {
    synchronized(CoreClassFactory.class) {
      CoreClassFactory.classFactory = classFactory;
    }
  }

  public static <T> T create(Class<T> clazz, String className, Class<?>[] types, Object[] args) {
    ClassLoader classLoader;
    synchronized(CoreClassFactory.class) {
      classLoader = classFactory != null? classFactory.classLoader: null;
    }
    if(classLoader == null) {
      classLoader = CoreClassFactory.class.getClassLoader();
    }
    try {
      @SuppressWarnings("unchecked")
      Class<T> wbClass = (Class<T>)classLoader.loadClass(className);
      Constructor<T> wbConstructor = wbClass.getDeclaredConstructor(types);
      wbConstructor.setAccessible(true);
      return wbConstructor.newInstance(args);
    } catch(RuntimeException e) {
      throw e;
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  private ClassLoader classLoader;

  public CoreClassFactory(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

}
