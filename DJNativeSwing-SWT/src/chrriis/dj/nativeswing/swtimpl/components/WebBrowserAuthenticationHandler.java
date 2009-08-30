/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

/**
 * The handler for authentications.
 * @author Christopher Deckers
 */
public interface WebBrowserAuthenticationHandler {

  /**
   * Get the credentials, or credentials with null user and password to get default prompt, or null to cancel the authentication request.
   * @return the credentials.
   */
  public Credentials getCredentials(JWebBrowser webBrowser, String resourceLocation);

}
