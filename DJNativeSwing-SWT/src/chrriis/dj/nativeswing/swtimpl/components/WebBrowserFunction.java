/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

/**
 * @author Christopher Deckers
 */
abstract class WebBrowserFunction {

  private String name;

  public WebBrowserFunction(String functionName) {
    name = functionName;
  }

  public String getName() {
    return name;
  }

  protected abstract Object invoke(JWebBrowser webBrowser, Object... args);

}
