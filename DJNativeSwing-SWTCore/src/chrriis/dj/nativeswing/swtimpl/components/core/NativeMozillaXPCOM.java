/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.core;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Shell;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.swtimpl.CommandMessage;
import chrriis.dj.nativeswing.swtimpl.NSSystemPropertySWT;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.MozillaXPCOM;
import chrriis.dj.nativeswing.swtimpl.components.internal.INativeMozillaXPCOM;
import chrriis.dj.nativeswing.swtimpl.core.ControlCommandMessage;

/**
 * @author Christopher Deckers
 */
class NativeMozillaXPCOM implements INativeMozillaXPCOM {

  private static class CMN_getWebBrowser extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      return pack_(((Browser)getControl()).getWebBrowser(), true);
    }
  }

  public Object getWebBrowser(JWebBrowser webBrowser) {
    return unpack(webBrowser.getNativeComponent().runSync(new CMN_getWebBrowser()));
  }

  public boolean initialize() {
    String path = NSSystemPropertySWT.WEBBROWSER_XULRUNNER_HOME.get();
    if(path == null) {
      path = NSSystemPropertySWT.ORG_ECLIPSE_SWT_BROWSER_XULRUNNERPATH.get();
    } else {
      NSSystemPropertySWT.ORG_ECLIPSE_SWT_BROWSER_XULRUNNERPATH.set(path);
    }
    if (Utils.IS_MAC) {
      if(path == null) {
        path = System.getenv("XULRUNNER_HOME");
      }
      if(path == null) {
        return false;
      }
      File file = new File(path);
      if(!file.exists()) {
        return false;
      }
      org.mozilla.xpcom.Mozilla.getInstance().initialize(file);
    } else {
      Shell shell = new Shell(SWT.NONE);
      new Browser(shell, SWT.MOZILLA);
      shell.dispose();
    }
    return true;
  }

  private static Map<Integer, Object> idToNativeInterfaceMap = new HashMap<Integer, Object>();
  private static Map<Integer, WeakReference<NativeSwingProxy>> idToProxyInterfaceReferenceMap = new HashMap<Integer, WeakReference<NativeSwingProxy>>();
  private static Map<InterfaceInfo, Integer> interfaceInfoToIDMap = new HashMap<InterfaceInfo, Integer>();

  private static int nextNativeSideID = 1;
  private static int nextSwingSideID = -1;

  private static class InterfaceDefinition implements Serializable {

    private int id;
    private Class<?>[] interfaces;
    private boolean isProxy;
    private boolean isNativeSide;

    private InterfaceDefinition(int id, Class<?>[] interfaces, boolean isProxy, boolean isNativeSide) {
      this.id = id;
      this.interfaces = interfaces;
      this.isProxy = isProxy;
      this.isNativeSide = isNativeSide;
    }

    public int getID() {
      return id;
    }

    public Class<?>[] getInterfaces() {
      return interfaces;
    }

    public boolean isProxy() {
      return isProxy;
    }

    public boolean isNativeSide() {
      return isNativeSide;
    }

  }

  private interface NativeSwingProxy {

    public void finalize();

  }

  /**
   * Proxy interfaces have hashcode method that would communicate through the communication channel, hence this class.
   * @author Christopher Deckers
   */
  private static class InterfaceInfo {

    private int id;

    public InterfaceInfo(Object intrface) {
      id = System.identityHashCode(intrface);
    }

    @Override
    public int hashCode() {
      return id;
    }

    @Override
    public boolean equals(Object o) {
      return ((InterfaceInfo)o).id == id;
    }

  }

  private static class ArrayInfo implements Serializable {

    private Class<?> arrayClass;
    private Object[] content;

    public ArrayInfo(Class<?> arrayClass, Object[] content) {
      // It is not clear whether the interface can return arrays with a class that is not visible in our class loader.
      // To avoid that issue, we try to find a visible class from the class hierarchy of the array.
      ClassLoader cl = MozillaXPCOM.class.getClassLoader();
      Class<?> arrayClass_ = null;
      while(true) {
        try {
          arrayClass_ = Class.forName(arrayClass.getName(), false, cl);
        } catch (ClassNotFoundException e) {
        }
        if(arrayClass_ == arrayClass) {
          break;
        }
        arrayClass_ = arrayClass_.getSuperclass();
      }
      this.arrayClass = arrayClass;
      this.content = content;
    }

    public Class<?> getArrayClass() {
      return arrayClass;
    }

    public Object[] getItems() {
      return content;
    }

    @Override
    public String toString() {
      return Arrays.deepToString(content);
    }

  }

  public Object pack(Object o, boolean isNativeSide) {
    return pack_(o, isNativeSide);
  }

  static Object pack_(Object o, boolean isNativeSide) {
    if(o == null) {
      return null;
    }
    if(o instanceof Object[]) {
      Object[] array = (Object[])o;
      Object[] newArray = new Object[array.length];
      for(int i=0; i<array.length; i++) {
        newArray[i] = pack_(array[i], isNativeSide);
      }
      return new ArrayInfo(array.getClass(), newArray);
    }
    Package pckage = o.getClass().getPackage();
    if(pckage != null && pckage.getName().equals("java.lang")) {
      return o;
    }
    InterfaceInfo interfaceInfo = new InterfaceInfo(o);
    Integer id = interfaceInfoToIDMap.get(interfaceInfo);
    if(id == null) {
      List<Class<?>> interfaceList = new ArrayList<Class<?>>();
      ClassLoader cl = MozillaXPCOM.class.getClassLoader();
      for(Class<?> intrface: o.getClass().getInterfaces()) {
        Class<?> interfaceClass = null;
        try {
          interfaceClass = Class.forName(intrface.getName(), false, cl);
        } catch (ClassNotFoundException e) {
        }
        if(interfaceClass == intrface) {
          interfaceList.add(intrface);
        }
      }
      interfaceList.add(NativeSwingProxy.class);
      if(isNativeSide) {
        id = nextNativeSideID++;
      } else {
        id = nextSwingSideID--;
      }
      idToNativeInterfaceMap.put(id, o);
      interfaceInfoToIDMap.put(interfaceInfo, id);
      return new InterfaceDefinition(id, interfaceList.toArray(new Class<?>[0]), true, !isNativeSide);
    }
    return new InterfaceDefinition(id, null, !(o instanceof NativeSwingProxy), !isNativeSide);
  }

  private static class RunMethodResult implements Serializable {
    private Object result;
    private Object[] outParams;
    public RunMethodResult(Object result, Object[] outParams) {
      this.result = result;
      this.outParams = outParams;
    }
    public Object getResult() {
      return result;
    }
    public Object[] getOutParams() {
      return outParams;
    }
  }

  private static class CM_runMethod extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      Integer interfaceID = (Integer)args[0];
      String methodName = (String)args[1];
      Class<?>[] parameterTypes = (Class<?>[])args[2];
      Object[] parameterValues = (Object[])unpack_(args[3]);
      boolean isNativeSide = (Boolean)args[4];
      Object nativeInterface = idToNativeInterfaceMap.get(interfaceID);
      try {
        Method method = nativeInterface.getClass().getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Object result = method.invoke(nativeInterface, parameterValues);
        List<Object> outParamList = null;
        // Array parameters may be (always are?) in/out parameters, so we have to capture potentially changed values.
        if(parameterValues != null) {
          outParamList = new ArrayList<Object>();
          for(Object o: parameterValues) {
            if(o instanceof Object[]) {
              outParamList.add(pack_(o, isNativeSide));
            }
          }
        }
        return new RunMethodResult(pack_(result, isNativeSide), outParamList == null || outParamList.isEmpty()? null: outParamList.toArray());
      } catch(Exception e) {
        throw new IllegalStateException("The method " + methodName + " could not be invoked on interface " + nativeInterface + "!", e);
      }
    }
  }

  private static class CM_disposeResources extends CommandMessage {
    @Override
    public Object run(Object[] args) {
      Object nativeInterface = idToNativeInterfaceMap.remove(args[0]);
      interfaceInfoToIDMap.remove(new InterfaceInfo(nativeInterface));
      return null;
    }
  }

  public Object unpack(Object o) {
    return unpack_(o);
  }

  static Object unpack_(Object o) {
    if(o == null) {
      return null;
    }
    if(o instanceof ArrayInfo) {
      ArrayInfo arrayInfo = (ArrayInfo)o;
      Class<?> arrayClass = arrayInfo.getArrayClass();
      Object[] items = arrayInfo.getItems();
      Object[] array = (Object[])Array.newInstance(arrayClass.getComponentType(), items.length);
      for(int i=0; i<items.length; i++) {
        array[i] = unpack_(items[i]);
      }
      return array;
    }
    if(o instanceof InterfaceDefinition) {
      InterfaceDefinition interfaceDefinition = (InterfaceDefinition)o;
      final Integer id = interfaceDefinition.getID();
      if(interfaceDefinition.isProxy()) {
        WeakReference<NativeSwingProxy> proxyInterfaceReference = idToProxyInterfaceReferenceMap.get(id);
        NativeSwingProxy proxyInterface = proxyInterfaceReference == null? null: proxyInterfaceReference.get();
        if(proxyInterface == null) {
          Class<?>[] interfaces = interfaceDefinition.getInterfaces();
          final boolean isNativeSide = interfaceDefinition.isNativeSide();
          proxyInterface = (NativeSwingProxy)Proxy.newProxyInstance(MozillaXPCOM.class.getClassLoader(), interfaces, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
              String methodName = method.getName();
              Class<?>[] parameterTypes = method.getParameterTypes();
              if("finalize".equals(methodName) && parameterTypes.length == 0) {
                idToProxyInterfaceReferenceMap.remove(id);
                interfaceInfoToIDMap.remove(new InterfaceInfo(this));
                new CM_disposeResources().syncExec(!isNativeSide, id);
                return null;
              }
              RunMethodResult result = (RunMethodResult)new CM_runMethod().syncExec(!isNativeSide, id, methodName, parameterTypes, pack_(args, isNativeSide), !isNativeSide);
              Object[] outParams = result.getOutParams();
              if(outParams != null) {
                int cur = 0;
                for(Object o: args) {
                  if(o instanceof Object[]) {
                    Object[] in = (Object[])o;
                    Object[] out = (Object[])unpack_(outParams[cur++]);
                    for(int j=0; j<in.length; j++) {
                      in[j] = out[j];
                    }
                  }
                }
              }
              return unpack_(result.getResult());
            }
          });
          idToProxyInterfaceReferenceMap.put(id, new WeakReference<NativeSwingProxy>(proxyInterface));
          interfaceInfoToIDMap.put(new InterfaceInfo(proxyInterface), id);
        }
        return proxyInterface;
      }
      return idToNativeInterfaceMap.get(id);
    }
    return o;
  }

}
