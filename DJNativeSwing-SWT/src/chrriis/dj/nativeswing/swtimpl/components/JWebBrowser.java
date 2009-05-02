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
import java.awt.Window;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.Utils;
import chrriis.dj.nativeswing.NSOption;
import chrriis.dj.nativeswing.swtimpl.EventDispatchUtils;
import chrriis.dj.nativeswing.swtimpl.NSPanelComponent;

/**
 * A native web browser, using Internet Explorer or Mozilla on Windows, and Mozilla on other platforms.<br/>
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fails, the methods will not have any effect. The results from methods have relevant values only when the component is valid.
 * @author Christopher Deckers
 */
public class JWebBrowser extends NSPanelComponent {

  /** The prefix to use when sending a command from some web content, using a static link or by setting window.location from Javascript. */
  public static final String COMMAND_LOCATION_PREFIX = NativeWebBrowser.COMMAND_LOCATION_PREFIX;

  /** The prefix to use when sending a command from some web content, by setting window.status from Javascript. */
  public static final String COMMAND_STATUS_PREFIX = NativeWebBrowser.COMMAND_STATUS_PREFIX;

  private static final String USE_XULRUNNER_RUNTIME_OPTION_KEY = "XULRunner Runtime";
  private static final NSOption XUL_RUNNER_RUNTIME_OPTION = new NSOption(USE_XULRUNNER_RUNTIME_OPTION_KEY);

  /**
   * Create an option to make the web browser use the Mozilla XULRunner runtime.
   * @return the option to use the XULRunner runtime.
   */
  public static NSOption useXULRunnerRuntime() {
    return XUL_RUNNER_RUNTIME_OPTION;
  }

  /**
   * A factory that creates the decorators for web browsers.
   * @author Christopher Deckers
   */
  public static interface WebBrowserDecoratorFactory {
    /**
     * Create the decorator for a web browser, which adds the rendering component to its component hierarchy and will itself be added to the web browser.
     * @param webBrowser the webbrowser for which to create the decorator.
     * @param renderingComponent the component that renders the web browser's content.
     * @return the decorator.
     */
    public WebBrowserDecorator createWebBrowserDecorator(JWebBrowser webBrowser, Component renderingComponent);
  }

  private static WebBrowserDecoratorFactory webBrowserDecoratorFactory;

  /**
   * Set the decorator that will be used for future web browser instances.
   * @param webBrowserDecoratorFactory the factory that creates the decorators, or null for default decorators.
   */
  public static void setWebBrowserDecoratorFactory(WebBrowserDecoratorFactory webBrowserDecoratorFactory) {
    JWebBrowser.webBrowserDecoratorFactory = webBrowserDecoratorFactory;
  }

  private WebBrowserDecorator webBrowserDecorator;

  WebBrowserDecorator getWebBrowserDecorator() {
    return webBrowserDecorator;
  }

  /**
   * Create a decorator for this web browser. This method can be overriden so that the web browser uses a different decorator.
   * @param renderingComponent the component to add to the decorator's component hierarchy.
   * @return the decorator that was created.
   */
  protected WebBrowserDecorator createWebBrowserDecorator(Component renderingComponent) {
    if(webBrowserDecoratorFactory != null) {
      WebBrowserDecorator webBrowserDecorator = webBrowserDecoratorFactory.createWebBrowserDecorator(this, renderingComponent);
      if(webBrowserDecorator != null) {
        return webBrowserDecorator;
      }
    }
    return new DefaultWebBrowserDecorator(this, renderingComponent);
  }

  /**
   * Clear all session cookies from all web browser instances.
   */
  public static void clearSessionCookies() {
    NativeWebBrowser.clearSessionCookies();
  }

  /**
   * Get a cookie for a given URL and a given name.
   * @return the cookie or null if it does not exist.
   */
  public static String getCookie(String url, String name) {
    return NativeWebBrowser.getCookie(url, name);
  }

  /**
   * Set a cookie for all web browser instances.
   * @param url the url.
   * @param value the value, in a cookie form like:
   * <code>foo=bar</code> (basic session cookie)
   * <code>foo=bar; path=/; domain=.eclipse.org</code> (session cookie)
   * <code>foo=bar; expires=Thu, 01-Jan-2030 00:00:01 GMT</code> (persistent cookie)
   * <code>foo=; expires=Thu, 01-Jan-1970 00:00:01 GMT</code> (deletes cookie <code>foo</code>)
   */
  public static void setCookie(String url, String value) {
    NativeWebBrowser.setCookie(url, value);
  }

  private NativeWebBrowser nativeWebBrowser;

  /**
   * Copy the appearance, the visibility of the various bars, from one web browser to another.
   * @param fromWebBrowser the web browser to copy the appearance from.
   * @param toWebBrowser the web browser to copy the appearance to.
   */
  public static void copyAppearance(JWebBrowser fromWebBrowser, JWebBrowser toWebBrowser) {
    toWebBrowser.setLocationBarVisible(fromWebBrowser.isLocationBarVisible());
    toWebBrowser.setButtonBarVisible(fromWebBrowser.isButtonBarVisible());
    toWebBrowser.setMenuBarVisible(fromWebBrowser.isMenuBarVisible());
    toWebBrowser.setStatusBarVisible(fromWebBrowser.isStatusBarVisible());
  }

  /**
   * Copy the content, whether a URL or its HTML content, from one web browser to another.
   * @param fromWebBrowser the web browser to copy the content from.
   * @param toWebBrowser the web browser to copy the content to.
   */
  public static void copyContent(JWebBrowser fromWebBrowser, JWebBrowser toWebBrowser) {
    String location = fromWebBrowser.getResourceLocation();
    if("about:blank".equals(location)) {
      toWebBrowser.setHTMLContent(fromWebBrowser.getHTMLContent());
    } else {
      toWebBrowser.navigate(location);
    }
  }

  private Component embeddableComponent;

  /**
   * Construct a new web browser.
   * @param options the options to configure the behavior of this component.
   */
  public JWebBrowser(NSOption... options) {
    Map<Object, Object> optionMap = NSOption.createOptionMap(options);
    nativeWebBrowser = new NativeWebBrowser(this, optionMap.get(USE_XULRUNNER_RUNTIME_OPTION_KEY) != null);
    initialize(nativeWebBrowser);
    embeddableComponent = nativeWebBrowser.createEmbeddableComponent(optionMap);
    webBrowserDecorator = createWebBrowserDecorator(embeddableComponent);
    add(webBrowserDecorator, BorderLayout.CENTER);
    setButtonBarVisible(true);
    setLocationBarVisible(true);
    setStatusBarVisible(true);
  }

  /**
   * Set whether the status bar is visible.
   * @param isStatusBarVisible true if the status bar should be visible, false otherwise.
   */
  public void setStatusBarVisible(boolean isStatusBarVisible) {
    webBrowserDecorator.setStatusBarVisible(isStatusBarVisible);
  }

  /**
   * Indicate whether the status bar is visible.
   * @return true if the status bar is visible.
   */
  public boolean isStatusBarVisible() {
    return webBrowserDecorator.isStatusBarVisible();
  }

  /**
   * Set whether the menu bar is visible.
   * @param isMenuBarVisible true if the menu bar should be visible, false otherwise.
   */
  public void setMenuBarVisible(boolean isMenuBarVisible) {
    webBrowserDecorator.setMenuBarVisible(isMenuBarVisible);
  }

  /**
   * Indicate whether the menu bar is visible.
   * @return true if the menu bar is visible.
   */
  public boolean isMenuBarVisible() {
    return webBrowserDecorator.isMenuBarVisible();
  }

  /**
   * Set whether the button bar is visible.
   * @param isButtonBarVisible true if the button bar should be visible, false otherwise.
   */
  public void setButtonBarVisible(boolean isButtonBarVisible) {
    webBrowserDecorator.setButtonBarVisible(isButtonBarVisible);
  }

  /**
   * Indicate whether the button bar is visible.
   * @return true if the button bar is visible.
   */
  public boolean isButtonBarVisible() {
    return webBrowserDecorator.isButtonBarVisible();
  }

  /**
   * Set whether the location bar is visible.
   * @param isLocationBarVisible true if the location bar should be visible, false otherwise.
   */
  public void setLocationBarVisible(boolean isLocationBarVisible) {
    webBrowserDecorator.setLocationBarVisible(isLocationBarVisible);
  }

  /**
   * Indicate whether the location bar is visible.
   * @return true if the location bar is visible.
   */
  public boolean isLocationBarVisible() {
    return webBrowserDecorator.isLocationBarVisible();
  }

  /**
   * Get the title of the web page.
   * @return the title of the page.
   */
  public String getPageTitle() {
    return nativeWebBrowser.getPageTitle();
  }

  /**
   * Get the status text.
   * @return the status text.
   */
  public String getStatusText() {
    return nativeWebBrowser.getStatusText();
  }

  /**
   * Get the HTML content.
   * @return the HTML content.
   */
  public String getHTMLContent() {
    return nativeWebBrowser.getHTMLContent();
  }

  /**
   * Set the HTML content.
   * @param html the HTML content.
   */
  public boolean setHTMLContent(String html) {
    return nativeWebBrowser.setHTMLContent(html);
  }

  /**
   * Get the location of the resource currently displayed.
   * @return the location.
   */
  public String getResourceLocation() {
    return nativeWebBrowser.getResourceLocation();
  }

  /**
   * Navigate to a resource, with its location specified as a URL or path.
   * @param resourceLocation the URL or path.
   * @return true if the navigation was successful.
   */
  public boolean navigate(String resourceLocation) {
    return nativeWebBrowser.navigate(resourceLocation);
  }

  /**
   * Indicate if the web browser Back functionality is enabled.
   * @return true if the web browser Back functionality is enabled.
   */
  public boolean isBackNavigationEnabled() {
    return nativeWebBrowser.isBackNavigationEnabled();
  }

  /**
   * Invoke the web browser Back functionality.
   */
  public void navigateBack() {
    nativeWebBrowser.navigateBack();
  }

  /**
   * Indicate if the web browser Forward functionality is enabled.
   * @return true if the web browser Forward functionality is enabled.
   */
  public boolean isForwardNavigationEnabled() {
    return nativeWebBrowser.isForwardNavigationEnabled();
  }

  /**
   * Invoke the web browser Forward functionality.
   */
  public void navigateForward() {
    nativeWebBrowser.navigateForward();
  }

  /**
   * Invoke the web browser Reload functionality.
   */
  public void reloadPage() {
    nativeWebBrowser.reloadPage();
  }

  /**
   * Invoke the web browser Stop functionality, to stop all current loading operations.
   */
  public void stopLoading() {
    nativeWebBrowser.stopLoading();
  }

  /**
   * Indicate if Javascript will be allowed to run in pages subsequently viewed.
   * @return true if Javascript is enabled.
   */
  public boolean isJavascriptEnabled() {
    return nativeWebBrowser.isJavascriptEnabled();
  }

  /**
   * Set whether javascript will be allowed to run in pages subsequently.
   * Note that setting this value does not affect the running of javascript in the current page.
   * @param isJavascriptEnabled true to enable Javascript, false otherwise.
   */
  public void setJavascriptEnabled(boolean isJavascriptEnabled) {
    nativeWebBrowser.setJavascriptEnabled(isJavascriptEnabled);
  }

//  /**
//   * Execute some javascript, and wait for the indication of success.
//   * @param javascript the javascript to execute.
//   * @return true if the execution succeeded.
//   */
//  public boolean executeJavascriptAndWait(String javascript) {
//    return nativeComponent.executeJavascriptAndWait(javascript);
//  }

  /**
   * Execute some javascript.
   * @param javascript the javascript to execute.
   */
  public void executeJavascript(String javascript) {
    nativeWebBrowser.executeJavascript(javascript);
  }

  /**
   * Execute some javascript, and wait for the result coming from the return statements.
   * @param javascript the javascript to execute which must contain explicit return statements.
   * @return the value, potentially a String, Number, Boolean.
   */
  public Object executeJavascriptWithResult(String javascript) {
    if(!javascript.endsWith(";")) {
      javascript = javascript + ";";
    }
//    return nativeWebBrowser.executeJavascriptWithResult(
//        "try {" +
//        "  return function() {" + javascript + "}();" +
//        "} catch(exxxxx) {" +
//        "  return null;" +
//        "}");
    String[] result = executeJavascriptWithCommandResult("[[getScriptResult]]",
        "try {" +
        "  var result = function() {" + javascript + "}();" +
        "  var type = result? typeof(result): '';" +
        "  if('string' == type) {" +
        "    window.location = '" + JWebBrowser.COMMAND_LOCATION_PREFIX + "' + encodeURIComponent('[[getScriptResult]]') + '&' + encodeURIComponent(result);" +
        "  } else {" +
        "    window.location = '" + JWebBrowser.COMMAND_LOCATION_PREFIX + "' + encodeURIComponent('[[getScriptResult]]') + '&' + encodeURIComponent(type) + '&' + encodeURIComponent(result);" +
        "  }" +
        "} catch(exxxxx) {" +
        "  window.location = '" + JWebBrowser.COMMAND_LOCATION_PREFIX + "' + encodeURIComponent('[[getScriptResult]]') + '&&'" +
        "}");
    if(result == null) {
      return null;
    }
    if(result.length == 1) {
      return convertJavascriptObjectToJava("string", result[0]);
    }
    return convertJavascriptObjectToJava(result[0], result[1]);
  }

  /**
   * Create the Javascript function call using the function name and Java objects as arguments. Note that it does not contain a semi-colon at the end of the statement, to allow call chaining.
   * @param functionName the name of the Javascript funtion.
   * @param args the Java objects (String, number, boolean, or array) which will get converted to Javascript arguments.
   * @return the function call, in the form "functionName(convArg1, convArg2, ...)".
   */
  public static String createJavascriptFunctionCall(String functionName, Object... args) {
    StringBuilder sb = new StringBuilder();
    sb.append(functionName).append('(');
    for(int i=0; i<args.length; i++) {
      if(i > 0) {
        sb.append(", ");
      }
      sb.append(convertJavaObjectToJavascript(args[i]));
    }
    sb.append(")");
    return sb.toString();
  }

  /**
   * Convert a Java object to Javascript, to simplify the task of executing scripts. Conversion adds quotes around Strings (with Java escaping and Javascript unescaping around), add brackets to arrays, treats arrays of arrays, and can handle null values.
   * @param o the object to convert, which can be a String, number, boolean, or array.
   */
  public static String convertJavaObjectToJavascript(Object o) {
    if(o == null) {
      return "null";
    }
    if(o instanceof Boolean || o instanceof Number) {
      return o.toString();
    }
    if(o.getClass().isArray()) {
      StringBuilder sb = new StringBuilder();
      sb.append('[');
      int length = Array.getLength(o);
      for(int i=0; i<length; i++) {
        if(i > 0) {
          sb.append(", ");
        }
        sb.append(convertJavaObjectToJavascript(Array.get(o, i)));
      }
      sb.append(']');
      return sb.toString();
    }
    o = o.toString();
    String encodedArg = Utils.encodeURL((String)o);
    if(o.equals(encodedArg)) {
      return '\'' + (String)o + '\'';
    }
    return "decodeURIComponent('" + encodedArg + "')";
  }

  private static Object convertJavascriptObjectToJava(String type, String value) {
    if(type.length() == 0) {
      return null;
    }
    if("boolean".equals(type)) {
      return Boolean.parseBoolean(value);
    }
    if("number".equals(type)) {
      try {
        return Integer.parseInt(value);
      } catch(Exception e) {}
      try {
        return Float.parseFloat(value);
      } catch(Exception e) {}
      try {
        return Long.parseLong(value);
      } catch(Exception e) {}
      throw new IllegalStateException("Could not convert number: " + value);
    }
    return value;
  }

  private static class NCommandListener extends WebBrowserAdapter {
    private String command;
    private Object[] resultArray;
    private NCommandListener(String command, Object[] resultArray) {
      this.command = command;
      this.resultArray = resultArray;
    }
    @Override
    public void commandReceived(WebBrowserEvent e, String command, String[] args) {
      if(this.command.equals(command)) {
        resultArray[0] = args;
        ((NativeWebBrowser)e.getWebBrowser().getNativeComponent()).removeWebBrowserListener(this);
      }
    }
  }

  private String[] executeJavascriptWithCommandResult(final String command, String script) {
    if(!nativeWebBrowser.isNativePeerInitialized()) {
      return null;
    }
    final Object[] resultArray = new Object[] {null};
    WebBrowserAdapter webBrowserListener = new NCommandListener(command, resultArray);
    nativeWebBrowser.addWebBrowserListener(webBrowserListener);
    if(nativeWebBrowser.executeJavascriptAndWait(script)) {
      for(int i=0; i<20; i++) {
        EventDispatchUtils.sleepWithEventDispatch(new EventDispatchUtils.Condition() {
          public boolean getValue() {
            return resultArray[0] != null;
          }
        }, 50);
      }
    }
    nativeWebBrowser.removeWebBrowserListener(webBrowserListener);
    return (String[])resultArray[0];
  }

  /**
   * Get the loading progress, a value between 0 and 100, where 100 means it is fully loaded.
   * @return a value between 0 and 100 indicating the current loading progress.
   */
  public int getLoadingProgress() {
    return nativeWebBrowser.getLoadingProgress();
  }

  private static class NativeWebBrowserListener implements WebBrowserListener {

    private Reference<WebBrowserListener> webBrowserListener;

    public NativeWebBrowserListener(WebBrowserListener webBrowserListener) {
      this.webBrowserListener = new WeakReference<WebBrowserListener>(webBrowserListener);
    }

    public void commandReceived(WebBrowserEvent e, String command, String[] args) {
      WebBrowserListener webBrowserListener = this.webBrowserListener.get();
      if(webBrowserListener != null) {
        boolean isInternal = command.startsWith("[Chrriis]");
        if(!isInternal || webBrowserListener.getClass().getName().startsWith("chrriis.")) {
          webBrowserListener.commandReceived(e, command, args);
        }
      }
    }

    public void loadingProgressChanged(WebBrowserEvent e) {
      WebBrowserListener webBrowserListener = this.webBrowserListener.get();
      if(webBrowserListener != null) {
        webBrowserListener.loadingProgressChanged(e);
      }
    }

    public void locationChangeCanceled(WebBrowserNavigationEvent e) {
      WebBrowserListener webBrowserListener = this.webBrowserListener.get();
      if(webBrowserListener != null) {
        webBrowserListener.locationChangeCanceled(e);
      }
    }

    public void locationChanged(WebBrowserNavigationEvent e) {
      WebBrowserListener webBrowserListener = this.webBrowserListener.get();
      if(webBrowserListener != null) {
        webBrowserListener.locationChanged(e);
      }
    }

    public void locationChanging(WebBrowserNavigationEvent e) {
      WebBrowserListener webBrowserListener = this.webBrowserListener.get();
      if(webBrowserListener != null) {
        webBrowserListener.locationChanging(e);
      }
    }

    public void statusChanged(WebBrowserEvent e) {
      WebBrowserListener webBrowserListener = this.webBrowserListener.get();
      if(webBrowserListener != null) {
        webBrowserListener.statusChanged(e);
      }
    }

    public void titleChanged(WebBrowserEvent e) {
      WebBrowserListener webBrowserListener = this.webBrowserListener.get();
      if(webBrowserListener != null) {
        webBrowserListener.titleChanged(e);
      }
    }

    public void windowClosing(WebBrowserEvent e) {
      WebBrowserListener webBrowserListener = this.webBrowserListener.get();
      if(webBrowserListener != null) {
        webBrowserListener.windowClosing(e);
      }
    }

    public void windowOpening(WebBrowserWindowOpeningEvent e) {
      WebBrowserListener webBrowserListener = this.webBrowserListener.get();
      if(webBrowserListener != null) {
        webBrowserListener.windowOpening(e);
      }
    }

    public void windowWillOpen(WebBrowserWindowWillOpenEvent e) {
      WebBrowserListener webBrowserListener = this.webBrowserListener.get();
      if(webBrowserListener != null) {
        webBrowserListener.windowWillOpen(e);
      }
    }

  }

  private Map<WebBrowserListener, NativeWebBrowserListener> webBrowserListenerToNativeWebBrowserListenerMap = new HashMap<WebBrowserListener, NativeWebBrowserListener>();

  /**
   * Add a web browser listener.
   * @param listener The web browser listener to add.
   */
  public void addWebBrowserListener(WebBrowserListener listener) {
    listenerList.add(WebBrowserListener.class, listener);
    NativeWebBrowserListener nativeWebBrowserListener = new NativeWebBrowserListener(listener);
    webBrowserListenerToNativeWebBrowserListenerMap.put(listener, nativeWebBrowserListener);
    nativeWebBrowser.addWebBrowserListener(nativeWebBrowserListener);
  }

  /**
   * Remove a web browser listener.
   * @param listener the web browser listener to remove.
   */
  public void removeWebBrowserListener(WebBrowserListener listener) {
    listenerList.remove(WebBrowserListener.class, listener);
    NativeWebBrowserListener nativeWebBrowserListener = webBrowserListenerToNativeWebBrowserListenerMap.get(listener);
    if(nativeWebBrowserListener != null) {
      nativeWebBrowser.removeWebBrowserListener(nativeWebBrowserListener);
    }
  }

  /**
   * Get the web browser listeners.
   * @return the web browser listeners.
   */
  public WebBrowserListener[] getWebBrowserListeners() {
    return listenerList.getListeners(WebBrowserListener.class);
  }

  /**
   * Show or hide all the bars at once.
   * @param areBarsVisible true to show all bars, false to hide them all.
   */
  public void setBarsVisible(boolean areBarsVisible) {
    setMenuBarVisible(areBarsVisible);
    setButtonBarVisible(areBarsVisible);
    setLocationBarVisible(areBarsVisible);
    setStatusBarVisible(areBarsVisible);
  }

  /**
   * Get the web browser window if the web browser is contained in one.
   * @return the web browser Window, or null.
   */
  public JWebBrowserWindow getWebBrowserWindow() {
    Window window = SwingUtilities.getWindowAncestor(this);
    if(window instanceof JWebBrowserWindow) {
      return (JWebBrowserWindow)window;
    }
    return null;
  }

  /**
   * Set whether this component is able to detect a popup menu gesture to show its default popup menu.
   * @param isDefaultPopupMenuRegistered true if the default popup menu is registered.
   */
  public void setDefaultPopupMenuRegistered(boolean isDefaultPopupMenuRegistered) {
    nativeWebBrowser.setDefaultPopupMenuRegistered(isDefaultPopupMenuRegistered);
  }

  JPanel getEmbeddableComponent() {
    return (JPanel)embeddableComponent;
  }

}
