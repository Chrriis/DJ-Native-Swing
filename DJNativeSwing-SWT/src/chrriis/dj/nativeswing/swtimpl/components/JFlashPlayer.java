/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.common.WebServer.HTTPRequest;
import chrriis.common.WebServer.WebServerContent;
import chrriis.common.WebServer.WebServerContentProvider;
import chrriis.dj.nativeswing.NSOption;
import chrriis.dj.nativeswing.NSSystemProperty;
import chrriis.dj.nativeswing.swtimpl.NSPanelComponent;
import chrriis.dj.nativeswing.swtimpl.WebBrowserObject;

/**
 * A native Flash player. It is a browser-based component, which relies on the Flash plugin.<br/>
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fails, the methods will not have any effect. The results from methods have relevant values only when the component is valid.
 * @author Christopher Deckers
 */
public class JFlashPlayer extends NSPanelComponent {

  private static final String SET_CUSTOM_JAVASCRIPT_DEFINITIONS_OPTION_KEY = "Flash Player Custom Javascript definitions";

  /**
   * Create an option to set some custom Javascript definitions (functions) that are added to the HTML page that contains the plugin.
   * @return the option to set some custom Javascript definitions.
   */
  public static NSOption setCustomJavascriptDefinitions(final String javascript) {
    return new NSOption(SET_CUSTOM_JAVASCRIPT_DEFINITIONS_OPTION_KEY) {
      @Override
      public Object getOptionValue() {
        return javascript;
      }
    };
  }

  static {
    WebServer.getDefaultWebServer().addContentProvider(new WebServerContentProvider() {
      public WebServerContent getWebServerContent(HTTPRequest httpRequest) {
        // When the Flash player wants to access the host files, it asks for this one...
        if("/crossdomain.xml".equals(httpRequest.getResourcePath())) {
          return new WebServerContent() {
            @Override
            public InputStream getInputStream() {
              return getInputStream("<?xml version=\"1.0\"?>" + Utils.LINE_SEPARATOR +
                                    "<!DOCTYPE cross-domain-policy SYSTEM \"http://www.adobe.com/xml/dtds/cross-domain-policy.dtd\">" + Utils.LINE_SEPARATOR +
                                    "<cross-domain-policy>" + Utils.LINE_SEPARATOR +
                                    "  <site-control permitted-cross-domain-policies=\"all\"/>" + Utils.LINE_SEPARATOR +
                                    "  <allow-access-from domain=\"*\" secure=\"false\"/>" + Utils.LINE_SEPARATOR +
                                    "  <allow-http-request-headers-from domain=\"*\" headers=\"*\" secure=\"false\"/>" + Utils.LINE_SEPARATOR +
                                    "</cross-domain-policy>"
              );
            }
          };
        }
        return null;
      }
    });
  }

  /**
   * A factory that creates the decorators for flash players.
   * @author Christopher Deckers
   */
  public static interface FlashPlayerDecoratorFactory {
    /**
     * Create the decorator for a flash player, which adds the rendering component to its component hierarchy and will itself be added to the flash player.
     * @param flashPlayer the flash player for which to create the decorator.
     * @param renderingComponent the component that renders the flash player's content.
     * @return the decorator.
     */
    public FlashPlayerDecorator createFlashPlayerDecorator(JFlashPlayer flashPlayer, Component renderingComponent);
  }

  private static FlashPlayerDecoratorFactory flashPlayerDecoratorFactory;

  /**
   * Set the decorator that will be used for future flash player instances.
   * @param flashPlayerDecoratorFactory the factory that creates the decorators, or null for default decorators.
   */
  public static void setFlashPlayerDecoratorFactory(FlashPlayerDecoratorFactory flashPlayerDecoratorFactory) {
    JFlashPlayer.flashPlayerDecoratorFactory = flashPlayerDecoratorFactory;
  }

  private FlashPlayerDecorator flashPlayerDecorator;

  FlashPlayerDecorator getFlashPlayerDecorator() {
    return flashPlayerDecorator;
  }

  /**
   * Create a decorator for this flash player. This method can be overriden so that the flash player uses a different decorator.
   * @param renderingComponent the component to add to the decorator's component hierarchy.
   * @return the decorator that was created.
   */
  protected FlashPlayerDecorator createFlashPlayerDecorator(Component renderingComponent) {
    if(flashPlayerDecoratorFactory != null) {
      FlashPlayerDecorator flashPlayerDecorator = flashPlayerDecoratorFactory.createFlashPlayerDecorator(this, renderingComponent);
      if(flashPlayerDecorator != null) {
        return flashPlayerDecorator;
      }
    }
    return new DefaultFlashPlayerDecorator(this, renderingComponent);
  }

  private JWebBrowser webBrowser;

  private static class NWebBrowserObject extends WebBrowserObject {

    private final JFlashPlayer flashPlayer;

    NWebBrowserObject(JFlashPlayer flashPlayer) {
      super(flashPlayer.webBrowser);
      this.flashPlayer = flashPlayer;
    }

    @Override
    protected ObjectHTMLConfiguration getObjectHtmlConfiguration() {
      ObjectHTMLConfiguration objectHTMLConfiguration = new ObjectHTMLConfiguration();
      if(flashPlayer.options != null) {
        // Possible when debugging and calling the same URL again. No options but better than nothing.
        Map<String, String> htmlParameters = flashPlayer.options.getHTMLParameters();
        if(!htmlParameters.containsKey("base")) {
          String loadedResource = flashPlayer.webBrowserObject.getLoadedResource();
          if(loadedResource != null) {
            int lastIndex = loadedResource.lastIndexOf('/');
            htmlParameters.put("base", loadedResource.substring(0, lastIndex + 1));
          }
        }
        objectHTMLConfiguration.setHTMLParameters(htmlParameters);
      }
      objectHTMLConfiguration.setWindowsClassID("D27CDB6E-AE6D-11cf-96B8-444553540000");
      objectHTMLConfiguration.setWindowsInstallationURL("http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=9,0,0,0");
      objectHTMLConfiguration.setMimeType("application/x-shockwave-flash");
      objectHTMLConfiguration.setInstallationURL("http://www.adobe.com/go/getflashplayer");
      objectHTMLConfiguration.setWindowsParamName("movie");
      objectHTMLConfiguration.setParamName("src");
//      flashPlayer.options = null;
      return objectHTMLConfiguration;
    }

    private final String LS = Utils.LINE_SEPARATOR;

    @Override
    protected String getJavascriptDefinitions() {
      String javascriptDefinitions = flashPlayer.customJavascriptDefinitions;
      return
        "      function " + getEmbeddedObjectJavascriptName() + "_DoFSCommand(command, args) {" + LS +
        "        sendCommand(command, args);" + LS +
        "      }" +
        (javascriptDefinitions == null? "": LS + javascriptDefinitions);
    }

    @Override
    protected String getAdditionalHeadDefinitions() {
      return
      "    <script language=\"VBScript\">" + LS +
      "    <!-- " + LS +
      "    Sub " + getEmbeddedObjectJavascriptName() + "_FSCommand(ByVal command, ByVal args)" + LS +
      "      call " + getEmbeddedObjectJavascriptName() + "_DoFSCommand(command, args)" + LS +
      "    end sub" + LS +
      "    //-->" + LS +
      "    </script>";
    }

    @Override
    public String getLocalFileURL(File localFile) {
      if(Boolean.parseBoolean(NSSystemProperty.WEBSERVER_ACTIVATEOLDRESOURCEMETHOD.get())) {
        // Local files cannot be played due to security restrictions. We need to proxy.
        // Moreover, we need to double encode non ASCII characters.
        return WebServer.getDefaultWebServer().getResourcePathURL(encodeSpecialCharacters(localFile.getParent()), encodeSpecialCharacters(localFile.getName()));
      }
      return WebServer.getDefaultWebServer().getResourcePathURL(localFile.getParent(), localFile.getName());
    }

    private String encodeSpecialCharacters(String s) {
      // We have to convert all special remaining characters (e.g. letters with accents).
      StringBuilder sb = new StringBuilder();
      for(int i=0; i<s.length(); i++) {
        char c = s.charAt(i);
        boolean isToEncode = false;
        if((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9')) {
          switch(c) {
            case '.':
            case '-':
            case '*':
            case '_':
            case '+':
            case '%':
            case ':':
            case '/':
              break;
            case '\\':
              if(Utils.IS_WINDOWS) {
                c = '/';
              }
              break;
            default:
              isToEncode = true;
              break;
          }
        }
        if(isToEncode) {
          sb.append(Utils.encodeURL(String.valueOf(c)));
        } else {
          sb.append(c);
        }
      }
      return sb.toString();
    }

  }

  private WebBrowserObject webBrowserObject;

  /**
   * Construct a flash player.
   * @param options the options to configure the behavior of this component.
   */
  public JFlashPlayer(NSOption... options) {
    Map<Object, Object> optionMap = NSOption.createOptionMap(options);
    customJavascriptDefinitions = (String)optionMap.get(SET_CUSTOM_JAVASCRIPT_DEFINITIONS_OPTION_KEY);
    webBrowser = new JWebBrowser(options);
    initialize(webBrowser.getNativeComponent());
    webBrowserObject = new NWebBrowserObject(this);
    webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
      @Override
      public void commandReceived(WebBrowserCommandEvent e) {
        String command = e.getCommand();
        Object[] parameters = e.getParameters();
        boolean isInternal = command.startsWith("[Chrriis]");
        FlashPlayerCommandEvent ev = null;
        for(FlashPlayerListener listener: getFlashPlayerListeners()) {
          if(!isInternal || listener.getClass().getName().startsWith("chrriis.")) {
            if(ev == null) {
              ev = new FlashPlayerCommandEvent(JFlashPlayer.this, command, parameters);
            }
            listener.commandReceived(ev);
          }
        }
      }
    });
    flashPlayerDecorator = createFlashPlayerDecorator(webBrowser);
    add(flashPlayerDecorator, BorderLayout.CENTER);
  }

  private volatile String customJavascriptDefinitions;

//  public String getLoadedResource() {
//    return webBrowserObject.getLoadedResource();
//  }

  /**
   * Load a file from the classpath.
   * @param clazz the reference clazz of the file to load.
   * @param resourcePath the path to the file.
   */
  public void load(Class<?> clazz, String resourcePath) {
    load(clazz, resourcePath, null);
  }

  /**
   * Load a file from the classpath.
   * @param clazz the reference clazz of the file to load.
   * @param resourcePath the path to the file.
   * @param options the options to better configure the initialization of the flash plugin.
   */
  public void load(Class<?> clazz, String resourcePath, FlashPluginOptions options) {
    addReferenceClassLoader(clazz.getClassLoader());
    load(WebServer.getDefaultWebServer().getClassPathResourceURL(clazz.getName(), resourcePath), options);
  }

  /**
   * Load a file.
   * @param resourceLocation the path or URL to the file.
   */
  public void load(String resourceLocation) {
    load(resourceLocation, null);
  }

  private volatile FlashPluginOptions options;

  /**
   * Load a file.
   * @param resourceLocation the path or URL to the file.
   * @param options the options to better configure the initialization of the flash plugin.
   */
  public void load(String resourceLocation, FlashPluginOptions options) {
    if("".equals(resourceLocation)) {
      resourceLocation = null;
    }
    if(options == null) {
      options = new FlashPluginOptions();
    }
    this.options = options;
    webBrowserObject.load(resourceLocation);
  }

  /**
   * Play a timeline-based flash applications.
   */
  public void play() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowserObject.invokeObjectFunction("Play");
  }

  /**
   * Pause the execution of timeline-based flash applications.
   */
  public void pause() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowserObject.invokeObjectFunction("StopPlay");
  }

  /**
   * Stop the execution of timeline-based flash applications.
   */
  public void stop() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowserObject.invokeObjectFunction("Rewind");
  }

  /**
   * Set the value of a variable. It is also possible to set object properties with that method, though it is recommended to create special accessor methods.
   * @param name the name of the variable.
   * @param value the new value of the variable.
   */
  public void setVariable(String name, String value) {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowserObject.invokeObjectFunction("SetVariable", name, value);
  }

  /**
   * Get the value of a variable, or an object property if the web browser used is Internet Explorer. On Mozilla, it is not possible to access object properties with that method, an accessor method or a global variable in the Flash application should be used instead.
   * @return the value, potentially a String, Number, Boolean.
   */
  public Object getVariable(String name) {
    if(!webBrowserObject.hasContent()) {
      return null;
    }
    return webBrowserObject.invokeObjectFunctionWithResult("GetVariable", name);
  }

  /**
   * Invoke a function on the Flash object, with optional arguments (Strings, numbers, booleans).
   * @param functionName the name of the function to invoke.
   * @param args optional arguments.
   */
  public void invokeFlashFunction(String functionName, Object... args) {
    webBrowserObject.invokeObjectFunction(functionName, args);
  }

  /**
   * Invoke a function on the Flash object and waits for a result, with optional arguments (Strings, numbers, booleans).
   * @param functionName the name of the function to invoke.
   * @param args optional arguments.
   * @return The value, potentially a String, Number, Boolean.
   */
  public Object invokeFlashFunctionWithResult(String functionName, Object... args) {
    return webBrowserObject.invokeObjectFunctionWithResult(functionName, args);
  }

  /**
   * Get the web browser that contains this component. The web browser should only be used to add listeners, for example to listen to window creation events.
   * @return the web browser.
   */
  public JWebBrowser getWebBrowser() {
    return webBrowser;
  }

  /**
   * Indicate whether the control bar is visible.
   * @return true if the control bar is visible.
   */
  public boolean isControlBarVisible() {
    return flashPlayerDecorator.isControlBarVisible();
  }

  /**
   * Set whether the control bar is visible.
   * @param isControlBarVisible true if the control bar should be visible, false otherwise.
   */
  public void setControlBarVisible(boolean isControlBarVisible) {
    flashPlayerDecorator.setControlBarVisible(isControlBarVisible);
  }

  /**
   * Add a flash player listener.
   * @param listener The flash player listener to add.
   */
  public void addFlashPlayerListener(FlashPlayerListener listener) {
    listenerList.add(FlashPlayerListener.class, listener);
  }

  /**
   * Remove a flash player listener.
   * @param listener the flash player listener to remove.
   */
  public void removeFlashPlayerListener(FlashPlayerListener listener) {
    listenerList.remove(FlashPlayerListener.class, listener);
  }

  /**
   * Get the flash player listeners.
   * @return the flash player listeners.
   */
  public FlashPlayerListener[] getFlashPlayerListeners() {
    return listenerList.getListeners(FlashPlayerListener.class);
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

  @Override
  public void removeNotify() {
    super.removeNotify();
    cleanup();
  }

  @Override
  public void disposeNativePeer() {
    super.disposeNativePeer();
    cleanup();
  }

  private void cleanup() {
    if(isNativePeerDisposed()) {
      webBrowserObject.load(null);
    }
  }

}
