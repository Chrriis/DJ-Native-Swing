/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import chrriis.common.WebServer;
import chrriis.dj.nativeswing.NSPanelComponent;
import chrriis.dj.nativeswing.NSOption;
import chrriis.dj.nativeswing.WebBrowserObject;
import chrriis.dj.nativeswing.components.VLCInput.VLCMediaState;

/**
 * A native multimedia player. It is a browser-based component, which relies on the VLC plugin.<br/>
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fails, the methods will not have any effect. The results from methods have relevant values only when the component is valid. 
 * @author Christopher Deckers
 */
public class JVLCPlayer extends NSPanelComponent {

  private final ResourceBundle RESOURCES = ResourceBundle.getBundle(JVLCPlayer.class.getPackage().getName().replace('.', '/') + "/resource/VLCPlayer");

  private JPanel webBrowserPanel;
  private JWebBrowser webBrowser;
  
  private JPanel controlBarPane;
  private JButton playButton;
  private JButton pauseButton;
  private JButton stopButton;

  private static class NWebBrowserObject extends WebBrowserObject {
    
    private JVLCPlayer vlcPlayer;
    
    public NWebBrowserObject(JVLCPlayer vlcPlayer) {
      super(vlcPlayer.webBrowser);
      this.vlcPlayer = vlcPlayer;
    }
    
    protected ObjectHTMLConfiguration getObjectHtmlConfiguration() {
      ObjectHTMLConfiguration objectHTMLConfiguration = new ObjectHTMLConfiguration();
      objectHTMLConfiguration.setHTMLLoadingMessage(vlcPlayer.RESOURCES.getString("LoadingMessage"));
      objectHTMLConfiguration.setHTMLParameters(vlcPlayer.options.getParameters());
      objectHTMLConfiguration.setWindowsClassID("9BE31822-FDAD-461B-AD51-BE1D1C159921");
      objectHTMLConfiguration.setWindowsInstallationURL("http://downloads.videolan.org/pub/videolan/vlc/latest/win32/axvlc.cab");
      objectHTMLConfiguration.setMimeType("application/x-vlc-plugin");
      objectHTMLConfiguration.setInstallationURL("http://www.videolan.org");
      objectHTMLConfiguration.setWindowsParamName("Src");
      objectHTMLConfiguration.setParamName("target");
      objectHTMLConfiguration.setVersion("VideoLAN.VLCPlugin.2");
      vlcPlayer.options = null;
      return objectHTMLConfiguration;
    }
    
  }
  
  private WebBrowserObject webBrowserObject;
  
  WebBrowserObject getWebBrowserObject() {
    return webBrowserObject;
  }
  
  private JSlider seekBarSlider;
  private volatile boolean isAdjustingSeekBar;
  private volatile Thread updateThread;
  private JLabel timeLabel;
  private JButton volumeButton;
  private JSlider volumeSlider;
  private boolean isAdjustingVolume;

  void adjustVolumePanel() {
    volumeButton.setEnabled(true);
    VLCAudio vlcAudio = getVLCAudio();
    boolean isMute = vlcAudio.isMute();
    if(isMute) {
      volumeButton.setIcon(createIcon("VolumeOffIcon"));
      volumeButton.setToolTipText(RESOURCES.getString("VolumeOffText"));
    } else {
      volumeButton.setIcon(createIcon("VolumeOnIcon"));
      volumeButton.setToolTipText(RESOURCES.getString("VolumeOnText"));
    }
    volumeSlider.setEnabled(!isMute);
    if(!isMute) {
      isAdjustingVolume = true;
      volumeSlider.setValue(vlcAudio.getVolume());
      isAdjustingVolume = false;
    }
  }
  
  @Override
  public void removeNotify() {
    stopUpdateThread();
    super.removeNotify();
  }
  
  @Override
  public void addNotify() {
    super.addNotify();
    if(webBrowserObject.hasContent()) {
      startUpdateThread();
    }
  }
  
  private void stopUpdateThread() {
    updateThread = null;
  }
  
  private void startUpdateThread() {
    if(updateThread != null) {
      return;
    }
    if(isNativePeerDisposed()) {
      return;
    }
    updateThread = new Thread("NativeSwing - VLC Player control bar update") {
      @Override
      public void run() {
        final Thread currentThread = this;
        while(currentThread == updateThread) {
          if(isNativePeerDisposed()) {
            stopUpdateThread();
            return;
          }
          try {
            sleep(1000);
          } catch(Exception e) {}
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              if(currentThread != updateThread) {
                return;
              }
              if(!isNativePeerValid()) {
                return;
              }
              VLCInput vlcInput = getVLCInput();
              VLCMediaState state = vlcInput.getMediaState();
              boolean isValid = state == VLCMediaState.OPENING || state == VLCMediaState.BUFFERING || state == VLCMediaState.PLAYING || state == VLCMediaState.PAUSED || state == VLCMediaState.STOPPING;
              if(isValid) {
                int time = vlcInput.getAbsolutePosition();
                int length = vlcInput.getDuration();
                isValid = time >= 0 && length > 0;
                if(isValid) {
                  isAdjustingSeekBar = true;
                  seekBarSlider.setValue(Math.round(time * 10000f / length));
                  isAdjustingSeekBar = false;
                  timeLabel.setText(formatTime(time, length >= 3600000) + " / " + formatTime(length, false));
                }
              }
              if(!isValid) {
                timeLabel.setText("");
              }
              seekBarSlider.setVisible(isValid);
            }
          });
        }
      }
    };
    updateThread.setDaemon(true);
    updateThread.start();
  }
  
  private static String formatTime(int milliseconds, boolean showHours) {
    int seconds = milliseconds / 1000;
    int hours = seconds / 3600;
    int minutes = (seconds % 3600) / 60;
    seconds = seconds % 60;
    StringBuilder sb = new StringBuilder();
    if(hours != 0 || showHours) {
      sb.append(hours).append(':');
    }
    sb.append(minutes < 10? "0": "").append(minutes).append(':');
    sb.append(seconds < 10? "0": "").append(seconds);
    return sb.toString();
  }
  
  /**
   * Construct a VLC player.
   * @param options the options to configure the behavior of this component.
   */
  public JVLCPlayer(NSOption... options) {
    webBrowser = new JWebBrowser(options);
    initialize(webBrowser.getNativeComponent());
    webBrowserObject = new NWebBrowserObject(this);
    vlcAudio = new VLCAudio(this);
    vlcInput = new VLCInput(this);
    vlcPlaylist = new VLCPlaylist(this);
    vlcVideo = new VLCVideo(this);
    webBrowserPanel = new JPanel(new BorderLayout(0, 0));
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    add(webBrowserPanel, BorderLayout.CENTER);
    controlBarPane = new JPanel(new BorderLayout(0, 0));
    seekBarSlider = new JSlider(0, 10000, 0);
    seekBarSlider.setVisible(false);
    seekBarSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if(!isAdjustingSeekBar) {
          getVLCInput().setRelativePosition(((float)seekBarSlider.getValue()) / 10000);
        }
      }
    });
    controlBarPane.add(seekBarSlider, BorderLayout.NORTH);
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
    playButton = new JButton(createIcon("PlayIcon"));
    playButton.setEnabled(false);
    playButton.setToolTipText(RESOURCES.getString("PlayText"));
    playButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getVLCPlaylist().play();
      }
    });
    buttonPanel.add(playButton);
    pauseButton = new JButton(createIcon("PauseIcon"));
    pauseButton.setEnabled(false);
    pauseButton.setToolTipText(RESOURCES.getString("PauseText"));
    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getVLCPlaylist().togglePause();
      }
    });
    buttonPanel.add(pauseButton);
    stopButton = new JButton(createIcon("StopIcon"));
    stopButton.setEnabled(false);
    stopButton.setToolTipText(RESOURCES.getString("StopText"));
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getVLCPlaylist().stop();
      }
    });
    buttonPanel.add(stopButton);
    JPanel volumePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
    volumeButton = new JButton();
    Insets margin = volumeButton.getMargin();
    margin.left = Math.min(2, margin.left);
    margin.right = Math.min(2, margin.left);
    volumeButton.setMargin(margin);
    volumeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getVLCAudio().toggleMute();
      }
    });
    volumePanel.add(volumeButton);
    volumeSlider = new JSlider();
    volumeSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if(!isAdjustingVolume) {
          getVLCAudio().setVolume(volumeSlider.getValue());
        }
      }
    });
    volumeSlider.setPreferredSize(new Dimension(60, volumeSlider.getPreferredSize().height));
    volumePanel.add(volumeSlider);
    adjustVolumePanel();
    volumeButton.setEnabled(false);
    volumeSlider.setEnabled(false);
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints cons = new GridBagConstraints();
    JPanel buttonBarPanel = new JPanel(gridBag);
    cons.gridx = 0;
    cons.gridy = 0;
    cons.weightx = 1.0;
    cons.anchor = GridBagConstraints.WEST;
    cons.fill = GridBagConstraints.HORIZONTAL;
    timeLabel = new JLabel(" ");
    timeLabel.setPreferredSize(new Dimension(0, timeLabel.getPreferredSize().height));
    gridBag.setConstraints(timeLabel, cons);
    buttonBarPanel.add(timeLabel);
    cons.gridx++;
    cons.weightx = 0.0;
    cons.anchor = GridBagConstraints.CENTER;
    cons.fill = GridBagConstraints.NONE;
    gridBag.setConstraints(buttonPanel, cons);
    buttonBarPanel.add(buttonPanel);
    buttonBarPanel.setMinimumSize(buttonBarPanel.getPreferredSize());
    cons.gridx++;
    cons.weightx = 1.0;
    cons.anchor = GridBagConstraints.EAST;
    cons.fill = GridBagConstraints.HORIZONTAL;
    volumePanel.setPreferredSize(new Dimension(0, volumePanel.getPreferredSize().height));
    gridBag.setConstraints(volumePanel, cons);
    buttonBarPanel.add(volumePanel);
    controlBarPane.add(buttonBarPanel, BorderLayout.CENTER);
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
   * @return the web browser.
   */
  public JWebBrowser getWebBrowser() {
    return webBrowser;
  }
  
//  public String getLoadedResource() {
//    return webBrowserObject.getLoadedResource();
//  }
  
  /**
   * Load the player, with no content.
   */
  public void load() {
    load((VLCPluginOptions)null);
  }
  
  /**
   * Load a file.
   * @param resourceLocation the path or URL to the file.
   */
  public void load(String resourceLocation) {
    load(resourceLocation, null);
  }
  
  /**
   * Load the player, with no content.
   * @param options the options to better configure the initialization of the VLC plugin.
   */
  public void load(VLCPluginOptions options) {
    load_("", options);
  }
  
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
   * @param options the options to better configure the initialization of the VLC plugin.
   */
  public void load(Class<?> clazz, String resourcePath, VLCPluginOptions options) {
    addReferenceClassLoader(clazz.getClassLoader());
    load(WebServer.getDefaultWebServer().getClassPathResourceURL(clazz.getName(), resourcePath), options);
  }
  
  private VLCPluginOptions options;
  
  /**
   * Load a file.
   * @param resourceLocation the path or URL to the file.
   * @param options the options to better configure the initialization of the VLC plugin.
   */
  public void load(String resourceLocation, VLCPluginOptions options) {
    if("".equals(resourceLocation)) {
      resourceLocation = null;
    }
    load_(resourceLocation, options);
  }
  
  private void load_(String resourceLocation, VLCPluginOptions options) {
    if(options == null) {
      options = new VLCPluginOptions();
    }
    this.options = options;
    webBrowserObject.load(resourceLocation);
    boolean hasContent = webBrowserObject.hasContent();
    playButton.setEnabled(hasContent);
    pauseButton.setEnabled(hasContent);
    stopButton.setEnabled(hasContent);
    if(hasContent) {
      adjustVolumePanel();
      startUpdateThread();
    }
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
  
  /* ------------------------- VLC API exposed ------------------------- */
  
  private VLCAudio vlcAudio;
  
  /**
   * Get the VLC object responsible for audio-related actions.
   * @return the VLC audio object.
   */
  public VLCAudio getVLCAudio() {
    return vlcAudio;
  }
  
  private VLCInput vlcInput;
  
  /**
   * Get the VLC object responsible for input-related actions.
   * @return the VLC input object.
   */
  public VLCInput getVLCInput() {
    return vlcInput;
  }
  
  private VLCPlaylist vlcPlaylist;
  
  /**
   * Get the VLC object responsible for playlist-related actions.
   * @return the VLC playlist object.
   */
  public VLCPlaylist getVLCPlaylist() {
    return vlcPlaylist;
  }
  
  private VLCVideo vlcVideo;
  
  /**
   * Get the VLC object responsible for video-related actions.
   * @return the VLC video object.
   */
  public VLCVideo getVLCVideo() {
    return vlcVideo;
  }
  
  private List<ClassLoader> referenceClassLoaderList = new ArrayList<ClassLoader>(1);
  
  void addReferenceClassLoader(ClassLoader referenceClassLoader) {
    if(referenceClassLoader == null || referenceClassLoader == getClass().getClassLoader() || referenceClassLoaderList.contains(referenceClassLoader)) {
      return;
    }
    // If a different class loader is used to locate a resource, we need to allow th web server to find that resource
    referenceClassLoaderList.add(referenceClassLoader);
    WebServer.getDefaultWebServer().addReferenceClassLoader(referenceClassLoader);
  }
  
  @Override
  protected void finalize() throws Throwable {
    for(ClassLoader referenceClassLoader: referenceClassLoaderList) {
      WebServer.getDefaultWebServer().removeReferenceClassLoader(referenceClassLoader);
    }
    referenceClassLoaderList.clear();
    super.finalize();
  }
  
}
