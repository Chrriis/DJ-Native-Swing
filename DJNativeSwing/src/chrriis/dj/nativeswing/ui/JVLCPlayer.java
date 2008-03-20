/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
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

import chrriis.common.Disposable;
import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.dj.nativeswing.ui.JVLCPlayer.VLCInput.VLCState;
import chrriis.dj.nativeswing.ui.event.InitializationListener;

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

  private WebBrowserObject webBrowserObject = new WebBrowserObject(this, webBrowser) {
    
    protected ObjectHTMLConfiguration getObjectHtmlConfiguration() {
      ObjectHTMLConfiguration objectHTMLConfiguration = new ObjectHTMLConfiguration();
      objectHTMLConfiguration.setHTMLLoadingMessage(RESOURCES.getString("LoadingMessage"));
      objectHTMLConfiguration.setHTMLParameters(loadingOptions.getParameters());
      objectHTMLConfiguration.setWindowsClassID("9BE31822-FDAD-461B-AD51-BE1D1C159921");
      objectHTMLConfiguration.setWindowsInstallationURL("http://downloads.videolan.org/pub/videolan/vlc/latest/win32/axvlc.cab");
      objectHTMLConfiguration.setMimeType("application/x-vlc-plugin");
      objectHTMLConfiguration.setInstallationURL("http://www.videolan.org");
      objectHTMLConfiguration.setWindowsParamName("Src");
      objectHTMLConfiguration.setParamName("target");
      objectHTMLConfiguration.setVersion("VideoLAN.VLCPlugin.2");
      loadingOptions = null;
      return objectHTMLConfiguration;
    }
    
  };
  
  private JSlider seekBarSlider;
  private volatile boolean isAdjustingSeekBar;
  private volatile Thread updateThread;
  private JLabel timeLabel;
  private JButton volumeButton;
  private JSlider volumeSlider;
  private boolean isAdjustingVolume;

  private void adjustVolumePanel() {
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
    updateThread = new Thread("NativeSwing - VLC Player control bar update") {
      @Override
      public void run() {
        final Thread currentThread = this;
        while(currentThread == updateThread) {
          try {
            sleep(1000);
          } catch(Exception e) {}
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              if(currentThread != updateThread) {
                return;
              }
              VLCInput vlcInput = getVLCInput();
              VLCState state = vlcInput.getState();
              boolean isValid = state == VLCState.OPENING || state == VLCState.BUFFERING || state == VLCState.PLAYING || state == VLCState.PAUSED || state == VLCState.STOPPING;
              if(isValid) {
                int time = vlcInput.getTime();
                int length = vlcInput.getLength();
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
  
  public JVLCPlayer() {
    super(new BorderLayout(0, 0));
    webBrowserPanel = new JPanel(new BorderLayout(0, 0));
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    add(webBrowserPanel, BorderLayout.CENTER);
    controlBarPane = new JPanel(new BorderLayout(0, 0));
    seekBarSlider = new JSlider(0, 10000, 0);
    seekBarSlider.setVisible(false);
    seekBarSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if(!isAdjustingSeekBar) {
          getVLCInput().setPosition(((float)seekBarSlider.getValue()) / 10000);
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
    load((VLCLoadingOptions)null);
  }
  
  public void load(String resourcePath) {
    load(resourcePath, null);
  }
  
  /**
   * Load the player, with no content.
   */
  public void load(VLCLoadingOptions loadingOptions) {
    load("", loadingOptions);
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
  public void load(Class<?> clazz, String resourcePath, VLCLoadingOptions loadingOptions) {
    load(WebServer.getDefaultWebServer().getClassPathResourceURL(clazz.getName(), resourcePath), loadingOptions);
  }
  
  private VLCLoadingOptions loadingOptions;
  
  public void load(String resourcePath, VLCLoadingOptions loadingOptions) {
    if("".equals(resourcePath)) {
      resourcePath = null;
    }
    load_(resourcePath, loadingOptions);
  }
  
  private void load_(String resourcePath, VLCLoadingOptions loadingOptions) {
    if(loadingOptions == null) {
      loadingOptions = new VLCLoadingOptions();
    }
    this.loadingOptions = loadingOptions;
    webBrowserObject.load(resourcePath);
    boolean hasContent = webBrowserObject.hasContent();
    playButton.setEnabled(hasContent);
    pauseButton.setEnabled(hasContent);
    stopButton.setEnabled(hasContent);
    if(hasContent) {
      adjustVolumePanel();
      startUpdateThread();
    }
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
  
  /* ------------------------- VLC API exposed ------------------------- */
  
  public static class VLCAudio {
    private JVLCPlayer vlcPlayer;
    private WebBrowserObject webBrowserObject;
    private VLCAudio(JVLCPlayer vlcPlayer) {
      this.vlcPlayer = vlcPlayer;
      this.webBrowserObject = vlcPlayer.webBrowserObject;
    }
    public void setMute(boolean isMute) {
      webBrowserObject.setObjectProperty("audio.mute", isMute);
      vlcPlayer.adjustVolumePanel();
    }
    public boolean isMute() {
      return "true".equals(webBrowserObject.getObjectProperty("audio.mute"));
    }
    public void setVolume(int volume) {
      if(volume < 0 || volume > 100) {
        throw new IllegalArgumentException("The volume must be between 0 and 100");
      }
      webBrowserObject.setObjectProperty("audio.volume", Math.round((volume * 1.99 + 1)));
      vlcPlayer.adjustVolumePanel();
    }
    public int getVolume() {
      try {
        return Math.max(0, (int)Math.round((Integer.parseInt(webBrowserObject.getObjectProperty("audio.volume")) - 1) / 1.99));
      } catch(Exception e) {
      }
      return -1;
    }
    public void setTrack(int track) {
      webBrowserObject.setObjectProperty("audio.track", track);
    }
    public int getTrack() {
      try {
        return Integer.parseInt(webBrowserObject.getObjectProperty("audio.track"));
      } catch(Exception e) {
      }
      return -1;
    }
    public enum VLCChannel {
      STEREO, REVERSE_STEREO, LEFT, RIGHT, DOLBY
    }
    public void setChannel(VLCChannel channel) {
      int value;
      switch(channel) {
        case STEREO: value = 1; break;
        case REVERSE_STEREO: value = 2; break;
        case LEFT: value = 3; break;
        case RIGHT: value = 4; break;
        case DOLBY: value = 5; break;
        default: throw new IllegalArgumentException("The channel value is invalid!");
      }
      webBrowserObject.setObjectProperty("audio.channel", value);
    }
    public VLCChannel getChannel() {
      try {
        int value = Integer.parseInt(webBrowserObject.getObjectProperty("audio.channel"));
        switch(value) {
          case 1: return VLCChannel.STEREO;
          case 2: return VLCChannel.REVERSE_STEREO;
          case 3: return VLCChannel.LEFT;
          case 4: return VLCChannel.RIGHT;
          case 5: return VLCChannel.DOLBY;
        }
      } catch(Exception e) {
      }
      return null;
    }
    public void toggleMute() {
      webBrowserObject.callObjectFunction("audio.toggleMute");
      vlcPlayer.adjustVolumePanel();
    }
  }
  
  private VLCAudio vlcAudio = new VLCAudio(this);
  
  public VLCAudio getVLCAudio() {
    return vlcAudio;
  }
  
  public static class VLCInput {
    private WebBrowserObject webBrowserObject;
    private VLCInput(JVLCPlayer vlcPlayer) {
      this.webBrowserObject = vlcPlayer.webBrowserObject;
    }
    /**
     * @return the length in milliseconds.
     */
    public int getLength() {
      try {
        return Integer.parseInt(webBrowserObject.getObjectProperty("input.length"));
      } catch(Exception e) {
      }
      return -1;
    }
    public float getFPS() {
      try {
        return Float.parseFloat(webBrowserObject.getObjectProperty("input.fps"));
      } catch(Exception e) {
      }
      return Float.NaN;
    }
    public boolean isVideoDisplayed() {
      return "true".equals(webBrowserObject.getObjectProperty("input.isVout"));
    }
    /**
     * @param position A value between 0.0 and 1.0.
     */
    public void setPosition(float position) {
      if(position < 0 || position > 1) {
        throw new IllegalArgumentException("The position must be between 0.0 and 1.0");
      }
      webBrowserObject.setObjectProperty("input.position", position);
    }
    public float getPosition() {
      try {
        return Float.parseFloat(webBrowserObject.getObjectProperty("input.position"));
      } catch(Exception e) {
      }
      return Float.NaN;
    }
    /**
     * @param time The time in milliseconds.
     */
    public void setTime(int time) {
      webBrowserObject.setObjectProperty("input.time", time);
    }
    /**
     * @return the time in milliseconds.
     */
    public int getTime() {
      try {
        return Integer.parseInt(webBrowserObject.getObjectProperty("input.time"));
      } catch(Exception e) {
      }
      return -1;
    }
    enum VLCState {
      IDLE_CLOSE, OPENING, BUFFERING, PLAYING, PAUSED, STOPPING, ERROR,
    }
    public void setState(VLCState state) {
      int value;
      switch(state) {
        case IDLE_CLOSE: value = 0; break;
        case OPENING: value = 1; break;
        case BUFFERING: value = 2; break;
        case PLAYING: value = 3; break;
        case PAUSED: value = 4; break;
        case STOPPING: value = 5; break;
        case ERROR: value = 6; break;
        default: throw new IllegalArgumentException("The state value is invalid!");
      }
      webBrowserObject.setObjectProperty("input.state", value);
    }
    public VLCState getState() {
      try {
        int value = Integer.parseInt(webBrowserObject.getObjectProperty("input.state"));
        switch(value) {
          case 0: return VLCState.IDLE_CLOSE;
          case 1: return VLCState.OPENING;
          case 2: return VLCState.BUFFERING;
          case 3: return VLCState.PLAYING;
          case 4: return VLCState.PAUSED;
          case 5: return VLCState.STOPPING;
          case 6: return VLCState.ERROR;
        }
      } catch(Exception e) {
      }
      return null;
    }
    public void setRate(float rate) {
      webBrowserObject.setObjectProperty("input.rate", rate);
    }
    public float getRate() {
      try {
        return Float.parseFloat(webBrowserObject.getObjectProperty("input.rate"));
      } catch(Exception e) {
      }
      return Float.NaN;
    }
  }
  
  private VLCInput vlcInput = new VLCInput(this);
  
  public VLCInput getVLCInput() {
    return vlcInput;
  }
  
  public static class VLCPlaylist {
    private JVLCPlayer vlcPlayer;
    private WebBrowserObject webBrowserObject;
    private VLCPlaylist(JVLCPlayer vlcPlayer) {
      this.vlcPlayer = vlcPlayer;
      this.webBrowserObject = vlcPlayer.webBrowserObject;
    }
    public int getItemCount() {
      try {
        return Integer.parseInt(webBrowserObject.getObjectProperty("playlist.items.count"));
      } catch(Exception e) {
      }
      return -1;
    }
    public boolean isPlaying() {
      return "true".equals(webBrowserObject.getObjectProperty("playlist.isPlaying"));
    }
    /**
     * @return the item ID, which can be used to add play or remove an item from the playlist.
     */
    public int add(String url) {
      if(!webBrowserObject.hasContent()) {
        vlcPlayer.load();
        clear();
      }
      File file = Utils.getLocalFile(url);
      if(file != null) {
        url = webBrowserObject.getLocalFileURL(file);
      }
      try {
        return Integer.parseInt(webBrowserObject.callObjectFunctionWithResult("playlist.add", url));
      } catch(Exception e) {
      }
      return -1;
    }
    public void play() {
      webBrowserObject.callObjectFunction("playlist.play");
    }
    public void playItem(int itemID) {
      webBrowserObject.callObjectFunction("playlist.playItem", itemID);
    }
    public void togglePause() {
      webBrowserObject.callObjectFunction("playlist.togglePause");
    }
    public void stop() {
      webBrowserObject.callObjectFunction("playlist.stop");
    }
    public void next() {
      webBrowserObject.callObjectFunction("playlist.next");
    }
    public void prev() {
      webBrowserObject.callObjectFunction("playlist.prev");
    }
    public void clear() {
      webBrowserObject.callObjectFunction("playlist.items.clear");
    }
    public void removeItem(int itemID) {
      webBrowserObject.callObjectFunction("playlist.items.removeItem", itemID);
    }
  }
  
  private VLCPlaylist vlcPlaylist = new VLCPlaylist(this);
  
  public VLCPlaylist getVLCPlaylist() {
    return vlcPlaylist;
  }
  
  public static class VLCVideo {
    private WebBrowserObject webBrowserObject;
    private VLCVideo(JVLCPlayer vlcPlayer) {
      this.webBrowserObject = vlcPlayer.webBrowserObject;
    }
    public int getWidth() {
      try {
        return Integer.parseInt(webBrowserObject.getObjectProperty("video.width"));
      } catch(Exception e) {
      }
      return -1;
    }
    public int getHeight() {
      try {
        return Integer.parseInt(webBrowserObject.getObjectProperty("video.height"));
      } catch(Exception e) {
      }
      return -1;
    }
    public void setFullScreen(boolean isFullScreen) {
      webBrowserObject.setObjectProperty("video.fullscreen", isFullScreen);
    }
    public boolean isFullScreen() {
      return "true".equals(webBrowserObject.getObjectProperty("video.fullscreen"));
    }
    public enum VLCAspectRatio {
      _1x1, _4x3, _16x9, _16x10, _221x100, _5x4,
    }
    public void setAspectRatio(VLCAspectRatio aspectRatio) {
      String value;
      switch(aspectRatio) {
        case _1x1: value = "1:1"; break;
        case _4x3: value = "4:3"; break;
        case _16x9: value = "16:9"; break;
        case _16x10: value = "16:10"; break;
        case _221x100: value = "221:100"; break;
        case _5x4: value = "5:4"; break;
        default: throw new IllegalArgumentException("The aspect ratio value is invalid!");
      }
      webBrowserObject.setObjectProperty("video.aspectRatio", value);
    }
    public VLCAspectRatio getAspectRatio() {
      String value = webBrowserObject.getObjectProperty("video.aspectRatio");
      if("1:1".equals(value)) return VLCAspectRatio._1x1;
      if("4:3".equals(value)) return VLCAspectRatio._4x3;
      if("16:9".equals(value)) return VLCAspectRatio._16x9;
      if("16:10".equals(value)) return VLCAspectRatio._16x10;
      if("221:100".equals(value)) return VLCAspectRatio._221x100;
      if("5:4".equals(value)) return VLCAspectRatio._5x4;
      return null;
    }
    /**
     * @param subtitleTrack The track of the subtitles, or 0 to disable it.
     */
    public void setSubtitleTrack(int subtitleTrack) {
      webBrowserObject.setObjectProperty("video.subtitle", subtitleTrack);
    }
    /**
     * @return the track of the subtitles, or 0 if disabled.
     */
    public int getSubtitleTrack() {
      try {
        return Integer.parseInt(webBrowserObject.getObjectProperty("video.subtitle"));
      } catch(Exception e) {
      }
      return -1;
    }
    public void toggleFullScreen() {
      webBrowserObject.callObjectFunction("video.toggleFullscreen");
    }
  }
  
  private VLCVideo vlcVideo = new VLCVideo(this);
  
  public VLCVideo getVLCVideo() {
    return vlcVideo;
  }
  
  /**
   * Run a command in sequence with other calls from this class. Calls are performed only when the component is loaded, and this method adds to the queue of calls in case it is not.
   */
  public void run(Runnable runnable) {
    webBrowser.run(runnable);
  }
  
  /**
   * Forces the component to initialize. All method calls will then be synchronous instead of being queued waiting for the componant to be initialized.
   * This call fails if the component is not in a component hierarchy with a Window ancestor.
   */
  public void initializeNativePeer() {
    webBrowser.initializeNativePeer();
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
