/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.Map;

import javax.swing.JPanel;

import chrriis.common.Registry;
import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.common.WebServer.HTTPRequest;
import chrriis.common.WebServer.WebServerContent;
import chrriis.dj.nativeswing.Disposable;
import chrriis.dj.nativeswing.NativeInterfaceHandler;
import chrriis.dj.nativeswing.Message.EmptyMessage;
import chrriis.dj.nativeswing.ui.event.HTMLEditorListener;
import chrriis.dj.nativeswing.ui.event.HTMLEditorSaveEvent;

/**
 * @author Christopher Deckers
 */
public class JHTMLEditor extends JPanel implements Disposable {

  private static final String FCK_INSTANCE = "FCKeditor1";
  
  protected JWebBrowser webBrowser;
  private int instanceID;

  protected static final String LS = System.getProperty("line.separator");

  public JHTMLEditor() {
    super(new BorderLayout(0, 0));
    if(getClass().getResource("/fckeditor/fckeditor.js") == null) {
      throw new IllegalStateException("The FCKEditor distribution is missing from the classpath!");
    }
    webBrowser = new JWebBrowser();
    webBrowser.setBarsVisible(false);
    add(webBrowser, BorderLayout.CENTER);
    instanceID = Registry.getInstance().add(this);
    webBrowser.setURL(WebServer.getDefaultWebServer().getDynamicContentURL(JHTMLEditor.class.getName(), String.valueOf(instanceID),  "index.html"));
  }
  
  protected static WebServerContent getWebServerContent(HTTPRequest httpRequest) {
    String resourcePath = httpRequest.getResourcePath();
    int index = resourcePath.indexOf('/');
    final int instanceID = Integer.parseInt(resourcePath.substring(0, index));
    final String resourcePath_ = resourcePath.substring(index + 1);
    final JHTMLEditor htmlEditor = (JHTMLEditor)Registry.getInstance().get(instanceID);
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
            "      body,form {margin: 0; padding: 0}" + LS +
            "    </style>" + LS +
            "    <script type=\"text/javascript\" src=\"fckeditor.js\"></script>" + LS +
            "    <script type=\"text/javascript\">" + LS +
            "      function sendCommand(command) {" + LS +
            "        command = command == null? '': encodeURIComponent(command);" + LS +
            "        window.location = 'command://' + command;" + LS +
            "      }" + LS +
            "      function JH_setData(html) {" + LS +
            "        var inst = FCKeditorAPI.GetInstance('" + FCK_INSTANCE + "');" + LS +
            "        inst.SetHTML(decodeURIComponent(html))" + LS +
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
            "        oFCKeditor.Create();" + LS +
            "      }" + LS +
            "      function FCKeditor_OnComplete(editorInstance) {" + LS +
            "        editorInstance.LinkedField.form.onsubmit = JH_doSave;" + LS +
            "      }" + LS +
            "    </script>" + LS +
            "  </head>" + LS +
            "  <body>" + LS +
            "  <iframe style=\"display:none;\" name=\"j_iframe\"></iframe>" + LS +
            "  <form name=\"jhtml_form\" method=\"POST\" target=\"j_iframe\">" + LS +
            "    <script type=\"text/javascript\">" + LS +
            "      createEditor();" + LS +
            "    </script>" + LS +
            "  </form>" + LS +
            "  </body>" + LS +
            "</html>" + LS;
          return getInputStream(content);
        }
      };
    }
    if("jhtml_save".equals(resourcePath_)) {
      String data = httpRequest.getHTTPPostDataArray()[0].getHeaderMap().get(FCK_INSTANCE);
      Object[] listeners = htmlEditor.listenerList.getListenerList();
      HTMLEditorSaveEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == HTMLEditorListener.class) {
          if(e == null) {
            e = new HTMLEditorSaveEvent(htmlEditor, data);
          }
          ((HTMLEditorListener)listeners[i + 1]).saveHTML(e);
        }
      }
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
  
  public String getHTML() {
    tempResult = this;
    webBrowser.execute("JH_sendData()");
    String data = null;
    for(int i=0; i<20; i++) {
      if(tempResult != this) {
        data = (String)tempResult;
        break;
      }
      NativeInterfaceHandler.syncExec(new EmptyMessage());
    }
    return data;
  }
  
  public void setHTML(String html) {
    webBrowser.execute("JH_setData('" + Utils.encodeURL(html.replaceAll("[\r\n]", "")) + "')");
  }
  
  public void addHTMLEditorListener(HTMLEditorListener listener) {
    listenerList.add(HTMLEditorListener.class, listener);
  }
  
  public void removeHTMLEditorListener(HTMLEditorListener listener) {
    listenerList.remove(HTMLEditorListener.class, listener);
  }
  
  public HTMLEditorListener[] getHTMLEditorListeners() {
    return listenerList.getListeners(HTMLEditorListener.class);
  }
  
  public void dispose() {
    webBrowser.dispose();
  }
  
  public boolean isDisposed() {
    return webBrowser.isDisposed();
  }
  
}
