/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components;

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

import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.dj.nativeswing.NSPanelComponent;
import chrriis.dj.nativeswing.WebBrowserObject;

/**
 * A native Flash player. It is a browser-based component, which relies on the Flash plugin.<br/>
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fail, the methods will not have any effect. The results from methods have relevant values only when the component is valid. 
 * @author Christopher Deckers
 */
public class JFlashPlayer extends NSPanelComponent {

  public static class FlashLoadingOptions {
    
    private Map<String, String> keyToValueVariableMap = new HashMap<String, String>();
    
    /**
     * Get the Flash plugin variables.
     * @return the variables.
     */
    public Map<String, String> getVariables() {
      return keyToValueVariableMap;
    }
    
    /**
     * Set the Flash variables that will be set when the plugin is created.
     * @param keyToValueVariableMap the map of key/value pairs.
     */
    public void setVariables(Map<String, String> keyToValueVariableMap) {
      if(keyToValueVariableMap == null) {
        keyToValueVariableMap = new HashMap<String, String>();
      }
      this.keyToValueVariableMap = keyToValueVariableMap;
    }
    
    private Map<String, String> keyToValueParameterMap = new HashMap<String, String>();
    
    /**
     * Get the Flash plugin HTML parameters.
     * @return the parameters.
     */
    public Map<String, String> getParameters() {
      return keyToValueParameterMap;
    }
    
    /**
     * Set the Flash HTML parameters that will be used when the plugin is created.
     * @param keyToValueParameterMap the map of key/value pairs.
     */
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
    public String getLocalFileURL(File localFile) {
      // Local files cannot be played due to security restrictions. We need to proxy.
      return WebServer.getDefaultWebServer().getResourcePathURL(localFile.getParent(), localFile.getName());
    }

  };

  /**
   * Construct a flash player.
   */
  public JFlashPlayer() {
    setNativeComponent(webBrowser.getNativeComponent());
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
  
//  public String getLoadedResource() {
//    return webBrowserObject.getLoadedResource();
//  }
  
  /**
   * Load a file from the classpath.
   * @param clazz the reference clazz of the file to load.
   * @param resourcePath the path to the file.
   */
  public void load(Class<?> clazz, String resourcePath) {
    load(clazz, resourcePath, null);
  }
  
  /**
   * Load a file from the classpath.
   * @param clazz the reference clazz of the file to load.
   * @param resourcePath the path to the file.
   * @param loadingOptions the options to better configure the initialization of the flash plugin.
   */
  public void load(Class<?> clazz, String resourcePath, FlashLoadingOptions loadingOptions) {
    load(WebServer.getDefaultWebServer().getClassPathResourceURL(clazz.getName(), resourcePath), loadingOptions);
  }
  
  /**
   * Load a file.
   * @param resourcePath the path or URL to the file.
   */
  public void load(String resourcePath) {
    load(resourcePath, null);
  }
  
  private FlashLoadingOptions loadingOptions;
  
  /**
   * Load a file.
   * @param resourcePath the path or URL to the file.
   * @param loadingOptions the options to better configure the initialization of the flash plugin.
   */
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

  /**
   * Play a timeline based flash applications. 
   */
  public void play() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowserObject.invokeObjectFunction("Play");
  }
  
  /**
   * Pause the execution of timeline based flash applications. 
   */
  public void pause() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowserObject.invokeObjectFunction("StopPlay");
  }
  
  /**
   * Stop the execution of timeline based flash applications. 
   */
  public void stop() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowserObject.invokeObjectFunction("Rewind");
  }
  
  /**
   * Set the value of a variable. It is also possible to set object properties with that method, though it is recommended to create special accessor methods.
   * @param name the name of the variable.
   * @param value the new value of the variable.
   */
  public void setVariable(String name, String value) {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowserObject.invokeObjectFunction("SetVariable", name, value);
  }
  
  /**
   * Get the value of a variable, or an object property if the web browser used is Internet Explorer. On Mozilla, it is not possible to access object properties with that method, an accessor method or a global variable in the Flash application should be used instead.
   * @return the value, potentially a String, Number, Boolean.
   */
  public Object getVariable(String name) {
    if(!webBrowserObject.hasContent()) {
      return null;
    }
    return webBrowserObject.invokeObjectFunctionWithResult("GetVariable", name);
  }
  
  /**
   * Invoke a function on the Flash object, with optional arguments (Strings, numbers, booleans).
   * @param functionName the name of the function to invoke.
   * @param args optional arguments.
   */
  public void invokeFlashFunction(String functionName, Object... args) {
    webBrowserObject.invokeObjectFunction(functionName, args);
  }
  
  /**
   * Invoke a function on the Flash object and waits for a result, with optional arguments (Strings, numbers, booleans).
   * @param functionName the name of the function to invoke.
   * @param args optional arguments.
   * @return The value, potentially a String, Number, Boolean.
   */
  public Object invokeFlashFunctionWithResult(String functionName, Object... args) {
    return webBrowserObject.invokeObjectFunctionWithResult(functionName, args);
  }
  
  /**
   * Get the web browser that contains this component. The web browser should only be used to add listeners, for example to listen to window creation events.
   * @return the web browser.
   */
  public JWebBrowser getWebBrowser() {
    return webBrowser;
  }
  
  /**
   * Indicate whether the control bar is visible.
   * @return true if the control bar is visible.
   */
  public boolean isControlBarVisible() {
    return controlBarPane.isVisible();
  }
  
  /**
   * Set whether the control bar is visible.
   * @param isControlBarVisible true if the control bar should be visible, false otherwise.
   */
  public void setControlBarVisible(boolean isControlBarVisible) {
    controlBarPane.setVisible(isControlBarVisible);
    adjustBorder();
  }
  
}
