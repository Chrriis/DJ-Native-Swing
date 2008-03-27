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

import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.dj.nativeswing.NSComponent;
import chrriis.dj.nativeswing.WebBrowserObject;
import chrriis.dj.nativeswing.components.JVLCPlayer.VLCInput.VLCMediaState;

/**
 * A native multimedia player. It is a browser-based component, which relies on the VLC plugin.<br/>
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fail, the methods will not have any effect. The results from methods have relevant values only when the component is valid. 
 * @author Christopher Deckers
 */
public class JVLCPlayer extends JPanel implements NSComponent {

  public static class VLCLoadingOptions {
    
    private Map<String, String> keyToValueParameterMap = new HashMap<String, String>();

    /**
     * Get the VLC plugin HTML parameters.
     * @return the parameters.
     */
    public Map<String, String> getParameters() {
      return keyToValueParameterMap;
    }
    
    /**
     * Set the VLC HTML parameters that will be used when the plugin is created.
     * @param keyToValueParameterMap the map of key/value pairs.
     */
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
              VLCMediaState state = vlcInput.getMediaState();
              boolean isValid = state == VLCMediaState.OPENING || state == VLCMediaState.BUFFERING || state == VLCMediaState.PLAYING || state == VLCMediaState.PAUSED || state == VLCMediaState.STOPPING;
              if(isValid) {
                int time = vlcInput.getAbsolutePosition();
                int length = vlcInput.getMediaLength();
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
    load((VLCLoadingOptions)null);
  }
  
  /**
   * Load a file.
   * @param resourcePath the path or URL to the file.
   */
  public void load(String resourcePath) {
    load(resourcePath, null);
  }
  
  /**
   * Load the player, with no content.
   * @param loadingOptions the options to better configure the initialization of the VLC plugin.
   */
  public void load(VLCLoadingOptions loadingOptions) {
    load("", loadingOptions);
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
   * @param loadingOptions the options to better configure the initialization of the VLC plugin.
   */
  public void load(Class<?> clazz, String resourcePath, VLCLoadingOptions loadingOptions) {
    load(WebServer.getDefaultWebServer().getClassPathResourceURL(clazz.getName(), resourcePath), loadingOptions);
  }
  
  private VLCLoadingOptions loadingOptions;
  
  /**
   * Load a file.
   * @param resourcePath the path or URL to the file.
   * @param loadingOptions the options to better configure the initialization of the VLC plugin.
   */
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

  /**
   * Indicate whether the control bar is visible.
   * @return true if the control bar is visible.
   */
  public boolean isControlBarVisible() {
    return controlBarPane.isVisible();
  }
  
  /**
   * Set whether the control bar is visible.
   * @param isButtonBarVisible true if the control bar should be visible, false otherwise.
   */
  public void setControlBarVisible(boolean isVisible) {
    controlBarPane.setVisible(isVisible);
    adjustBorder();
  }
  
  public void disposeNativePeer() {
    webBrowserObject.disposeNativePeer();
  }
  
  public boolean isNativePeerDisposed() {
    return webBrowserObject.isNativePeerDisposed();
  }
  
  /* ------------------------- VLC API exposed ------------------------- */
  
  public static class VLCAudio {
    private JVLCPlayer vlcPlayer;
    private WebBrowserObject webBrowserObject;
    private VLCAudio(JVLCPlayer vlcPlayer) {
      this.vlcPlayer = vlcPlayer;
      this.webBrowserObject = vlcPlayer.webBrowserObject;
    }
    /**
     * Set whether audio is mute.
     * @param isMute true if audio should be mute, false otherwise.
     */
    public void setMute(boolean isMute) {
      webBrowserObject.setObjectProperty("audio.mute", isMute);
      vlcPlayer.adjustVolumePanel();
    }
    /**
     * Indicate whether audio is mute.
     * @return true if audio is mute.
     */
    public boolean isMute() {
      return Boolean.TRUE.equals(webBrowserObject.getObjectProperty("audio.mute"));
    }
    /**
     * Set the volume.
     * @param volume the new volume, with a value between 0 and 100. Note that a value of 0 may not make it completely silent, mute should be used instead.
     */
    public void setVolume(int volume) {
      if(volume < 0 || volume > 100) {
        throw new IllegalArgumentException("The volume must be between 0 and 100");
      }
      webBrowserObject.setObjectProperty("audio.volume", Math.round((volume * 1.99 + 1)));
      vlcPlayer.adjustVolumePanel();
    }
    /**
     * Get the volume, as a value between 0 and 100.
     * @return the volume.
     */
    public int getVolume() {
      Object value = webBrowserObject.getObjectProperty("audio.volume");
      return value == null? -1: Math.max(0, (int)Math.round((((Number)value).intValue() - 1) / 1.99));
    }
    /**
     * Set the audio track, or 0 to disable it.
     * @param track the track to play.
     */
    public void setTrack(int track) {
      webBrowserObject.setObjectProperty("audio.track", track);
    }
    /**
     * Get the audio track, or 0 if disabled.
     * @return the audio track.
     */
    public int getTrack() {
      Object value = webBrowserObject.getObjectProperty("audio.track");
      return value == null? -1: ((Number)value).intValue();
    }
    public enum VLCChannel {
      STEREO, REVERSE_STEREO, LEFT, RIGHT, DOLBY
    }
    /**
     * Set the audio channel to use.
     * @param channel the audio channel to use.
     */
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
    /**
     * Get the audio channel.
     * @return the audio channel.
     */
    public VLCChannel getChannel() {
      Object value = webBrowserObject.getObjectProperty("audio.channel");
      if(value == null) {
        return null;
      }
      switch(((Number)value).intValue()) {
        case 1: return VLCChannel.STEREO;
        case 2: return VLCChannel.REVERSE_STEREO;
        case 3: return VLCChannel.LEFT;
        case 4: return VLCChannel.RIGHT;
        case 5: return VLCChannel.DOLBY;
      }
      return null;
    }
    /**
     * Toggle the mute state.
     */
    public void toggleMute() {
      webBrowserObject.invokeObjectFunction("audio.toggleMute");
      vlcPlayer.adjustVolumePanel();
    }
  }
  
  private VLCAudio vlcAudio = new VLCAudio(this);
  
  /**
   * Get the VLC object responsible for audio-related actions.
   * @return the VLC audio object.
   */
  public VLCAudio getVLCAudio() {
    return vlcAudio;
  }
  
  public static class VLCInput {
    private WebBrowserObject webBrowserObject;
    private VLCInput(JVLCPlayer vlcPlayer) {
      this.webBrowserObject = vlcPlayer.webBrowserObject;
    }
    /**
     * Get the length in milliseconds of the current media.
     * @return the length in milliseconds.
     */
    public int getMediaLength() {
      Object value = webBrowserObject.getObjectProperty("input.length");
      return value == null? -1: ((Number)value).intValue();
    }
    /**
     * Get the number of frames per second.
     * @return the number of frames per second.
     */
    public float getFrameRate() {
      Object value = webBrowserObject.getObjectProperty("input.fps");
      return value == null? Float.NaN: ((Number)value).floatValue();
    }
    /**
     * Indicate if a video is currently displayed.
     * @return true if a video is displayed.
     */
    public boolean isVideoDisplayed() {
      return Boolean.TRUE.equals(webBrowserObject.getObjectProperty("input.isVout"));
    }
    /**
     * Set the current relative position on the timeline.
     * @param position A value between 0 and 1.
     */
    public void setRelativePosition(float position) {
      if(position >= 0 && position <= 1) {
        webBrowserObject.setObjectProperty("input.position", position);
        return;
      }
      throw new IllegalArgumentException("The position must be between 0 and 1");
    }
    /**
     * Get the current relative position on the timeline as a float between 0 and 1.
     * @return the current relative position, or Float.NaN if not available.
     */
    public float getRelativePosition() {
      Object value = webBrowserObject.getObjectProperty("input.position");
      return value == null? Float.NaN: ((Number)value).floatValue();
    }
    /**
     * Set the current position on the timeline.
     * @param time The current position in milliseconds.
     */
    public void setAbsolutePosition(int time) {
      webBrowserObject.setObjectProperty("input.time", time);
    }
    /**
     * Get the current position on the timeline.
     * @return the current position in milliseconds.
     */
    public int getAbsolutePosition() {
      Object value = webBrowserObject.getObjectProperty("input.time");
      return value == null? -1: ((Number)value).intValue();
    }
    public enum VLCMediaState {
      IDLE_CLOSE, OPENING, BUFFERING, PLAYING, PAUSED, STOPPING, ERROR,
    }
    /**
     * Get the state.
     * @return the state.
     */
    public VLCMediaState getMediaState() {
      Object value = webBrowserObject.getObjectProperty("input.state");
      if(value == null) {
        return null;
      }
      switch(((Number)value).intValue()) {
        case 0: return VLCMediaState.IDLE_CLOSE;
        case 1: return VLCMediaState.OPENING;
        case 2: return VLCMediaState.BUFFERING;
        case 3: return VLCMediaState.PLAYING;
        case 4: return VLCMediaState.PAUSED;
        case 5: return VLCMediaState.STOPPING;
        case 6: return VLCMediaState.ERROR;
      }
      return null;
    }
    /**
     * Set the speed factor that is applied when a media is played.
     * @param speedFactor the speed factor.
     */
    public void setPlayRate(float speedFactor) {
      webBrowserObject.setObjectProperty("input.rate", speedFactor);
    }
    /**
     * Get the speed factor that is applied when a media is played.
     * @return the speed factor.
     */
    public float getPlaySpeedFactor() {
      Object value = webBrowserObject.getObjectProperty("input.rate");
      return value == null? Float.NaN: ((Number)value).floatValue();
    }
  }
  
  private VLCInput vlcInput = new VLCInput(this);
  
  /**
   * Get the VLC object responsible for input-related actions.
   * @return the VLC input object.
   */
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
    /**
     * Get the number of items in the playlist.
     * @return the item count.
     */
    public int getItemCount() {
      Object value = webBrowserObject.getObjectProperty("playlist.items.count");
      return value == null? -1: ((Number)value).intValue();
    }
    /**
     * Indicate whether the playlist is currently playing an item.
     * @return true if an item is being played.
     */
    public boolean isPlaying() {
      return Boolean.TRUE.equals(webBrowserObject.getObjectProperty("playlist.isPlaying"));
    }
    /**
     * Add an item from the classpath to the playlist and get its ID for future manipulation.
     * @param clazz the reference clazz of the file to load.
     * @param resourcePath the path to the file.
     */
    public int addItem(Class<?> clazz, String resourcePath) {
      return addItem(WebServer.getDefaultWebServer().getClassPathResourceURL(clazz.getName(), resourcePath));
    }
    /**
     * Add an item to the playlist and get its ID for future manipulation.
     * @param resourcePath the path or URL to the file.
     * @return the item ID, which can be used to add play or remove an item from the playlist.
     */
    public int addItem(String resourcePath) {
      if(!webBrowserObject.hasContent()) {
        vlcPlayer.load();
        clear();
      }
      File file = Utils.getLocalFile(resourcePath);
      if(file != null) {
        resourcePath = webBrowserObject.getLocalFileURL(file);
      }
      Object value = webBrowserObject.invokeObjectFunctionWithResult("playlist.add", resourcePath);
      return value == null? -1: ((Number)value).intValue();
    }
    /**
     * Start playing the playlist.
     */
    public void play() {
      webBrowserObject.invokeObjectFunction("playlist.play");
    }
    /**
     * Start playing an item from the playlist using its ID.
     * @param itemID the ID of the item to play.
     */
    public void playItem(int itemID) {
      webBrowserObject.invokeObjectFunction("playlist.playItem", itemID);
    }
    /**
     * Toggle the pause state.
     */
    public void togglePause() {
      webBrowserObject.invokeObjectFunction("playlist.togglePause");
    }
    /**
     * Stop playing.
     */
    public void stop() {
      webBrowserObject.invokeObjectFunction("playlist.stop");
    }
    /**
     * Move to the next item of the playlist.
     */
    public void next() {
      webBrowserObject.invokeObjectFunction("playlist.next");
    }
    /**
     * Move to the previous item of the playlist.
     */
    public void prev() {
      webBrowserObject.invokeObjectFunction("playlist.prev");
    }
    /**
     * Clear the playlist.
     */
    public void clear() {
      webBrowserObject.invokeObjectFunction("playlist.items.clear");
    }
    /**
     * Remove an item using its ID.
     * @param itemID the ID of the item.
     */
    public void removeItem(int itemID) {
      webBrowserObject.invokeObjectFunction("playlist.items.removeItem", itemID);
    }
  }
  
  private VLCPlaylist vlcPlaylist = new VLCPlaylist(this);
  
  /**
   * Get the VLC object responsible for playlist-related actions.
   * @return the VLC playlist object.
   */
  public VLCPlaylist getVLCPlaylist() {
    return vlcPlaylist;
  }
  
  public static class VLCVideo {
    private WebBrowserObject webBrowserObject;
    private VLCVideo(JVLCPlayer vlcPlayer) {
      this.webBrowserObject = vlcPlayer.webBrowserObject;
    }
    /**
     * Get the width of the video.
     * @return the width.
     */
    public int getWidth() {
      Object value = webBrowserObject.getObjectProperty("video.width");
      return value == null? -1: ((Number)value).intValue();
    }
    /**
     * Get the height of the video.
     * @return the height.
     */
    public int getHeight() {
      Object value = webBrowserObject.getObjectProperty("video.height");
      return value == null? -1: ((Number)value).intValue();
    }
    /**
     * Set whether the video is playing in full screen mode.
     * @param true if the full screen mode should be active, false otherwise.
     */
    public void setFullScreen(boolean isFullScreen) {
      webBrowserObject.setObjectProperty("video.fullscreen", isFullScreen);
    }
    /**
     * Indicate whether the video is in full screen mode.
     * @return true if the video is in full screen mode.
     */
    public boolean isFullScreen() {
      return Boolean.TRUE.equals(webBrowserObject.getObjectProperty("video.fullscreen"));
    }
    public enum VLCAspectRatio {
      _1x1, _4x3, _16x9, _16x10, _221x100, _5x4,
    }
    /**
     * Set the aspect ration of the video.
     * @param aspectRatio the aspect ratio.
     */
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
    /**
     * Get the aspect ratio of the video media.
     * @return the aspect ratio.
     */
    public VLCAspectRatio getAspectRatio() {
      String value = (String)webBrowserObject.getObjectProperty("video.aspectRatio");
      if("1:1".equals(value)) return VLCAspectRatio._1x1;
      if("4:3".equals(value)) return VLCAspectRatio._4x3;
      if("16:9".equals(value)) return VLCAspectRatio._16x9;
      if("16:10".equals(value)) return VLCAspectRatio._16x10;
      if("221:100".equals(value)) return VLCAspectRatio._221x100;
      if("5:4".equals(value)) return VLCAspectRatio._5x4;
      return null;
    }
    /**
     * Set the track of the subtitles.
     * @param subtitleTrack The track of the subtitles, or 0 to disable them.
     */
    public void setSubtitleTrack(int subtitleTrack) {
      webBrowserObject.setObjectProperty("video.subtitle", subtitleTrack);
    }
    /**
     * Get the track of the subtitles.
     * @return the track of the subtitles, or 0 if disabled.
     */
    public int getSubtitleTrack() {
      Object value = webBrowserObject.getObjectProperty("video.subtitle");
      return value == null? -1: ((Number)value).intValue();
    }
    /**
     * Toggle full screen mode.
     */
    public void toggleFullScreen() {
      webBrowserObject.invokeObjectFunction("video.toggleFullscreen");
    }
  }
  
  private VLCVideo vlcVideo = new VLCVideo(this);
  
  /**
   * Get the VLC object responsible for video-related actions.
   * @return the VLC video object.
   */
  public VLCVideo getVLCVideo() {
    return vlcVideo;
  }
  
  public void runInSequence(Runnable runnable) {
    webBrowser.runInSequence(runnable);
  }
  
  public void initializeNativePeer() {
    webBrowser.initializeNativePeer();
  }
  
  public boolean isNativePeerInitialized() {
    return webBrowser.isNativePeerInitialized();
  }
  
  public boolean isNativePeerValid() {
    return webBrowser.isNativePeerValid();
  }
  
}
