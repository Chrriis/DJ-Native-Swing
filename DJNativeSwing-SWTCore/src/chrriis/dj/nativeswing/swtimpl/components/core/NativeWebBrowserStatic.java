/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.core;

import chrriis.dj.nativeswing.swtimpl.components.internal.INativeWebBrowserStatic;

/**
 * @author Christopher Deckers
 */
class NativeWebBrowserStatic implements INativeWebBrowserStatic {

  public void clearSessionCookies() {
    NativeWebBrowser.clearSessionCookies();
  }
  public String getCookie(String url, String name) {
    return NativeWebBrowser.getCookie(url, name);
  }
  public void setCookie(String url, String value) {
    NativeWebBrowser.setCookie(url, value);
  }

}
