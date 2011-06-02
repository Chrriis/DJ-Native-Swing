/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import chrriis.common.ObjectRegistry;
import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.common.WebServer.HTTPData;
import chrriis.common.WebServer.HTTPRequest;
import chrriis.common.WebServer.WebServerContent;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;

/**
 * A helper class to simplify the development of native components that leverage a web browser plugin (like the JFlashPlayer).
 * @author Christopher Deckers
 */
public abstract class WebBrowserObject {

  private final JWebBrowser webBrowser;
  private volatile int instanceID;

  public WebBrowserObject(JWebBrowser webBrowser) {
    this.webBrowser = webBrowser;
    webBrowser.getNativeComponent().setBackground(Color.WHITE);
    webBrowser.setDefaultPopupMenuRegistered(false);
    webBrowser.setBarsVisible(false);
    webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
      @Override
      public void commandReceived(WebBrowserCommandEvent e) {
        if("[Chrriis]WB_setLoaded".equals(e.getCommand())) {
          Object[] listeners = listenerList.getListenerList();
          for(int i=listeners.length-2; i>=0; i-=2) {
            if(listeners[i] == InitializationListener.class) {
              ((InitializationListener)listeners[i + 1]).objectInitialized();
            }
          }
        }
      }
    });
  }

  private volatile String resourcePath;

  public String getLoadedResource() {
    return "".equals(resourcePath)? null: resourcePath;
  }

  public boolean hasContent() {
    return resourcePath != null;
  }

  @Override
  protected void finalize() throws Throwable {
    ObjectRegistry.getInstance().remove(instanceID);
    super.finalize();
  }

  private volatile String backgroundColor;

  public void load(String resourcePath) {
    backgroundColor = getHexStringColor(webBrowser.getNativeComponent().getBackground());
    this.resourcePath = resourcePath;
    ObjectRegistry.getInstance().remove(instanceID);
    if(resourcePath == null) {
      if(!webBrowser.isNativePeerDisposed()) {
        webBrowser.setHTMLContent("");
      }
      return;
    }
    instanceID = ObjectRegistry.getInstance().add(this);
    String resourceLocation = WebServer.getDefaultWebServer().getDynamicContentURL(WebBrowserObject.class.getName(), String.valueOf(instanceID), "html");
    final AtomicBoolean result = new AtomicBoolean();
    InitializationListener initializationListener = new InitializationListener() {
      public void objectInitialized() {
        removeInitializationListener(this);
        result.set(true);
      }
    };
    addInitializationListener(initializationListener);
    webBrowser.navigate(resourceLocation);
    webBrowser.getNativeComponent().runSync(new LocalMessage() {
      @Override
      public Object run(Object[] args) {
        InitializationListener initializationListener = (InitializationListener)args[0];
        final AtomicBoolean result = (AtomicBoolean)args[1];
        EventDispatchUtils.sleepWithEventDispatch(new EventDispatchUtils.Condition() {
          public boolean getValue() {
            return result.get();
          }
        }, 4000);
        removeInitializationListener(initializationListener);
        return null;
      }
    }, initializationListener, result);
  }

  public String getLocalFileURL(File localFile) {
    try {
      return localFile.toURI().toURL().toExternalForm();
    } catch(Exception e) {
    }
    return WebServer.getDefaultWebServer().getResourcePathURL(localFile.getParent(), localFile.getName());
  }

  public static String getEmbeddedObjectJavascriptName() {
    return "myEmbeddedObject";
  }

  private static final String LS = Utils.LINE_SEPARATOR;

  protected static WebServerContent getWebServerContent(HTTPRequest httpRequest) {
    String resourcePath = httpRequest.getResourcePath();
    int index = resourcePath.indexOf('/');
    final int instanceID = Integer.parseInt(resourcePath.substring(0, index));
    final WebBrowserObject webBrowserObject = (WebBrowserObject)ObjectRegistry.getInstance().get(instanceID);
    if(webBrowserObject == null) {
      return null;
    }
    resourcePath = resourcePath.substring(index + 1);
    String type = resourcePath;
    if("html".equals(type)) {
      final WebBrowserObject component = (WebBrowserObject)ObjectRegistry.getInstance().get(instanceID);
      if(component == null) {
        return new WebServerContent() {
          @Override
          public InputStream getInputStream() {
            return getInputStream("<html><body></body></html>");
          }
        };
      }
      return new WebServerContent() {
        @Override
        public InputStream getInputStream() {
          String javascriptDefinitions = component.getJavascriptDefinitions();
          javascriptDefinitions = javascriptDefinitions == null? "": javascriptDefinitions + LS;
          String additionalHeadDefinitions = component.getAdditionalHeadDefinitions();
          additionalHeadDefinitions = additionalHeadDefinitions == null? "": additionalHeadDefinitions + LS;
          String content =
            "<html>" + LS +
            "  <head>" + LS +
            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>" + LS +
            "    <script language=\"JavaScript\" type=\"text/javascript\">" + LS +
            "      <!--" + LS +
            "      var sendCommand = " + JWebBrowser.COMMAND_FUNCTION + ";" + LS +
            "      function postCommand(command) {" + LS +
            "        var elements = new Array();" + LS +
            "        for(var i=1; i<arguments.length; i++) {" + LS +
            "          var element = document.createElement('input');" + LS +
            "          element.type='text';" + LS +
            "          element.name='j_arg' + (i-1);" + LS +
            "          element.value=arguments[i];" + LS +
            "          document.createElement('j_arg' + (i-1));" + LS +
            "          elements[i-1] = element;" + LS +
            "          document.j_form.appendChild(element);" + LS +
            "        }" + LS +
            "        document.j_form.j_command.value = command;" + LS +
            "        document.j_form.submit();" + LS +
            "        for(var i=0; i<elements.length; i++) {" + LS +
            "          document.j_form.removeChild(elements[i]);" + LS +
            "        }" + LS +
            "      }" + LS +
            "      function getEmbeddedObject() {" + LS +
            "        var movieName = \"" + getEmbeddedObjectJavascriptName() + "\";" + LS +
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
            javascriptDefinitions +
            "      //-->" + LS +
            "    </script>" + LS +
            "    <style type=\"text/css\">" + LS +
            "      html { background-color: " + webBrowserObject.backgroundColor + "; }" + LS +
            "      html, object, embed, div, body, table { width: 100%; height: 100%; min-height: 100%; margin: 0; padding: 0; overflow: hidden; text-align: center; }" + LS +
            "      object, embed, div { position: absolute; left:0; top:0;}" + LS +
            "      td { vertical-align: middle; }" + LS +
            "    </style>" + LS +
            additionalHeadDefinitions +
            "  </head>" + LS +
            "  <body height=\"*\">" + LS +
            "    <iframe style=\"display:none;\" name=\"j_iframe\"></iframe>" + LS +
            "    <form style=\"display:none;\" name=\"j_form\" action=\"" + WebServer.getDefaultWebServer().getDynamicContentURL(WebBrowserObject.class.getName(), "postCommand/" + instanceID) + "\" method=\"POST\" target=\"j_iframe\">" + LS +
            "      <input name=\"j_command\" type=\"text\"></input>" + LS +
            "    </form>" + LS +
            "    <script src=\"" + WebServer.getDefaultWebServer().getDynamicContentURL(WebBrowserObject.class.getName(), String.valueOf(instanceID), "js") + "\"></script>" + LS +
            "  </body>" + LS +
            "</html>" + LS;
          return getInputStream(content);
        }
      };
    }
    if("js".equals(type)) {
      String url = webBrowserObject.resourcePath;
      // local files may have some security restrictions depending on the plugin, so let's ask the plugin for a valid URL.
      File file = Utils.getLocalFile(url);
      if(file != null) {
        url = webBrowserObject.getLocalFileURL(file);
      }
      final String escapedURL = Utils.escapeXML(url);
      final String encodedURL = Utils.encodeURL(url);
      return new WebServerContent() {
        @Override
        public String getContentType() {
          return getDefaultMimeType(".js");
        }
        @Override
        public InputStream getInputStream() {
          ObjectHTMLConfiguration objectHtmlConfiguration = webBrowserObject.getObjectHtmlConfiguration();
          StringBuilder objectParameters = new StringBuilder();
          StringBuilder embedParameters = new StringBuilder();
          Map<String, String> parameters = objectHtmlConfiguration.getHTMLParameters();
          HashMap<String, String> htmlParameters = parameters == null? new HashMap<String, String>(): new HashMap<String, String>(parameters);
          String windowsParamName = objectHtmlConfiguration.getWindowsParamName();
          String paramName = objectHtmlConfiguration.getParamName();
          htmlParameters.remove("width");
          htmlParameters.remove("height");
          htmlParameters.remove("type");
          htmlParameters.remove("name");
          if(windowsParamName != null) {
            htmlParameters.remove(windowsParamName);
          }
          if(paramName != null) {
            htmlParameters.remove(paramName);
          }
          for(Entry<String, String> param: htmlParameters.entrySet()) {
            String name = Utils.escapeXML(param.getKey());
            String value = Utils.escapeXML(param.getValue());
            embedParameters.append(' ').append(name).append("=\"").append(value).append("\"");
            objectParameters.append("window.document.write('  <param name=\"").append(name).append("\" value=\"").append(value).append("\"/>');" + LS);
          }
          String version = objectHtmlConfiguration.getVersion();
          String versionParameter = version != null? " version=\"" + version + "\"": "";
          String embeddedObjectJavascriptName = getEmbeddedObjectJavascriptName();
          String content =
            "<!--" + LS +
            "window.document.write('<object classid=\"clsid:" + objectHtmlConfiguration.getWindowsClassID() + "\" id=\"" + embeddedObjectJavascriptName + "\" codebase=\"" + objectHtmlConfiguration.getWindowsInstallationURL() + "\" events=\"true\">');" + LS +
            (windowsParamName == null? "": "window.document.write('  <param name=\"" + windowsParamName + "\" value=\"' + decodeURIComponent('" + encodedURL + "') + '\"/>');" + LS) +
            objectParameters +
            "window.document.write('  <embed" + embedParameters + " name=\"" + embeddedObjectJavascriptName + "\"" + (paramName == null? "": " " + paramName + "=\"" + escapedURL + "\"") + " type=\"" + objectHtmlConfiguration.getMimeType() + "\" pluginspage=\"" + objectHtmlConfiguration.getInstallationURL() + "\"" + versionParameter+ ">');" + LS +
            "window.document.write('  </embed>');" + LS +
            "window.document.write('</object>');" + LS +
            "var embeddedObject = getEmbeddedObject();" + LS +
            "embeddedObject.style.width = '100%';" + LS +
            "embeddedObject.style.height = '100%';" + LS +
            "sendCommand('[Chrriis]WB_setLoaded');" + LS +
            "window.document.attachEvent(\"onkeydown\", function() {" + LS +
            "  switch (event.keyCode) {" + LS +
                 // F5
            "    case 116 :" + LS +
            "      event.returnValue = false;" + LS +
            "      event.keyCode = 0;" + LS +
            "      break;" + LS +
            "  }" + LS +
            "});" + LS +
            "//-->" + LS;
          return getInputStream(content);
        }
      };
    }
    if("postCommand".equals(type)) {
      HTTPData postData = httpRequest.getHTTPPostDataArray()[0];
      Map<String, String> headerMap = postData.getHeaderMap();
      int size = headerMap.size();
      final String command = headerMap.get("j_command");
      final String[] arguments = new String[size - 1];
      for(int i=0; i<arguments.length; i++) {
        arguments[i] = headerMap.get("j_arg" + i);
      }
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          WebBrowserListener[] webBrowserListeners = webBrowserObject.webBrowser.getWebBrowserListeners();
          WebBrowserCommandEvent e = null;
          for(int i=webBrowserListeners.length-1; i>= 0; i--) {
            if(e == null) {
              e = new WebBrowserCommandEvent(webBrowserObject.webBrowser, command, arguments);
            }
            webBrowserListeners[i].commandReceived(e);
          }
        }
      });
      return new WebServerContent() {
        @Override
        public InputStream getInputStream() {
          String content =
            "<html>" + LS +
            "  <body>" + LS +
            "    Command sent successfully." + LS +
            "  </body>" + LS +
            "</html>" + LS;
          return getInputStream(content);
        }
      };
    }
    final String resource = resourcePath;
    return new WebServerContent() {
      @Override
      public InputStream getInputStream() {
        try {
          String url = webBrowserObject.resourcePath;
          // local files may have some security restrictions depending on the plugin, so let's ask the plugin for a valid URL.
          File file = Utils.getLocalFile(url);
          if(file != null) {
            url = webBrowserObject.getLocalFileURL(file);
          }
          url = url.substring(0, url.lastIndexOf('/')) + "/" + resource;
          return new URL(url).openStream();
        } catch (Exception e) {
        }
        return null;
      }
    };
  }

  public static class ObjectHTMLConfiguration {

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

    private String version;

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
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

  protected String getJavascriptDefinitions() {
    return null;
  }

  protected String getAdditionalHeadDefinitions() {
    return null;
  }

  private static interface InitializationListener extends EventListener {
    public void objectInitialized();
  }

  private EventListenerList listenerList = new EventListenerList();

  private void addInitializationListener(InitializationListener listener) {
    listenerList.add(InitializationListener.class, listener);
  }

  private void removeInitializationListener(InitializationListener listener) {
    listenerList.remove(InitializationListener.class, listener);
  }

//  private InitializationListener[] getInitializationListeners() {
//    return listenerList.getListeners(InitializationListener.class);
//  }

  /**
   * Set the value of a property of the object (a String, number, boolean, or array).
   */
  public void setObjectProperty(String property, Object value) {
    webBrowser.executeJavascript("try {getEmbeddedObject()." + property + " = " + JWebBrowser.convertJavaObjectToJavascript(value) + ";} catch(exxxxx) {}");
  }

  /**
   * @return The value, potentially a String, Number, boolean.
   */
  public Object getObjectProperty(String property) {
    return webBrowser.executeJavascriptWithResult("return getEmbeddedObject()." + property);
  }

  /**
   * Invoke a function on the object, with optional arguments (Strings, numbers, booleans, or array).
   */
  public void invokeObjectFunction(String functionName, Object... args) {
    webBrowser.executeJavascript("try {getEmbeddedObject()." + JWebBrowser.createJavascriptFunctionCall(functionName, args) + ";} catch(exxxxx) {}");
  }

  /**
   * Invoke a function on the object and waits for a result, with optional arguments (Strings, numbers, booleans, or array).
   * @return The value, potentially a String, Number, boolean.
   */
  public Object invokeObjectFunctionWithResult(String functionName, Object... args) {
    return webBrowser.executeJavascriptWithResult("return getEmbeddedObject()." + JWebBrowser.createJavascriptFunctionCall(functionName, args));
  }

  /**
   * Set the background color of the object, which by default is the color of the native component.
   * @param background The background to set.
   */
  public void setBackground(Color background) {
    backgroundColor = getHexStringColor(background);
    webBrowser.executeJavascript("document.bgColor = '" + backgroundColor + "';");
  }

  private static String getHexStringColor(Color background) {
    String backgroundColor = Integer.toHexString(background.getRGB() & 0xFFFFFF).toUpperCase(Locale.ENGLISH);
    backgroundColor = '#' + "000000".substring(backgroundColor.length()) + backgroundColor;
    return backgroundColor;
  }

}
