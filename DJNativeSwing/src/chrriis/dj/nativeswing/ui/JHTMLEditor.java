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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.common.Disposable;
import chrriis.common.Registry;
import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.common.WebServer.HTTPRequest;
import chrriis.common.WebServer.WebServerContent;
import chrriis.dj.nativeswing.LocalMessage;
import chrriis.dj.nativeswing.NativeInterfaceHandler;
import chrriis.dj.nativeswing.Message.EmptyMessage;
import chrriis.dj.nativeswing.ui.event.HTMLEditorListener;
import chrriis.dj.nativeswing.ui.event.HTMLEditorSaveEvent;
import chrriis.dj.nativeswing.ui.event.InitializationEvent;
import chrriis.dj.nativeswing.ui.event.InitializationListener;
import chrriis.dj.nativeswing.ui.event.WebBrowserAdapter;
import chrriis.dj.nativeswing.ui.event.WebBrowserEvent;

/**
 * @author Christopher Deckers
 */
public class JHTMLEditor extends JPanel implements Disposable {

  private static final String FCK_INSTANCE = "FCKeditor1";
  
  protected JWebBrowser webBrowser;
  private int instanceID;

  protected static final String LS = System.getProperty("line.separator");

  private static class NWebBrowserListener extends WebBrowserAdapter {
    protected Reference<JHTMLEditor> htmlEditor;
    protected NWebBrowserListener(JHTMLEditor htmlEditor) {
      this.htmlEditor = new WeakReference<JHTMLEditor>(htmlEditor);
    }
    @Override
    public void commandReceived(WebBrowserEvent e, String command) {
      JHTMLEditor htmlEditor = this.htmlEditor.get();
      if(htmlEditor == null) {
        return;
      }
      if("JH_setLoaded".equals(command)) {
        Object[] listeners = htmlEditor.listenerList.getListenerList();
        InitializationEvent ev = null;
        for(int i=listeners.length-2; i>=0; i-=2) {
          if(listeners[i] == InitializationListener.class) {
            if(ev == null) {
              ev = new InitializationEvent(htmlEditor);
            }
            ((InitializationListener)listeners[i + 1]).objectInitialized(ev);
          }
        }
      }
    }
  }
  
  private class CMLocal_waitForInitialization extends LocalMessage {
    @Override
    public Object run() {
      InitializationListener initializationListener = (InitializationListener)args[0];
      Boolean[] resultArray = (Boolean[])args[1];
      for(long time = System.currentTimeMillis(); !resultArray[0].booleanValue() && System.currentTimeMillis() - time < 4000; ) {
        NativeInterfaceHandler.syncExec(new EmptyMessage());
        try {
          Thread.sleep(50);
        } catch(Exception e) {}
      }
      removeInitializationListener(initializationListener);
      return null;
    }
  }
  
  public JHTMLEditor() {
    super(new BorderLayout(0, 0));
    if(getClass().getResource("/fckeditor/fckeditor.js") == null) {
      throw new IllegalStateException("The FCKEditor distribution is missing from the classpath!");
    }
    webBrowser = new JWebBrowser();
    webBrowser.addWebBrowserListener(new NWebBrowserListener(this));
    webBrowser.setBarsVisible(false);
    add(webBrowser, BorderLayout.CENTER);
    instanceID = Registry.getInstance().add(this);
    final Boolean[] resultArray = new Boolean[] {Boolean.FALSE};
    InitializationListener initializationListener = new InitializationListener() {
      public void objectInitialized(InitializationEvent e) {
        removeInitializationListener(this);
        resultArray[0] = Boolean.TRUE;
      }
    };
    addInitializationListener(initializationListener);
    webBrowser.setURL(WebServer.getDefaultWebServer().getDynamicContentURL(JHTMLEditor.class.getName(), String.valueOf(instanceID),  "index.html"));
    webBrowser.getDisplayComponent().runSync(new CMLocal_waitForInitialization(), initializationListener, resultArray);
  }
  
  /**
   * Get the web browser that contains this component. The web browser should only be used to add listeners, for example to listen to window creation events.
   */
  public JWebBrowser getWebBrowser() {
    return webBrowser;
  }
  
  protected static WebServerContent getWebServerContent(final HTTPRequest httpRequest) {
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
            "        sendCommand('JH_setLoaded');" + LS +
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
  
  public String getHTML() {
    if(!webBrowser.isInitialized()) {
      return "";
    }
    tempResult = this;
    webBrowser.execute("JH_sendData()");
    String html = null;
    for(int i=0; i<20; i++) {
      if(tempResult != this) {
        html = (String)tempResult;
        break;
      }
      NativeInterfaceHandler.syncExec(new EmptyMessage());
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
  
  public void setHTML(String html) {
    html = convertLinksFromLocal(html.replaceAll("[\r\n]", ""));
    webBrowser.execute("JH_setData('" + Utils.encodeURL(html) + "')");
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
  
  /**
   * Run a command in sequence with other calls from this class. Calls are performed only when the component is initialized, and this method adds to the queue of calls in case it is not.
   */
  public void run(Runnable runnable) {
    webBrowser.run(runnable);
  }
  
  public void addInitializationListener(InitializationListener listener) {
    listenerList.add(InitializationListener.class, listener);
  }
  
  public void removeInitializationListener(InitializationListener listener) {
    listenerList.remove(InitializationListener.class, listener);
  }
  
  public InitializationListener[] getInitializationListeners() {
    return listenerList.getListeners(InitializationListener.class);
  }

}
