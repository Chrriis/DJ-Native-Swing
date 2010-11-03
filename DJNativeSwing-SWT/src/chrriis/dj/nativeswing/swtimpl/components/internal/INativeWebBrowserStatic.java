/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.internal;

/**
 * @author Christopher Deckers
 */
public interface INativeWebBrowserStatic {

  public void clearSessionCookies();
  public String getCookie(String url, String name);
  public void setCookie(String url, String value);

}
