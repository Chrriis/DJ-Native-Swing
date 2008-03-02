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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import chrriis.dj.nativeswing.Disposable;

/**
 * @author Christopher Deckers
 */
public class JVLCPlayer extends JPanel implements Disposable {

  public static class VLCLoadingOptions {
    
    public VLCLoadingOptions() {
      this(null);
    }
    
    public VLCLoadingOptions(Map<String, String> parameters) {
      setParameters(parameters);
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
    
  }
  
  private final ResourceBundle RESOURCES = ResourceBundle.getBundle(JVLCPlayer.class.getPackage().getName().replace('.', '/') + "/resource/VLCPlayer");

  private JPanel webBrowserPanel;
  private JWebBrowser webBrowser = new JWebBrowser();
  
  private JPanel controlBarPane;
  private JButton playButton;
  private JButton pauseButton;
  private JButton stopButton;

  private WebBrowserObject webBrowserObject = new WebBrowserObject(webBrowser) {
    
    protected ObjectHTMLConfiguration getObjectHtmlConfiguration() {
      ObjectHTMLConfiguration objectHTMLConfiguration = new ObjectHTMLConfiguration();
      objectHTMLConfiguration.setHTMLLoadingMessage(RESOURCES.getString("LoadingMessage"));
      objectHTMLConfiguration.setHTMLParameters(loadingOptions.getParameters());
      objectHTMLConfiguration.setWindowsClassID("E23FE9C6-778E-49D4-B537-38FCDE4887D8");
      objectHTMLConfiguration.setWindowsInstallationURL("http://downloads.videolan.org/pub/videolan/vlc/latest/win32/axvlc.cab");
      objectHTMLConfiguration.setMimeType("application/x-vlc-plugin");
      objectHTMLConfiguration.setInstallationURL("http://www.videolan.org");
      objectHTMLConfiguration.setWindowsParamName("Src");
      objectHTMLConfiguration.setParamName("target");
      loadingOptions = null;
      return objectHTMLConfiguration;
    }
    
    @Override
    protected String getJavascriptDefinitions() {
      return
      "      function fullscreenEO() {" + LS +
      "        var flashMovie = getEmbeddedObject();" + LS +
      "        flashMovie.fullscreen();" + LS +
      "      }" + LS +
      "      function playEO() {" + LS +
      "        var flashMovie = getEmbeddedObject();" + LS +
      "        flashMovie.play();" + LS +
      "      }" + LS +
      "      function stopEO() {" + LS +
      "        var flashMovie = getEmbeddedObject();" + LS +
      "        flashMovie.stop();" + LS +
      "      }" + LS +
      "      function pauseEO() {" + LS +
      "        var flashMovie = getEmbeddedObject();" + LS +
      "        flashMovie.pause();" + LS +
      "      }" + LS;
    }
    
  };

  public JVLCPlayer() {
    super(new BorderLayout(0, 0));
    webBrowserPanel = new JPanel(new BorderLayout(0, 0));
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    add(webBrowserPanel, BorderLayout.CENTER);
    controlBarPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
    playButton = new JButton(createIcon("PlayIcon"));
    playButton.setToolTipText(RESOURCES.getString("PlayText"));
    playButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        play();
      }
    });
    controlBarPane.add(playButton);
    pauseButton = new JButton(createIcon("PauseIcon"));
    pauseButton.setToolTipText(RESOURCES.getString("PauseText"));
    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pause();
      }
    });
    controlBarPane.add(pauseButton);
    stopButton = new JButton(createIcon("StopIcon"));
    stopButton.setToolTipText(RESOURCES.getString("StopText"));
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stop();
      }
    });
    controlBarPane.add(stopButton);
    add(controlBarPane, BorderLayout.SOUTH);
    adjustBorder();
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
  
  /**
   * Get the web browser that contains this component. The web browser should only be used to add listeners, for example to listen to window creation events.
   */
  public JWebBrowser getWebBrowser() {
    return webBrowser;
  }
  
  public String getURL() {
    return webBrowserObject.getURL();
  }
  
  public void setURL(String url) {
    setURL(url, new VLCLoadingOptions());
  }
  
  private VLCLoadingOptions loadingOptions;
  
  public void setURL(String url, VLCLoadingOptions loadingOptions) {
    this.loadingOptions = loadingOptions;
    webBrowserObject.setURL(url);
  }

  public void play() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowser.execute("playEO();");
  }
  
  public void pause() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowser.execute("pauseEO();");
  }
  
  public void stop() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowser.execute("stopEO();");
  }
  
  public void toggleFullScreen() {
    if(!webBrowserObject.hasContent()) {
      return;
    }
    webBrowser.execute("fullscreenEO();");
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
  
}
