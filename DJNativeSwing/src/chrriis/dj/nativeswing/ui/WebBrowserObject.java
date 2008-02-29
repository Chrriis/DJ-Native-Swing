/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import chrriis.common.Registry;
import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.common.WebServer.HTTPRequest;
import chrriis.common.WebServer.WebServerContent;
import chrriis.dj.nativeswing.Disposable;

/**
 * A helper class to simplify the development of native components that act as a plugin to the web browser component (like the JFlashPlayer).
 * @author Christopher Deckers
 */
public abstract class WebBrowserObject implements Disposable {

  private JWebBrowser webBrowser;
  private int instanceID;
  
  public WebBrowserObject(JWebBrowser webBrowser) {
    this.webBrowser = webBrowser;
    webBrowser.setBarsVisible(false);
  }
  
  private String url;

  public String getURL() {
    return url;
  }
  
  public boolean hasContent() {
    return url != null;
  }
  
  @SuppressWarnings("deprecation")
  public void setURL(String url) {
    this.url = url;
    Registry.getInstance().remove(instanceID);
    if(url == null) {
      webBrowser.setText("");
      return;
    }
    instanceID = Registry.getInstance().add(this);
    url = WebServer.getDefaultWebServer().getDynamicContentURL(WebBrowserObject.class.getName(), "html/" + instanceID);
    webBrowser.setURL(url);
  }
  
  protected String getLocalFileURL(File localFile) {
    try {
      return localFile.toURI().toURL().toExternalForm();
    } catch(Exception e) {
    }
    return WebServer.getDefaultWebServer().getResourcePathURL(localFile.getParent(), localFile.getName());
  }

  protected static final String LS = System.getProperty("line.separator");

  protected static WebServerContent getWebServerContent(HTTPRequest httpRequest) {
    String resourcePath = httpRequest.getResourcePath();
    int index = resourcePath.indexOf('/');
    String type = resourcePath.substring(0, index);
    resourcePath = resourcePath.substring(index + 1);
    if("html".equals(type)) {
      final int instanceID = Integer.parseInt(resourcePath);
      final WebBrowserObject component = (WebBrowserObject)Registry.getInstance().get(instanceID);
      if(component == null) {
        return null;
      }
      return new WebServerContent() {
        @Override
        public InputStream getInputStream() {
          String content =
            "<html>" + LS +
            "  <head>" + LS +
            "    <script language=\"JavaScript\" type=\"text/javascript\">" + LS +
            "      <!--" + LS +
            "      function sendCommand(command) {" + LS +
            "        command = command == null? '': encodeURIComponent(command);" + LS +
            "        window.location = 'command://' + command;" + LS +
            "      }" + LS +
            "      function getEmbeddedObject() {" + LS +
            "        var movieName = \"myEmbeddedObject\";" + LS +
            "        if(window.document[movieName]) {" + LS +
            "          return window.document[movieName];" + LS +
            "        }" + LS +
            "        if(navigator.appName.indexOf(\"Microsoft Internet\") == -1) {" + LS +
            "          if(document.embeds && document.embeds[movieName]) {" + LS +
            "            return document.embeds[movieName];" + LS +
            "          }" + LS +
            "        } else {" + LS +
            "          return document.getElementById(movieName);" + LS +
            "        }" + LS +
            "      }" + LS +
            component.getJavascriptDefinitions() + LS +
            "      //-->" + LS +
            "    </script>" + LS +
            "    <style type=\"text/css\">" + LS +
            "      html, object, embed, div, body, table { width: 100%; height: 100%; min-height: 100%; margin: 0; padding: 0; overflow: hidden; background-color: #FFFFFF; text-align: center; }" + LS +
            "      object, embed, div { position: absolute; left:0; top:0;}" + LS +
            "      td { vertical-align: middle; }" + LS +
            "    </style>" + LS +
            "  </head>" + LS +
            "  <body height=\"*\">" + LS +
            "    <script src=\"" + WebServer.getDefaultWebServer().getDynamicContentURL(WebBrowserObject.class.getName(), "js/" + instanceID) + "\"></script>" + LS +
            "  </body>" + LS +
            "</html>" + LS;
          return getInputStream(content);
        }
      };
    }
    if("js".equals(type)) {
      final int instanceID = Integer.parseInt(resourcePath);
      final WebBrowserObject webBrowserObject = (WebBrowserObject)Registry.getInstance().get(instanceID);
      if(webBrowserObject == null) {
        return null;
      }
      String url = webBrowserObject.url;
      // local files may have some security restrictions, so let's use our proxy.
      File file = Utils.getLocalFile(url);
      if(file != null) {
        url = webBrowserObject.getLocalFileURL(file);
      }
      final String escapedURL = Utils.escapeXML(url);
      return new WebServerContent() {
        @Override
        public String getContentType() {
          return getDefaultMimeType(".js");
        }
        public InputStream getInputStream() {
          ObjectHTMLConfiguration objectHtmlConfiguration = webBrowserObject.getObjectHtmlConfiguration();
          StringBuffer objectParameters = new StringBuffer();
          StringBuffer embedParameters = new StringBuffer();
          HashMap<String, String> htmlParameters = new HashMap<String, String>(objectHtmlConfiguration.getHTMLParameters());
          htmlParameters.remove("width");
          htmlParameters.remove("height");
          htmlParameters.remove("type");
          htmlParameters.remove("name");
          htmlParameters.remove(objectHtmlConfiguration.getWindowsParamName());
          htmlParameters.remove(objectHtmlConfiguration.getParamName());
          for(Entry<String, String> param: htmlParameters.entrySet()) {
            String name = Utils.escapeXML(param.getKey());
            String value = Utils.escapeXML(param.getValue());
            embedParameters.append(' ').append(name).append("=\"").append(value).append("\"");
            objectParameters.append("window.document.write('  <param name=\"").append(name).append("\" value=\"").append(value).append("\"/>');" + LS);
          }
          String content =
            "<!--" + LS +
            "window.document.write('<object classid=\"clsid:" + objectHtmlConfiguration.getWindowsClassID() + "\" id=\"myEmbeddedObject\" codebase=\"" + objectHtmlConfiguration.getWindowsInstallationURL() + "\" events=\"true\">');" + LS +
            "window.document.write('  <param name=\"" + objectHtmlConfiguration.getWindowsParamName() + "\" value=\"' + decodeURIComponent('" + escapedURL + "') + '\";\"/>');" + LS +
            objectParameters +
            "window.document.write('  <embed" + embedParameters + " name=\"myEmbeddedObject\" " + objectHtmlConfiguration.getParamName() + "=\"" + escapedURL + "\" type=\"" + objectHtmlConfiguration.getMimeType() + "\" pluginspage=\"" + objectHtmlConfiguration.getInstallationURL() + "\">');" + LS +
            "window.document.write('  </embed>');" + LS +
            "window.document.write('</object>');" + LS +
            "window.document.write('<div></div>');" + LS +
            "window.document.write('<div id=\"messageDiv\" style=\"display:none;\"><table><tr><td>" + objectHtmlConfiguration.getHTMLLoadingMessage() + "</td></tr></table></div>');" + LS +
            "setTimeout('document.getElementById(\\'messageDiv\\').style.display = \\'inline\\'', 2000);" + LS +
            "var embeddedObject = getEmbeddedObject();" + LS +
            "embeddedObject.style.width = '100%';" + LS +
            "embeddedObject.style.height = '100%';" + LS +
            "//-->" + LS;
          return getInputStream(content);
        }
      };
    }
    return null;
  }
  
  protected static class ObjectHTMLConfiguration {
    
    private String htmlLoadingMessage;
    
    public void setHTMLLoadingMessage(String htmlLoadingMessage) {
      this.htmlLoadingMessage = htmlLoadingMessage;
    }
    
    public String getHTMLLoadingMessage() {
      return htmlLoadingMessage;
    }
    
    private String windowsClassID;
    
    public void setWindowsClassID(String windowsClassID) {
      this.windowsClassID = windowsClassID;
    }
    
    public String getWindowsClassID() {
      return windowsClassID;
    }
    
    private String windowsInstallationURL;
    
    public String getWindowsInstallationURL() {
      return windowsInstallationURL;
    }
    
    public void setWindowsInstallationURL(String windowsInstallationURL) {
      this.windowsInstallationURL = windowsInstallationURL;
    }
    
    private String installationURL;
    
    public String getInstallationURL() {
      return installationURL;
    }
    
    public void setInstallationURL(String installationURL) {
      this.installationURL = installationURL;
    }
    
    private String windowsParamName;
    
    public String getWindowsParamName() {
      return windowsParamName;
    }
    
    public void setWindowsParamName(String windowsParamName) {
      this.windowsParamName = windowsParamName;
    }
    
    private String paramName;
    
    public String getParamName() {
      return paramName;
    }
    
    public void setParamName(String paramName) {
      this.paramName = paramName;
    }
    
    private Map<String, String> htmlParameters;
    
    public Map<String, String> getHTMLParameters() {
      return htmlParameters;
    }
    
    public void setHTMLParameters(Map<String, String> htmlParameters) {
      this.htmlParameters = htmlParameters;
    }
    
    private String mimeType;
    
    public String getMimeType() {
      return mimeType;
    }
    
    public void setMimeType(String mimeType) {
      this.mimeType = mimeType;
    }
    
  }
  
  protected abstract ObjectHTMLConfiguration getObjectHtmlConfiguration();
 
  protected abstract String getJavascriptDefinitions();
  
  public void dispose() {
    webBrowser.dispose();
  }
  
  public boolean isDisposed() {
    return webBrowser.isDisposed();
  }

}
