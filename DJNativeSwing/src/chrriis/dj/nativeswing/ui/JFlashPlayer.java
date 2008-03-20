/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import chrriis.common.Disposable;
import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.dj.nativeswing.ui.event.InitializationListener;

/**
 * @author Christopher Deckers
 */
public class JFlashPlayer extends JPanel implements Disposable {

  public static class FlashLoadingOptions {
    
    protected Map<String, String> keyToValueVariableMap = new HashMap<String, String>();
    
    public Map<String, String> getVariables() {
      return keyToValueVariableMap;
    }
    
    public void setVariables(Map<String, String> keyToValueVariableMap) {
      if(keyToValueVariableMap == null) {
        keyToValueVariableMap = new HashMap<String, String>();
      }
      this.keyToValueVariableMap = keyToValueVariableMap;
    }
    
    protected Map<String, String> keyToValueParameterMap = new HashMap<String, String>();
    
    public Map<String, String> getParameters() {
      return keyToValueParameterMap;
    }
    
    public void setParameters(Map<String, String> keyToValueParameterMap) {
      if(keyToValueParameterMap == null) {
        keyToValueParameterMap = new HashMap<String, String>();
      }
      this.keyToValueParameterMap = keyToValueParameterMap;
    }
    
    Map<String, String> getHTMLParameters() {
      HashMap<String, String> htmlParameters = new HashMap<String, String>(getParameters());
      StringBuffer variablesSB = new StringBuffer();
      for(Entry<String, String> variable: getVariables().entrySet()) {
        if(variablesSB.length() > 0) {
          variablesSB.append('&');
        }
        variablesSB.append(Utils.escapeXML(variable.getKey())).append('=').append(Utils.escapeXML(variable.getValue()));
      }
      if(variablesSB.length() > 0) {
        htmlParameters.put("flashvars", variablesSB.toString());
      }
      htmlParameters.put("allowScriptAccess", "always");
      htmlParameters.put("swliveconnect", "true");
      return htmlParameters;
    }
    
  }
  
  private final ResourceBundle RESOURCES = ResourceBundle.getBundle(JFlashPlayer.class.getPackage().getName().replace('.', '/') + "/resource/FlashPlayer");

  private JPanel webBrowserPanel;
  private JWebBrowser webBrowser = new JWebBrowser();
  
  private JPanel controlBarPane;
  private JButton playButton;
  private JButton pauseButton;
  private JButton stopButton;

  private WebBrowserObject webBrowserObject = new WebBrowserObject(this, webBrowser) {
    
    protected ObjectHTMLConfiguration getObjectHtmlConfiguration() {
      ObjectHTMLConfiguration objectHTMLConfiguration = new ObjectHTMLConfiguration();
      objectHTMLConfiguration.setHTMLLoadingMessage(RESOURCES.getString("LoadingMessage"));
      objectHTMLConfiguration.setHTMLParameters(loadingOptions.getHTMLParameters());
      objectHTMLConfiguration.setWindowsClassID("D27CDB6E-AE6D-11cf-96B8-444553540000");
      objectHTMLConfiguration.setWindowsInstallationURL("http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=9,0,0,0");
      objectHTMLConfiguration.setMimeType("application/x-shockwave-flash");
      objectHTMLConfiguration.setInstallationURL("http://www.adobe.com/go/getflashplayer");
      objectHTMLConfiguration.setWindowsParamName("movie");
      objectHTMLConfiguration.setParamName("src");
      loadingOptions = null;
      return objectHTMLConfiguration;
    }
    
    @Override
    protected String getJavascriptDefinitions() {
      return JFlashPlayer.this.getJavascriptDefinitions();
    }
    
    @Override
    protected String getLocalFileURL(File localFile) {
      // Local files cannot be played due to security restrictions. We need to proxy.
      return WebServer.getDefaultWebServer().getResourcePathURL(localFile.getParent(), localFile.getName());
    }

  };

  public JFlashPlayer() {
    super(new BorderLayout(0, 0));
    webBrowserPanel = new JPanel(new BorderLayout(0, 0));
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    add(webBrowserPanel, BorderLayout.CENTER);
    controlBarPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
    playButton = new JButton(createIcon("PlayIcon"));
    playButton.setEnabled(false);
    playButton.setToolTipText(RESOURCES.getString("PlayText"));
    playButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        play();
      }
    });
    controlBarPane.add(playButton);
    pauseButton = new JButton(createIcon("PauseIcon"));
    pauseButton.setEnabled(false);
    pauseButton.setToolTipText(RESOURCES.getString("PauseText"));
    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pause();
      }
    });
    controlBarPane.add(pauseButton);
    stopButton = new JButton(createIcon("StopIcon"));
    stopButton.setEnabled(false);
    stopButton.setToolTipText(RESOURCES.getString("StopText"));
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stop();
      }
    });
    controlBarPane.add(stopButton);
    add(controlBarPane, BorderLayout.SOUTH);
    adjustBorder();
    setControlBarVisible(false);
  }
  
  private void adjustBorder() {
    if(isControlBarVisible()) {
      webBrowserPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    } else {
      webBrowserPanel.setBorder(null);
    }
  }
  
  private Icon createIcon(String resourceKey) {
    String value = RESOURCES.getString(resourceKey);
    return value.length() == 0? null: new ImageIcon(JWebBrowser.class.getResource(value));
  }
  
  protected String getJavascriptDefinitions() {
    return null;
  }
  
  public String getLoadedResource() {
    return webBrowserObject.getLoadedResource();
  }
  
  /**
   * Load a file from the classpath.
   */
  public void load(Class<?> clazz, String resourcePath) {
    load(clazz, resourcePath, null);
  }
  
  /**
   * Load a file from the classpath.
   */
  public void load(Class<?> clazz, String resourcePath, FlashLoadingOptions loadingOptions) {
    load(WebServer.getDefaultWebServer().getClassPathResourceURL(clazz.getName(), resourcePath), loadingOptions);
  }
  
  public void load(String resourcePath) {
    load(resourcePath, null);
  }
  
  private FlashLoadingOptions loadingOptions;
  
  public void load(String resourcePath, FlashLoadingOptions loadingOptions) {
    if("".equals(resourcePath)) {
      resourcePath = null;
    }
    if(loadingOptions == null) {
      loadingOptions = new FlashLoadingOptions();
    }
    this.loadingOptions = loadingOptions;
    webBrowserObject.load(resourcePath);
    boolean isEnabled = resourcePath != null;
    playButton.setEnabled(isEnabled);
    pauseButton.setEnabled(isEnabled);
    stopButton.setEnabled(isEnabled);
  }

  public void play() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowserObject.callObjectFunction("Play");
  }
  
  public void pause() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowserObject.callObjectFunction("StopPlay");
  }
  
  public void stop() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowserObject.callObjectFunction("Rewind");
  }
  
  public void setVariable(String name, String value) {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowserObject.callObjectFunction("SetVariable", name, value);
  }
  
  /**
   * Get the value of a variable.
   * Note that on Mozilla, it is not possible to access object properties with that method, you should create your own accessor in the Flash application or use a global variable.
   * @return The value, or null or an empty string when the variable is not defined.
   */
  public String getVariable(String name) {
    if(!webBrowserObject.hasContent()) {
      return null;
    }
    return webBrowserObject.callObjectFunctionWithResult("GetVariable", name);
  }
  
  /**
   * Call a function on the Flash object, with optional arguments (Strings, numbers, booleans).
   */
  public void callFlashFunction(String functionName, Object... args) {
    webBrowserObject.callObjectFunction(functionName, args);
  }
  
  /**
   * Call a function on the Flash object and waits for a result, with optional arguments (Strings, numbers, booleans).
   */
  public String callFlashFunctionWithResult(String functionName, Object... args) {
    return webBrowserObject.callObjectFunctionWithResult(functionName, args);
  }
  
  /**
   * Get the web browser that contains this component. The web browser should only be used to add listeners, for example to listen to window creation events.
   */
  public JWebBrowser getWebBrowser() {
    return webBrowser;
  }
  
  public boolean isControlBarVisible() {
    return controlBarPane.isVisible();
  }
  
  public void setControlBarVisible(boolean isVisible) {
    controlBarPane.setVisible(isVisible);
    adjustBorder();
  }
  
  public void dispose() {
    webBrowserObject.dispose();
  }
  
  public boolean isDisposed() {
    return webBrowserObject.isDisposed();
  }
  
  /**
   * Run a command in sequence with other calls from this class. Calls are performed only when the component is initialized, and this method adds to the queue of calls in case it is not.
   */
  public void run(Runnable runnable) {
    webBrowser.run(runnable);
  }
  
  /**
   * Forces the component to initialize. All method calls will then be synchronous instead of being queued waiting for the componant to be initialized.
   * This call fails if the component is not in a component hierarchy with a Window ancestor.
   */
  public void initialize() {
    webBrowser.initialize();
  }
  
  public void addInitializationListener(InitializationListener listener) {
    webBrowserObject.addInitializationListener(listener);
  }
  
  public void removeInitializationListener(InitializationListener listener) {
    webBrowserObject.removeInitializationListener(listener);
  }
  
  public InitializationListener[] getInitializationListeners() {
    return webBrowserObject.getInitializationListeners();
  }

}
