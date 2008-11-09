/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.BorderLayout;
import java.io.File;
import java.net.MalformedURLException;
import java.util.EventListener;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chrriis.common.ObjectRegistry;
import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.common.WebServer.HTTPRequest;
import chrriis.common.WebServer.WebServerContent;
import chrriis.dj.nativeswing.NSOption;
import chrriis.dj.nativeswing.swtimpl.EventDispatchUtils;
import chrriis.dj.nativeswing.swtimpl.LocalMessage;
import chrriis.dj.nativeswing.swtimpl.NSPanelComponent;

/**
 * An HTML editor. It is a browser-based component, which relies on the FCKeditor (the default) or the TinyMCE editor.<br/>
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fails, the methods will not have any effect. The results from methods have relevant values only when the component is valid. 
 * @author Christopher Deckers
 * @author Jörn Heid (TinyMCE implementation)
 */
public class JHTMLEditor extends NSPanelComponent {

  static interface JHTMLEditorImplementation {
    
    public WebServerContent getWebServerContent(HTTPRequest httpRequest, String resourcePath, final int instanceID);
    
    public String getHTMLContent();
    
    public void setHTMLContent(String html);

  }
  
  private static final String HTML_EDITOR_COMPONENT_OPTION_KEY = "HTML Editor";
  static final String SET_CUSTOM_JAVASCRIPT_CONFIGURATION_OPTION_KEY = "HTML Editor Custom Configuration Script";
  
  public static enum HTMLEditorImplementation { FCKEditor, TinyMCE };
  
  public static NSOption setEditorImplementation(final HTMLEditorImplementation comp) {
    return new NSOption (HTML_EDITOR_COMPONENT_OPTION_KEY) {
      @Override
      public Object getOptionValue () {
        return comp;
      }
    };
  }
   
  /**
   * Create an option to set custom configuration for the FCKeditor or the TinyMCE editor.<br/>
   * The list of possible options to set for FCKeditor can be found here: <a href="http://docs.fckeditor.net/FCKeditor_2.x/Developers_Guide/Configuration/Configuration_Options">http://docs.fckeditor.net/FCKeditor_2.x/Developers_Guide/Configuration/Configuration_Options</a>.<br/>
   * The list of possible options to set for TinyMCE can be found here: <a href="http://wiki.moxiecode.com/index.php/TinyMCE:Configuration">http://wiki.moxiecode.com/index.php/TinyMCE:Configuration</a>.
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
  
  private JWebBrowser webBrowser;
  private int instanceID;

  private JHTMLEditorImplementation implementation;
  
  JHTMLEditorImplementation getImplementation() {
    return implementation;
  }
  
  /**
   * Construct an HTML editor.
   * @param options the options to configure the behavior of this component.
   */
  public JHTMLEditor(NSOption... options) {
    Map<Object, Object> optionMap = NSOption.createOptionMap(options);
    webBrowser = new JWebBrowser(options);
    initialize(webBrowser.getNativeComponent());
    HTMLEditorImplementation editorImplementation = (HTMLEditorImplementation)optionMap.get(HTML_EDITOR_COMPONENT_OPTION_KEY);
    HTMLEditorImplementation editorImplementation_ = editorImplementation == null? HTMLEditorImplementation.FCKEditor: editorImplementation;
    switch(editorImplementation_) {
      case FCKEditor:
        try {
          implementation = new JHTMLEditorFCKeditor(this, optionMap);
          break;
        } catch(RuntimeException e) {
          if(editorImplementation != null) {
            throw e;
          }
        }
      case TinyMCE:
        try {
          implementation = new JHTMLEditorTinyMCE(this, optionMap);
          break;
        } catch(RuntimeException e) {
          if(editorImplementation != null) {
            throw e;
          }
        }
      default:
        throw new IllegalStateException("A suitable HTML editor (FCKeditor, TinyMCE) distribution could not be found on the classpath!");
    }
    webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
      @Override
      public void commandReceived(WebBrowserEvent e, String command, String[] args) {
        if("JH_setLoaded".equals(command)) {
          Object[] listeners = listenerList.getListenerList();
          for(int i=listeners.length-2; i>=0; i-=2) {
            if(listeners[i] == InitializationListener.class) {
              ((InitializationListener)listeners[i + 1]).objectInitialized();
            }
          }
        }
      }
    });
    webBrowser.setBarsVisible(false);
    add(webBrowser, BorderLayout.CENTER);
    instanceID = ObjectRegistry.getInstance().add(this);
    final boolean[] resultArray = new boolean[1];
    InitializationListener initializationListener = new InitializationListener() {
      public void objectInitialized() {
        removeInitializationListener(this);
        resultArray[0] = true;
      }
    };
    addInitializationListener(initializationListener);
    webBrowser.navigate(WebServer.getDefaultWebServer().getDynamicContentURL(JHTMLEditor.class.getName(), String.valueOf(instanceID),  "index.html"));
    webBrowser.getNativeComponent().runSync(new LocalMessage() {
      @Override
      public Object run(Object[] args) {
        InitializationListener initializationListener = (InitializationListener)args[0];
        final boolean[] resultArray = (boolean[])args[1];
        EventDispatchUtils.sleepWithEventDispatch(new EventDispatchUtils.Condition() {
          public boolean getValue() {
            return resultArray[0];
          }
        }, 4000);
        removeInitializationListener(initializationListener);
        return null;
      }
    }, initializationListener, resultArray);
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
    int instanceID = Integer.parseInt(resourcePath.substring(0, index));
    JHTMLEditor htmlEditor = (JHTMLEditor)ObjectRegistry.getInstance().get(instanceID);
    if(htmlEditor == null) {
      return null;
    }
    String resourcePath_ = resourcePath.substring(index + 1);
    if(resourcePath_.startsWith("/")) {
      resourcePath_ = resourcePath_.substring(1);
    }
    JHTMLEditorImplementation implementation = htmlEditor.getImplementation();
    return implementation.getWebServerContent(httpRequest, resourcePath_, instanceID);
  }
  
  /**
   * Get the HTML content.
   * @return the HTML content.
   */
  public String getHTMLContent() {
    return convertLinksToLocal(implementation.getHTMLContent());
  }

  /**
   * Set the HTML content.
   * @param html the HTML content.
   */
  public void setHTMLContent(String html) {
    html = JHTMLEditor.convertLinksFromLocal(html.replaceAll("[\r\n]", " "));
    implementation.setHTMLContent(html);
  }
  
  static String convertLinksToLocal(String html) {
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

  static String convertLinksFromLocal(String html) {
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
