/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.Map;

import javax.swing.SwingUtilities;

import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.common.WebServer.HTTPRequest;
import chrriis.common.WebServer.WebServerContent;
import chrriis.dj.nativeswing.swtimpl.EventDispatchUtils;
import chrriis.dj.nativeswing.swtimpl.Message;
import chrriis.dj.nativeswing.swtimpl.NSSystemPropertySWT;
import chrriis.dj.nativeswing.swtimpl.NativeComponent;
import chrriis.dj.nativeswing.swtimpl.components.JHTMLEditor.JHTMLEditorImplementation;

/**
 * @author Christopher Deckers
 * @author Simon Pope
 */
class JHTMLEditorCKeditor implements JHTMLEditorImplementation {

  private static final String PACKAGE_PREFIX = "/ckeditor/";
  private static final String EDITOR_INSTANCE = "HTMLeditor1";

  private final JHTMLEditor htmlEditor;
  private final String customOptions;

  @SuppressWarnings("unchecked")
  public JHTMLEditorCKeditor(JHTMLEditor htmlEditor, Map<Object, Object> optionMap) {
    if(getClass().getResource(PACKAGE_PREFIX + "ckeditor.js") == null) {
      throw new IllegalStateException("The CKEditor distribution is missing from the classpath!");
    }
    this.htmlEditor = htmlEditor;
    Map<String, String> customOptionsMap = (Map<String, String>)optionMap.get(JHTMLEditor.CKEditorOptions.SET_OPTIONS_OPTION_KEY);
    if(customOptionsMap != null) {
      StringBuilder sb = new StringBuilder();
      for(String key: customOptionsMap.keySet()) {
        String value = customOptionsMap.get(key);
        if(value != null && value.length() > 0) {
          if(sb.length() > 0) {
            sb.append(',' + LS);
          }
          sb.append("          " + key + ": " + value);
        }
      }
      customOptions = sb.length() > 0? sb.toString(): null;
    } else {
      customOptions = null;
    }
    // We want potential dialogs to actually be dialogs.
    htmlEditor.getWebBrowser().addWebBrowserListener(new WebBrowserAdapter() {
      @Override
      public void windowWillOpen(WebBrowserWindowWillOpenEvent e) {
        e.setDialogWindow(true);
      }
    });
  }

  static final String LS = Utils.LINE_SEPARATOR;


  public WebServerContent getWebServerContent(final HTTPRequest httpRequest, final String resourcePath, final int instanceID) {
    if("index.html".equals(resourcePath)) {
      return new WebServerContent() {
        @Override
        public String getContentType() {
          int index = resourcePath.lastIndexOf('.');
          return getDefaultMimeType(index == -1? null: resourcePath.substring(index));
        }
        @Override
        public InputStream getInputStream() {
          String content =
            "<html>" + LS +
            "  <head>" + LS +
            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>" + LS +
            "    <style type=\"text/css\">" + LS +
            "      body, form {margin: 0; padding: 0; overflow: auto;}" + LS +
            "    </style>" + LS +
            "    <script type=\"text/javascript\" src=\"ckeditor.js\"></script>" + LS +
            "    <script type=\"text/javascript\">" + LS +
            "      var sendCommand = " + JWebBrowser.COMMAND_FUNCTION + ";" + LS +
            "      var htmlContent;" + LS +
            "      var htmlDirtyTracker;" + LS +
            "      var isDirtyTrackingActive = true;" + LS +
            "      function JH_checkDirty() {" + LS +
            "        var oEditor = CKEDITOR.instances." + EDITOR_INSTANCE + ";" + LS +
            "        if(htmlContent == null) {" + LS +
            "          try {" + LS +
            "            htmlContent = oEditor.getData();" + LS +
            "          } catch(e) {" + LS +
            "          }" + LS +
            "          htmlDirtyTracker = setTimeout('JH_checkDirty()', 1000);" + LS +
            "        } else {" + LS +
            "          try {" + LS +
            "            var newHtmlContent = oEditor.getData();" + LS +
            "            if(newHtmlContent != htmlContent) {" + LS +
            "              htmlContent = null;" + LS +
            "              htmlDirtyTracker = null;" + LS +
            "              sendCommand('[Chrriis]JH_setDirty');" + LS +
            "            } else {" + LS +
            "              htmlContent = newHtmlContent;" + LS +
            "              htmlDirtyTracker = setTimeout('JH_checkDirty()', 1000);" + LS +
            "            }" + LS +
            "          } catch(e) {" + LS +
            "            htmlDirtyTracker = setTimeout('JH_checkDirty()', 1000);" + LS +
            "          }" + LS +
            "        }" + LS +
            "      }" + LS +
            "      function JH_clearDirtyIndicator() {" + LS +
            "        if(htmlDirtyTracker) {" + LS +
            "          clearTimeout(htmlDirtyTracker);" + LS +
            "        }" + LS +
            "        htmlContent = null;" + LS +
            "        if(isDirtyTrackingActive) {" + LS +
            "          htmlDirtyTracker = setTimeout('JH_checkDirty()', 1000);" + LS +
            "        }" + LS +
            "      }" + LS +
            "      function JH_setDirtyTrackingActive(isActive) {" + LS +
            "        isDirtyTrackingActive = isActive;" + LS +
            "        JH_clearDirtyIndicator();" + LS +
            "      }" + LS +
            "      function JH_setData(html) {" + LS +
            "        var oEditor = CKEDITOR.instances." + EDITOR_INSTANCE + ";" + LS +
            "        oEditor.setData(decodeURIComponent(html));" + LS +
            "        JH_clearDirtyIndicator();" + LS +
            "      }" + LS +
            "      function JH_sendData() {" + LS +
            "        document.jhtml_form.action = 'jhtml_sendData';" + LS +
            "        document.jhtml_form.submit();" + LS +
            "        document.jhtml_form.action = 'jhtml_save';" + LS +
            "        return false;" + LS +
            "      }" + LS +
            "      function JH_doSave() {" + LS +
            "        document.jhtml_form.action = 'jhtml_save';" + LS +
            "        document.jhtml_form.submit();" + LS +
            "        return false;" + LS +
            "      }" + LS +
            "      CKEDITOR.on('instanceReady'," + LS +
            "      	 function(evt) {" + LS +
            "     	   var editor = evt.editor;" + LS +
            "     	   editor.execCommand('maximize');" + LS +
            "          JH_clearDirtyIndicator();" + LS +
            "          sendCommand('[Chrriis]JH_setLoaded');" + LS +
            "      	 });" + LS +
            "    </script>" + LS +
            "  </head>" + LS +
            "  <body>" + LS +
            "    <iframe style=\"display:none;\" id=\"j_iframe\" name=\"j_iframe\"></iframe>" + LS +
            "    <form name=\"jhtml_form\" method=\"POST\" target=\"j_iframe\" action=\"jhtml_save\">" + LS +
            "      <textarea name=\"" + EDITOR_INSTANCE + "\">&lt;p&gt;&lt;/p&gt;</textarea>" + LS +
            "      <script type=\"text/javascript\">" + LS +
            "        CKEDITOR.replace('" + EDITOR_INSTANCE + "'" +
            (customOptions != null? "," + "{" + LS + customOptions + LS + "        });": ");") + LS +
            "      </script>" + LS +
            "    </form>" + LS +
            "  </body>" + LS +
            "</html>";
            return getInputStream(content);
        }
      };
    }
//    if("customConfigurationScript.js".equals(resourcePath)) {
//      return new WebServerContent () {
//        @Override
//        public String getContentType () {
//          return getDefaultMimeType(".js");
//        }
//        @Override
//        public InputStream getInputStream () {
//          return getInputStream(customJavascriptConfiguration);
//        }
//      };
//    }
    if("jhtml_save".equals(resourcePath)) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          String html = JHTMLEditor.convertLinksToLocal(httpRequest.getHTTPPostDataArray()[0].getHeaderMap().get(EDITOR_INSTANCE));
          HTMLEditorSaveEvent e = null;
          for(HTMLEditorListener listener: htmlEditor.getHTMLEditorListeners()) {
            if(e == null) {
              e = new HTMLEditorSaveEvent(htmlEditor, html);
            }
            listener.saveHTML(e);
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

    if("jhtml_sendData".equals(resourcePath)) {
      String data = httpRequest.getHTTPPostDataArray()[0].getHeaderMap().get(EDITOR_INSTANCE);
      tempResult = data;
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
    if("editor/filemanager/connectors/php/upload.php".equals(resourcePath)) {
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
    if("editor/filemanager/connectors/php/connector.php".equals(resourcePath)) {
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
            String rootPath = file.getAbsolutePath();
            if(Utils.IS_WINDOWS && rootPath.endsWith("\\")) {
              rootPath = rootPath.substring(0, rootPath.length() - 1);
            }
            sb.append("<Folder name=\"").append(Utils.escapeXML(rootPath)).append("\"/>");
          }
          sb.append("</Folders>");
        } else {
          if("GetFoldersAndFiles".equals(command) || "GetFolders".equals(command)) {
            sb.append("<Folders>");
            for(File file: new File(currentDir).listFiles(new FileFilter() {
              public boolean accept(File pathname) {
                return !pathname.isFile() && !pathname.isHidden() && pathname.listFiles() != null;
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
    return WebServer.getDefaultWebServer ().getURLContent(WebServer.getDefaultWebServer ().getClassPathResourceURL(JHTMLEditor.class.getName(), PACKAGE_PREFIX + resourcePath));
  }

  public void clearDirtyIndicator() {
    htmlEditor.getWebBrowser().executeJavascript("JH_clearDirtyIndicator();");
  }

  public void setDirtyTrackingActive(boolean isDirtyTrackingActive) {
    htmlEditor.getWebBrowser().executeJavascript("JH_setDirtyTrackingActive(" + isDirtyTrackingActive + ");");
  }

  private volatile Object tempResult;

  public String getHTMLContent() {
    JWebBrowser webBrowser = htmlEditor.getWebBrowser();
    tempResult = this;
    webBrowser.executeJavascript("JH_sendData();");
    int timeout = Integer.parseInt(NSSystemPropertySWT.HTMLEDITOR_GETHTMLCONTENT_TIMEOUT.get("1500"));
    long start = System.currentTimeMillis();
    while(true) {
      EventDispatchUtils.sleepWithEventDispatch(new EventDispatchUtils.Condition() {
        public boolean getValue() {
          return tempResult != JHTMLEditorCKeditor.this;
        }
      }, 50);
      if(tempResult != this) {
        return (String)tempResult;
      }
      if(System.currentTimeMillis() - start > timeout) {
        break;
      }
    }
    return null;
  }

  public void setHTMLContent(String html) {
//    webBrowser.executeJavascript("JH_setData('" + Utils.encodeURL(html) + "');");
    // There is a problem: IE crashes when it has the focus and is flooded with this message.
    // While waiting for an SWT fix, we use the workaround to disable the component which loses the focus.
    JWebBrowser webBrowser = htmlEditor.getWebBrowser();
    NativeComponent nativeComponent = webBrowser.getNativeComponent();
    boolean isEnabled = nativeComponent.isEnabled();
    nativeComponent.setEnabled(false);
    new Message().syncSend(true);
    webBrowser.executeJavascript("JH_setData('" + Utils.encodeURL(html) + "');");
    new Message().syncSend(true);
    nativeComponent.setEnabled(isEnabled);
  }

}
