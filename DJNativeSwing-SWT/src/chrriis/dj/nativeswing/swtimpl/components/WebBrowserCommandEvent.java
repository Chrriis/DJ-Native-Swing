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
public class WebBrowserCommandEvent extends WebBrowserEvent {

  private String command;
  private Object[] parameters;

  public WebBrowserCommandEvent(JWebBrowser webBrowser, String command, Object[] parameters) {
    super(webBrowser);
    this.command = command;
    this.parameters = parameters;
  }

  public String getCommand() {
    return command;
  }

  public Object[] getParameters() {
    return parameters;
  }

}
