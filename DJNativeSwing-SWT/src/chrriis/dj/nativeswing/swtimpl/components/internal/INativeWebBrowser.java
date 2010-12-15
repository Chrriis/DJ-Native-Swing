/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.internal;

import java.awt.Component;
import java.util.Map;

import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAuthenticationHandler;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserFunction;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationParameters;

/**
 * @author Christopher Deckers
 */
public interface INativeWebBrowser {

  static enum WebBrowserRuntime {
    DEFAULT,
    XULRUNNER,
    WEBKIT
  }

  public static final String CONSOLE_OUT_FUNCTION = "nsConsoleOut";
  public static final String CONSOLE_ERR_FUNCTION = "nsConsoleErr";
  public static final String COMMAND_FUNCTION = "sendNSCommand";
  public static final String COMMAND_LOCATION_PREFIX = "command://";
  public static final String COMMAND_STATUS_PREFIX = "scommand://";

  public WebBrowserRuntime getRuntime();

  public String getResourceLocation();

  public boolean navigate(String resourceLocation, WebBrowserNavigationParameters parameters);

  public String getHTMLContent();

  public boolean setHTMLContent(String html);

  public boolean isJavascriptEnabled();

  public void setJavascriptEnabled(boolean isJavascriptEnabled);

  public boolean executeJavascriptAndWait(String script);

  public void executeJavascript(String script);

  public Object executeJavascriptWithResult(String script);

  public void stopLoading();

  public void reloadPage();

  public boolean isBackNavigationEnabled();

  public void navigateBack();

  public boolean isForwardNavigationEnabled();

  public void navigateForward();

  public void setDefaultPopupMenuRegistered(boolean isDefaultPopupMenuRegistered);

  public String getStatusText();

  public String getPageTitle();

  public int getLoadingProgress();

  public void registerFunction(WebBrowserFunction function);

  public void unregisterFunction(WebBrowserFunction function);

  public void setAuthenticationHandler(WebBrowserAuthenticationHandler authenticationHandler);

  public WebBrowserAuthenticationHandler getAuthenticationHandler();

  public String getBrowserType();

  public String getBrowserVersion();

  public void addWebBrowserListener(WebBrowserListener listener);

  public void removeWebBrowserListener(WebBrowserListener listener);

  public Component createEmbeddableComponent(Map<Object, Object> optionMap);

  public boolean unloadAndDispose();

  public void requestFocus();

  public boolean isNativePeerDisposed();

  public boolean isNativePeerInitialized();

  public boolean print(boolean isShowingDialog);

}
