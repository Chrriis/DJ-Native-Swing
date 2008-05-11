/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.EventListener;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import chrriis.common.ObjectRegistry;
import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.common.WebServer.HTTPRequest;
import chrriis.common.WebServer.WebServerContent;
import chrriis.dj.nativeswing.LocalMessage;
import chrriis.dj.nativeswing.Message;
import chrriis.dj.nativeswing.NSOption;
import chrriis.dj.nativeswing.NSPanelComponent;
import chrriis.dj.nativeswing.NativeComponent;

/**
 * An HTML editor. It is a browser-based component, which relies on the FCKeditor.<br/>
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fails, the methods will not have any effect. The results from methods have relevant values only when the component is valid. 
 * @author Christopher Deckers
 */
public class JHTMLEditor extends NSPanelComponent {

  private static final String SET_CUSTOM_JAVASCRIPT_CONFIGURATION_OPTION_KEY = "HTML Editor Custom Configuration Script";
  
  /**
   * Create an option to set custom configuration for the FCKeditor. The list of possible options to set can be found here: <a href="http://docs.fckeditor.net/FCKeditor_2.x/Developers_Guide/Configuration/Configuration_Options">http://docs.fckeditor.net/FCKeditor_2.x/Developers_Guide/Configuration/Configuration_Options</a>.
   * @return the option to set a custom configuration.
   */
  public static NSOption setCustomJavascriptConfiguration(final String javascript) {
    return new NSOption(SET_CUSTOM_JAVASCRIPT_CONFIGURATION_OPTION_KEY) {
      @Override
      public Object getOptionValue() {
        return javascript;
      }
    };
  }
  
  private static final String FCK_INSTANCE = "FCKeditor1";
  
  private JWebBrowser webBrowser;
  private int instanceID;

  private static final String LS = Utils.LINE_SEPARATOR;

  private static class NWebBrowserListener extends WebBrowserAdapter {
    protected Reference<JHTMLEditor> htmlEditor;
    protected NWebBrowserListener(JHTMLEditor htmlEditor) {
      this.htmlEditor = new WeakReference<JHTMLEditor>(htmlEditor);
    }
    @Override
    public void commandReceived(WebBrowserEvent e, String command, String[] args) {
      JHTMLEditor htmlEditor = this.htmlEditor.get();
      if(htmlEditor == null) {
        return;
      }
      if("JH_setLoaded".equals(command)) {
        Object[] listeners = htmlEditor.listenerList.getListenerList();
        for(int i=listeners.length-2; i>=0; i-=2) {
          if(listeners[i] == InitializationListener.class) {
            ((InitializationListener)listeners[i + 1]).objectInitialized();
          }
        }
      }
    }
  }
  
  private class CMLocal_waitForInitialization extends LocalMessage {
    @Override
    public Object run(Object[] args) {
      InitializationListener initializationListener = (InitializationListener)args[0];
      Boolean[] resultArray = (Boolean[])args[1];
      for(long time = System.currentTimeMillis(); !resultArray[0].booleanValue() && System.currentTimeMillis() - time < 4000; ) {
        new Message().syncSend();
        try {
          Thread.sleep(50);
        } catch(Exception e) {}
      }
      removeInitializationListener(initializationListener);
      return null;
    }
  }
  
  private String customJavascriptConfiguration;
  
  /**
   * Construct an HTML editor.
   * @param options the options to configure the behavior of this component.
   */
  public JHTMLEditor(NSOption... options) {
    if(getClass().getResource("/fckeditor/fckeditor.js") == null) {
      throw new IllegalStateException("The FCKEditor distribution is missing from the classpath!");
    }
    Map<Object, Object> optionMap = NSOption.createOptionMap(options);
    customJavascriptConfiguration = (String)optionMap.get(SET_CUSTOM_JAVASCRIPT_CONFIGURATION_OPTION_KEY);
    webBrowser = new JWebBrowser(options);
    initialize(webBrowser.getNativeComponent());
    webBrowser.addWebBrowserListener(new NWebBrowserListener(this));
    webBrowser.setBarsVisible(false);
    add(webBrowser, BorderLayout.CENTER);
    instanceID = ObjectRegistry.getInstance().add(this);
    final Boolean[] resultArray = new Boolean[] {Boolean.FALSE};
    InitializationListener initializationListener = new InitializationListener() {
      public void objectInitialized() {
        removeInitializationListener(this);
        resultArray[0] = Boolean.TRUE;
      }
    };
    addInitializationListener(initializationListener);
    webBrowser.navigate(WebServer.getDefaultWebServer().getDynamicContentURL(JHTMLEditor.class.getName(), String.valueOf(instanceID),  "index.html"));
    webBrowser.getNativeComponent().runSync(new CMLocal_waitForInitialization(), initializationListener, resultArray);
  }
  
  /**
   * Get the web browser that contains this component. The web browser should only be used to add listeners, for example to listen to window creation events.
   * @return the web browser.
   */
  public JWebBrowser getWebBrowser() {
    return webBrowser;
  }
  
  protected static WebServerContent getWebServerContent(final HTTPRequest httpRequest) {
    String resourcePath = httpRequest.getResourcePath();
    int index = resourcePath.indexOf('/');
    final int instanceID = Integer.parseInt(resourcePath.substring(0, index));
    final String resourcePath_ = resourcePath.substring(index + 1);
    final JHTMLEditor htmlEditor = (JHTMLEditor)ObjectRegistry.getInstance().get(instanceID);
    if(htmlEditor == null) {
      return null;
    }
    if("index.html".equals(resourcePath_)) {
      return new WebServerContent() {
        @Override
        public String getContentType() {
          int index = resourcePath_.lastIndexOf('.');
          return getDefaultMimeType(index == -1? null: resourcePath_.substring(index));
        }
        @Override
        public InputStream getInputStream() {
          String content =
            "<html>" + LS +
            "  <head>" + LS +
            "    <style type=\"text/css\">" + LS +
            "      body, form {margin: 0; padding: 0; overflow: auto;}" + LS +
            "    </style>" + LS +
            "    <script type=\"text/javascript\" src=\"fckeditor.js\"></script>" + LS +
            "    <script type=\"text/javascript\">" + LS +
            // We override the FCK editor function because on Linux this may return false if navigator.product is empty (instead of "Gecko")
            "      function FCKeditor_IsCompatibleBrowser() {" + LS +
            "        return true;" + LS +
            "      }" + LS +
            "      function sendCommand(command) {" + LS +
            "        var s = 'command://' + encodeURIComponent(command);" + LS +
            "        for(var i=1; i<arguments.length; s+='&'+encodeURIComponent(arguments[i++]));" + LS +
            "        window.location = s;" + LS +
            "      }" + LS +
            "      function JH_setData(html) {" + LS +
            "        var inst = FCKeditorAPI.GetInstance('" + FCK_INSTANCE + "');" + LS +
            "        inst.SetHTML(decodeURIComponent(html));" + LS +
            "      }" + LS +
            "      function JH_sendData() {" + LS +
            "        document.jhtml_form.action = 'jhtml_sendData';" + LS +
            "        document.jhtml_form.submit();" + LS +
            "        return false;" + LS +
            "      }" + LS +
            "      function JH_doSave() {" + LS +
            "        document.jhtml_form.action = 'jhtml_save';" + LS +
            "        document.jhtml_form.submit();" + LS +
            "        return false;" + LS +
            "      }" + LS +
            "      function createEditor() {" + LS +
            "        var oFCKeditor = new FCKeditor('" + FCK_INSTANCE + "');" + LS +
            "        oFCKeditor.Width = \"100%\";" + LS +
            "        oFCKeditor.Height = \"100%\";" + LS +
            "        oFCKeditor.BasePath = \"\";" + LS +
            (htmlEditor.customJavascriptConfiguration != null? "        oFCKeditor.Config[\"CustomConfigurationsPath\"] = '" + WebServer.getDefaultWebServer().getDynamicContentURL(JHTMLEditor.class.getName(), String.valueOf(instanceID), "customConfigurationScript.js") + "';" + LS: "") +
            "        oFCKeditor.Create();" + LS +
            "      }" + LS +
            "      function FCKeditor_OnComplete(editorInstance) {" + LS +
            "        editorInstance.LinkedField.form.onsubmit = JH_doSave;" + LS +
            "        sendCommand('JH_setLoaded');" + LS +
            "      }" + LS +
            "    </script>" + LS +
            "  </head>" + LS +
            "  <body>" + LS +
            "  <iframe style=\"display:none;\" name=\"j_iframe\"></iframe>" + LS +
            "  <form name=\"jhtml_form\" method=\"POST\" target=\"j_iframe\">" + LS +
            "    <script type=\"text/javascript\">" + LS +
            "      createEditor();" + LS +
            "    </script>" +
            "</form>" + LS + // No space at the begining of this line or else a scrollbar appears.
            "  </body>" + LS +
            "</html>" + LS;
          return getInputStream(content);
        }
      };
    }
    if("customConfigurationScript.js".equals(resourcePath_)) {
      return new WebServerContent () {
        @Override
        public String getContentType () {
          return getDefaultMimeType(".js");
        }
        public InputStream getInputStream () {
          return getInputStream(htmlEditor.customJavascriptConfiguration);
        }
      };
    }
    if("jhtml_save".equals(resourcePath_)) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          String html = convertLinksToLocal(httpRequest.getHTTPPostDataArray()[0].getHeaderMap().get(FCK_INSTANCE));
          Object[] listeners = htmlEditor.listenerList.getListenerList();
          HTMLEditorSaveEvent e = null;
          for(int i=listeners.length-2; i>=0; i-=2) {
            if(listeners[i] == HTMLEditorListener.class) {
              if(e == null) {
                e = new HTMLEditorSaveEvent(htmlEditor, html);
              }
              ((HTMLEditorListener)listeners[i + 1]).saveHTML(e);
            }
          }
        }
      });
      return new WebServerContent() {
        @Override
        public InputStream getInputStream() {
          String content =
            "<html>" + LS +
            "  <body>" + LS +
            "    Save successful." + LS +
            "  </body>" + LS +
            "</html>" + LS;
          return getInputStream(content);
        }
      };
    }
    if("jhtml_sendData".equals(resourcePath_)) {
      String data = httpRequest.getHTTPPostDataArray()[0].getHeaderMap().get(FCK_INSTANCE);
      htmlEditor.tempResult = data;
      return new WebServerContent() {
        @Override
        public InputStream getInputStream() {
          String content =
            "<html>" + LS +
            "  <body>" + LS +
            "    Send data successful." + LS +
            "  </body>" + LS +
            "</html>" + LS;
          return getInputStream(content);
        }
      };
    }
    if("editor/filemanager/connectors/php/upload.php".equals(resourcePath_)) {
      return new WebServerContent() {
        @Override
        public InputStream getInputStream() {
          String content =
            "<html>" + LS +
            "  <head>" + LS +
            "    <script type=\"text/javascript\">" + LS +
            "      alert('upload to local system are not allowed...');" + LS +
            "    </script>" + LS +
            "  </head>" + LS +
            "  <body>" + LS +
            "    Upload successful." + LS +
            "  </body>" + LS +
            "</html>" + LS;
          return getInputStream(content);
        }
      };
    }
    if("editor/filemanager/connectors/php/connector.php".equals(resourcePath_)) {
      Map<String, String> queryParameterMap = httpRequest.getQueryParameterMap();
      String command = queryParameterMap.get("Command");
      String content = null;
      if("GetFoldersAndFiles".equals(command) || "GetFolders".equals(command)) {
        final String type = queryParameterMap.get("Type");
        String currentDir = queryParameterMap.get("CurrentFolder");
        File[] roots = File.listRoots();
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        sb.append("<Connector command=\"").append(command).append("\" resourceType=\"").append(type).append("\">");
        sb.append("<CurrentFolder path=\"").append(currentDir).append("\" url=\"").append(WebServer.getDefaultWebServer().getResourcePathURL(currentDir, "")).append("\" />");
//        try {
//          sb.append("<CurrentFolder path=\"").append(currentDir).append("\" url=\"").append(new File(currentDir).toURI().toURL()).append("\" />");
//        } catch (MalformedURLException e) {
//        }
        if(("GetFoldersAndFiles".equals(command) || "GetFolders".equals(command)) && currentDir.equals("/") && roots.length > 1) {
          sb.append("<Folders>");
          for(File file: roots) {
            sb.append("<Folder name=\"").append(Utils.escapeXML(file.getAbsolutePath())).append("\"/>");
          }
          sb.append("</Folders>");
        } else {
          if("GetFoldersAndFiles".equals(command) || "GetFolders".equals(command)) {
            sb.append("<Folders>");
            for(File file: new File(currentDir).listFiles(new FileFilter() {
              public boolean accept(File pathname) {
                return !pathname.isFile();
              }
            })) {
              sb.append("<Folder name=\"").append(Utils.escapeXML(file.getName())).append("\"/>");
            }
            sb.append("</Folders>");
          }
          if("GetFoldersAndFiles".equals(command)) {
            sb.append("<Files>");
            for(File file: new File(currentDir).listFiles(new FileFilter() {
              public boolean accept(File pathname) {
                if(!pathname.isFile()) {
                  return false;
                }
                if("Image".equals(type)) {
                  String name = pathname.getName().toLowerCase();
                  return
                  name.endsWith(".bmp") ||
                  name.endsWith(".jpg") ||
                  name.endsWith(".gif") ||
                  name.endsWith(".png");
                }
                if("Flash".equals(type)) {
                  String name = pathname.getName().toLowerCase();
                  return name.endsWith(".swf");
                }
                return true;
              }
            })) {
              sb.append("<File name=\"").append(Utils.escapeXML(file.getName())).append("\" size=\"").append(file.length() / 1000).append("\"/>");
            }
            sb.append("</Files>");
          }
        }
        sb.append("</Connector>");
        content = sb.toString();
      }
      final String content_ = content;
      return new WebServerContent() {
        @Override
        public String getContentType() {
          return "text/xml; charset=utf-8";
        }
        @Override
        public InputStream getInputStream() {
          return getInputStream(content_);
        }
      };
    }
    return WebServer.getDefaultWebServer().getURLContent(WebServer.getDefaultWebServer().getClassPathResourceURL(JHTMLEditor.class.getName(), "/fckeditor/" + resourcePath_));
  }
  
  private Object tempResult;
  
  /**
   * Get the HTML content.
   * @return the HTML content.
   */
  public String getHTMLContent() {
    if(!webBrowser.isNativePeerInitialized()) {
      return "";
    }
    tempResult = this;
    webBrowser.executeJavascript("JH_sendData();");
    String html = null;
    for(int i=0; i<20; i++) {
      if(tempResult != this) {
        html = (String)tempResult;
        break;
      }
      new Message().syncSend();
      if(tempResult != this) {
        html = (String)tempResult;
        break;
      }
      try {
        Thread.sleep(50);
      } catch(Exception e) {}
    }
    return convertLinksToLocal(html);
  }
  
  private static String convertLinksToLocal(String html) {
    if(html == null) {
      return html;
    }
    // Transform proxied URLs to "file:///".
    Pattern p = Pattern.compile("=\\s*\"(" + WebServer.getDefaultWebServer().getURLPrefix() + "/resource/)([^/]+)/([^\"]+)\"\\s");
    for(Matcher m; (m = p.matcher(html)).find(); ) {
      String codeBase = html.substring(m.start(2), m.end(2));
      String resource = html.substring(m.start(3), m.end(3));
      try {
        resource = new File(Utils.decodeURL(Utils.decodeURL(codeBase)), resource).toURI().toURL().toExternalForm();
      } catch (MalformedURLException e) {
      }
      html = html.substring(0, m.start(1)) + resource + html.substring(m.end(3));
    }
    return html;
  }

  private static String convertLinksFromLocal(String html) {
    if(html == null) {
      return html;
    }
    // Transform "file:///" to proxied URLs.
    Pattern p = Pattern.compile("=\\s*\"(file:/{1,3})([^\"]+)\"\\s");
    for(Matcher m; (m = p.matcher(html)).find(); ) {
      String resource = html.substring(m.start(2), m.end(2));
      File resourceFile = new File(resource);
      resource = WebServer.getDefaultWebServer().getResourcePathURL(Utils.encodeURL(resourceFile.getParent()), resourceFile.getName());
      html = html.substring(0, m.start(1)) + resource + html.substring(m.end(2));
    }
    return html;
  }
  
  /**
   * Set the HTML content.
   * @param html the HTML content.
   */
  public void setHTMLContent(String html) {
    html = convertLinksFromLocal(html.replaceAll("[\r\n]", ""));
//    webBrowser.executeJavascript("JH_setData('" + Utils.encodeURL(html) + "');");
    // There is a problem: IE crashes when it has the focus and is flooded with this message.
    // While waiting for an SWT fix, we use the workaround to disable the component which loses the focus.
    NativeComponent nativeComponent = webBrowser.getNativeComponent();
    boolean isEnabled = nativeComponent.isEnabled();
    nativeComponent.setEnabled(false);
    new Message().syncSend();
    webBrowser.executeJavascript("JH_setData('" + Utils.encodeURL(html) + "');");
    new Message().syncSend();
    nativeComponent.setEnabled(isEnabled);
  }
  
  /**
   * Add an HTML editor listener.
   * @param listener The HTML editor listener to add.
   */
  public void addHTMLEditorListener(HTMLEditorListener listener) {
    listenerList.add(HTMLEditorListener.class, listener);
  }
  
  /**
   * Remove an HTML editor listener.
   * @param listener the HTML editor listener to remove.
   */
  public void removeHTMLEditorListener(HTMLEditorListener listener) {
    listenerList.remove(HTMLEditorListener.class, listener);
  }
  
  /**
   * Get the HTML editor listeners.
   * @return the HTML editor listeners.
   */
  public HTMLEditorListener[] getHTMLEditorListeners() {
    return listenerList.getListeners(HTMLEditorListener.class);
  }
  
  private static interface InitializationListener extends EventListener {
    public void objectInitialized();
  }

  private void addInitializationListener(InitializationListener listener) {
    listenerList.add(InitializationListener.class, listener);
  }
  
  private void removeInitializationListener(InitializationListener listener) {
    listenerList.remove(InitializationListener.class, listener);
  }
  
//  private InitializationListener[] getInitializationListeners() {
//    return listenerList.getListeners(InitializationListener.class);
//  }

}
