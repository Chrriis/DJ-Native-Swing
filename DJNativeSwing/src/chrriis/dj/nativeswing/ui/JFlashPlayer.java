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
    
    public String customJavascriptDefinitions;
    
    public void setCustomJavascriptDefinitions(String customJavascriptDefinitions) {
      this.customJavascriptDefinitions = customJavascriptDefinitions;
    }
    
    public String getCustomJavascriptDefinitions() {
      return customJavascriptDefinitions;
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
      String customJavascript = loadingOptions.getCustomJavascriptDefinitions();
      if(customJavascript != null) {
        customJavascript = customJavascript + LS;
      } else {
        customJavascript = "";
      }
      return
      customJavascript +
      "      function setVariableEO(variableName, variableValue) {" + LS +
      "        var flashMovie = getEmbeddedObject();" + LS +
      "        flashMovie.SetVariable(decodeURIComponent(variableName), decodeURIComponent(variableValue));" + LS +
      "      }" + LS +
      "      function getVariableEO(variableName) {" + LS +
      "        var flashMovie = getEmbeddedObject();" + LS +
      "        try {" + LS +
      "          sendCommand('getVariableEO', flashMovie.GetVariable(decodeURIComponent(variableName)));" + LS +
      "        } catch(e) {" + LS +
      "          sendCommand('getVariableEO', '');" + LS +
      "        }" + LS +
      "      }" + LS;
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
  
  public String getURL() {
    return webBrowserObject.getURL();
  }
  
  public void setURL(String url) {
    setURL(url, new FlashLoadingOptions());
  }
  
  private FlashLoadingOptions loadingOptions;
  
  public void setURL(String url, FlashLoadingOptions loadingOptions) {
    if("".equals(url)) {
      url = null;
    }
    this.loadingOptions = loadingOptions;
    webBrowserObject.setURL(url);
    playButton.setEnabled(true);
    pauseButton.setEnabled(true);
    stopButton.setEnabled(true);
  }

  public void play() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowser.execute("getEmbeddedObject().Play();");
  }
  
  public void pause() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowser.execute("getEmbeddedObject().StopPlay();");
  }
  
  public void stop() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowser.execute("getEmbeddedObject().Rewind();");
  }
  
  public void setVariable(String name, String value) {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowser.execute("setVariableEO('" + Utils.encodeURL(name) + "', '" + Utils.encodeURL(value) + "')");
  }
  
  /**
   * @return The value, or null or an empty string when the variable is not defined.
   */
  public String getVariable(String name) {
    if(!webBrowserObject.hasContent()) {
      return null;
    }
    return webBrowser.executeAndWaitForCommandResult("getVariableEO", "getVariableEO('" + Utils.encodeURL(name) + "');");
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
