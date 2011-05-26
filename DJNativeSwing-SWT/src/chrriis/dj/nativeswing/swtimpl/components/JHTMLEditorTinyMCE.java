/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

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
 * @author Jörn Heid
 * @author Christopher Deckers
 */
class JHTMLEditorTinyMCE implements JHTMLEditorImplementation {

  private static final String PACKAGE_PREFIX = "/tiny_mce/";
  private static final String EDITOR_INSTANCE = "HTMLeditor1";

  private final JHTMLEditor htmlEditor;
  private final String customOptions;
  private final String customHTMLHeaders;

  private static final String LS = Utils.LINE_SEPARATOR;

  @SuppressWarnings("unchecked")
  public JHTMLEditorTinyMCE(JHTMLEditor htmlEditor, Map<Object, Object> optionMap) {
    if(getClass().getResource(PACKAGE_PREFIX + "tiny_mce.js") == null) {
      throw new IllegalStateException("The TinyMCE distribution is missing from the classpath!");
    }
    this.htmlEditor = htmlEditor;
    Map<String, String> customOptionsMap = (Map<String, String>)optionMap.get(JHTMLEditor.TinyMCEOptions.SET_OPTIONS_OPTION_KEY);
    if(customOptionsMap != null) {
      StringBuilder sb = new StringBuilder();
      for(String key: customOptionsMap.keySet()) {
        String value = customOptionsMap.get(key);
        if(value != null && value.length() > 0) {
          if(sb.length() > 0) {
            sb.append(',' + LS);
          }
          sb.append("        " + key + ": " + value);
        }
      }
      customOptions = sb.length() > 0? sb.toString(): null;
    } else {
      customOptions = null;
    }
    customHTMLHeaders = (String)optionMap.get(JHTMLEditor.TinyMCEOptions.SET_CUSTOM_HTML_HEADERS_OPTION_KEY);
    // We want potential dialogs to actually be dialogs.
    htmlEditor.getWebBrowser().addWebBrowserListener(new WebBrowserAdapter() {
      @Override
      public void windowWillOpen(WebBrowserWindowWillOpenEvent e) {
        e.setDialogWindow(true);
      }
    });
  }

  public WebServerContent getWebServerContent(final HTTPRequest httpRequest, final String resourcePath, final int instanceID) {
    if ("index.html".equals (resourcePath)) {
      return new WebServerContent() {
        @Override
        public String getContentType() {
          int index = resourcePath.lastIndexOf('.');
          return getDefaultMimeType(index == -1? null: resourcePath.substring(index));
        }
        @Override
        public InputStream getInputStream () {
          String content =
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">" + LS +
            "  <head>" + LS +
            "    <title></title>" + LS +
            (customHTMLHeaders != null? customHTMLHeaders + LS: "") +
            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>" + LS +
            "    <style type=\"text/css\">" + LS +
            "      body, form {margin: 0; padding: 0; overflow: auto;}" + LS +
            "    </style>" + LS +
            "    <script type=\"text/javascript\" src=\"tiny_mce.js\"></script>" + LS +
            "    <script type=\"text/javascript\">" + LS +
            "      function debug (text) {" + LS +
            "        document.getElementById ('debug').innerHTML = text;" + LS +
            "      }" + LS +
//            "      window.onerror = function (e) {" + LS +
//            "        var text = '';" + LS +
//            "        if (typeof e == 'string') {" + LS +
//            "          text = e;" + LS +
//            "        } else {" +
//            "          for (var x in e) {" + LS +
//            "            text += x+': '+e[x]+'\\n';" + LS +
//            "          }" + LS +
//            "        }" + LS +
//            "        debug (text);" + LS +
//            "      };" + LS +
            "      var sendCommand = " + JWebBrowser.COMMAND_FUNCTION + ";" + LS +
            "      var htmlContent;" + LS +
            "      var htmlDirtyTracker;" + LS +
            "      var isDirtyTrackingActive = true;" + LS +
            "      function JH_checkDirty() {" + LS +
            "        var inst = tinyMCE.get ('" + EDITOR_INSTANCE + "');" + LS +
            "        if(htmlContent == null) {" + LS +
            "          try {" + LS +
            "            htmlContent = inst.getContent();" + LS +
            "          } catch(e) {" + LS +
            "          }" + LS +
            "          htmlDirtyTracker = setTimeout('JH_checkDirty()', 1000);" + LS +
            "        } else {" + LS +
            "          try {" + LS +
            "            var newHtmlContent = inst.getContent();" + LS +
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
            "      function JH_setData (html) {" + LS +
            "        tinyMCE.get ('" + EDITOR_INSTANCE + "').setContent (decodeURIComponent (html));" + LS +
            "        JH_clearDirtyIndicator();" + LS +
            "      }" + LS +
            "      function JH_sendData () {" + LS +
            "        tinyMCE.get ('" + EDITOR_INSTANCE + "').save ();" + LS +
            "        document.jhtml_form.action = 'jhtml_sendData';" + LS +
            "        document.jhtml_form.submit ();" + LS +
            "      }" + LS +
            "      function JH_doSave () {" + LS +
            "        tinyMCE.get ('" + EDITOR_INSTANCE + "').save ();" + LS +
            "        document.jhtml_form.action = 'jhtml_save';" + LS +
            "        document.jhtml_form.submit ();" + LS +
            "        return false;" + LS +
            "      }" + LS +
            "      var opts = {" + LS +
            "        mode: 'exact'," + LS +
            "        elements: '" + EDITOR_INSTANCE + "'," + LS +
            "        theme: 'advanced'," + LS +
            "        save_onsavecallback : 'JH_doSave'," + LS +
            "        setup: function (ed) {" + LS +
            "          ed.onInit.add (function (ed) {" + LS +
            "            sendCommand ('[Chrriis]JH_setLoaded');" + LS +
            "          })" + LS +
            "        }" + LS +
            "      };" + LS +
            (customOptions != null? "      var addOpts = {" + LS + customOptions + LS + "      };" + LS + "      for (var x in addOpts) {" + LS + "        opts[x] = addOpts[x];" + LS + "      }" + LS: "") +
            "      tinyMCE.init (opts);" + LS +
            "      JH_clearDirtyIndicator();" + LS +
            "    </script>" + LS +
            "  </head>" + LS +
            "  <body>" + LS +
            "    <div id=\"debug\"></div>" + LS +
            "    <iframe style=\"display:none;\" name=\"j_iframe\"></iframe>" + LS +
            "    <form name=\"jhtml_form\" method=\"POST\" target=\"j_iframe\">" + LS +
            "      <textarea name=\"" + EDITOR_INSTANCE + "\" id=\"" + EDITOR_INSTANCE + "\" style=\"width:100%;height:100%\"></textarea>" + LS +
            "    </form>" + LS +
            "  </body>" + LS +
            "</html>" + LS;
          return getInputStream(content);
        }
      };
    }
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
    return WebServer.getDefaultWebServer ().getURLContent(WebServer.getDefaultWebServer ().getClassPathResourceURL (JHTMLEditor.class.getName(), PACKAGE_PREFIX + resourcePath));
  }

  public void clearDirtyIndicator() {
    htmlEditor.getWebBrowser().executeJavascript("JH_clearDirtyIndicator();");
  }

  public void setDirtyTrackingActive(boolean isDirtyTrackingActive) {
    htmlEditor.getWebBrowser().executeJavascript("JH_setDirtyTrackingActive(" + isDirtyTrackingActive + ");");
  }

  private volatile Object tempResult;

  /**
   * Get the HTML content.
   * @return the HTML content.
   */
  public String getHTMLContent() {
    JWebBrowser webBrowser = htmlEditor.getWebBrowser();
    if(!webBrowser.isNativePeerInitialized()) {
      return "";
    }
    tempResult = this;
    webBrowser.executeJavascript("JH_sendData();");
    int timeout = Integer.parseInt(NSSystemPropertySWT.HTMLEDITOR_GETHTMLCONTENT_TIMEOUT.get("1500"));
    long start = System.currentTimeMillis();
    while(true) {
      EventDispatchUtils.sleepWithEventDispatch(new EventDispatchUtils.Condition() {
        public boolean getValue() {
          return tempResult != JHTMLEditorTinyMCE.this;
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
