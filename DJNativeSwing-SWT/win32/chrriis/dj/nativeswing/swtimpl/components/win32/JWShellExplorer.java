/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.win32;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import chrriis.common.WebServer;
import chrriis.dj.nativeswing.NSOption;
import chrriis.dj.nativeswing.swtimpl.NSPanelComponent;
import chrriis.dj.nativeswing.swtimpl.NativeComponent;
import chrriis.dj.nativeswing.swtimpl.components.win32.internal.INativeWShellExplorer;
import chrriis.dj.nativeswing.swtimpl.internal.NativeCoreObjectFactory;

/**
 * A shell explorer, based on the Windows Shell Explorer (only available on the Windows operating system).<br/>
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fails, the methods will not have any effect. The results from methods have relevant values only when the component is valid.
 * @author Christopher Deckers
 */
public class JWShellExplorer extends NSPanelComponent {

  private INativeWShellExplorer nativeComponent;

  /**
   * Construct a Windows Media Player.
   * @param options the options to configure the behavior of this component.
   */
  public JWShellExplorer(NSOption... options) {
    nativeComponent = NativeCoreObjectFactory.create(INativeWShellExplorer.class, "chrriis.dj.nativeswing.swtimpl.components.win32.core.NativeWShellExplorer", new Class<?>[] {JWShellExplorer.class}, new Object[] {this});
    initialize((NativeComponent)nativeComponent);
    add(nativeComponent.createEmbeddableComponent(NSOption.createOptionMap(options)), BorderLayout.CENTER);
  }

  /**
   * Load a file.
   * @param resourcePath the path or URL to the file.
   */
  public void load(String resourcePath) {
    nativeComponent.invokeOleFunction("Navigate", resourcePath == null? "": resourcePath);
  }

  /**
   * Load a file from the classpath.
   * @param clazz the reference clazz of the file to load.
   * @param resourcePath the path to the file.
   */
  public void load(Class<?> clazz, String resourcePath) {
    addReferenceClassLoader(clazz.getClassLoader());
    load(WebServer.getDefaultWebServer().getClassPathResourceURL(clazz.getName(), resourcePath));
  }

//  /**
//   * The state of the shell explorer.
//   * @author Christopher Deckers
//   */
//  public static enum WSEReadyState {
//    UNINITIALIZED, LOADING, LOADED, INTERACTIVE, COMPLETE
//  }
//
//  /**
//   * Get the state of the shell explorer.
//   * @return the state of the shell explorer.
//   */
//  public WSEReadyState getMediaState() {
//    try {
//      switch((Integer)nativeComponent.getOleProperty("playState")) {
//        case 0: return WSEReadyState.UNINITIALIZED;
//        case 1: return WSEReadyState.LOADING;
//        case 2: return WSEReadyState.LOADED;
//        case 3: return WSEReadyState.INTERACTIVE;
//        case 4: return WSEReadyState.COMPLETE;
//      }
//    } catch(IllegalStateException e) {
//      // Invalid UI thread is an illegal state
//      throw e;
//    } catch(Exception e) {
//    }
//    return WSEReadyState.UNINITIALIZED;
//  }

  public void addShellExplorerListener(ShellExplorerListener listener) {
    nativeComponent.addShellExplorerListener(listener);
  }

  public void removeShellExplorerListener(ShellExplorerListener listener) {
    nativeComponent.removeShellExplorerListener(listener);
  }

  private List<ClassLoader> referenceClassLoaderList = new ArrayList<ClassLoader>(1);

  private void addReferenceClassLoader(ClassLoader referenceClassLoader) {
    if(referenceClassLoader == null || referenceClassLoader == getClass().getClassLoader() || referenceClassLoaderList.contains(referenceClassLoader)) {
      return;
    }
    // If a different class loader is used to locate a resource, we need to allow th web server to find that resource
    referenceClassLoaderList.add(referenceClassLoader);
    WebServer.getDefaultWebServer().addReferenceClassLoader(referenceClassLoader);
  }

  @Override
  protected void finalize() throws Throwable {
    for(ClassLoader referenceClassLoader: referenceClassLoaderList) {
      WebServer.getDefaultWebServer().removeReferenceClassLoader(referenceClassLoader);
    }
    referenceClassLoaderList.clear();
    super.finalize();
  }

}
