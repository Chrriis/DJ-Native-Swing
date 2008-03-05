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
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import chrriis.common.Disposable;
import chrriis.common.Utils;
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
    super.removeNotify();
    stopUpdateThread();
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
    updateThread = new Thread("NativeSwing - VLC Player seek bar update") {
      @Override
      public void run() {
        while(this == updateThread) {
          try {
            sleep(1000);
          } catch(Exception e) {}
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              VLCInput vlcInput = getVLCInput();
              VLCState state = vlcInput.getState();
              boolean isValid = state == VLCState.OPENING || state == VLCState.BUFFERING || state == VLCState.PLAYING || state == VLCState.PAUSED || state == VLCState.STOPPING;
              if(isValid) {
                float position = vlcInput.getPosition();
                isValid = !Float.isNaN(position);
                if(position == 0 && vlcInput.getLength() == 0) {
                  isValid = false;
                }
                if(seekBarSlider.isVisible() != isValid) {
                  seekBarSlider.setVisible(isValid);
                }
                if(isValid) {
                  isAdjustingSeekBar = true;
                  seekBarSlider.setValue(Math.round(position * 10000));
                  isAdjustingSeekBar = false;
                }
              } else {
                if(seekBarSlider.isVisible() != isValid) {
                  seekBarSlider.setVisible(isValid);
                }
              }
            }
          });
        }
      }
    };
    updateThread.setDaemon(true);
    updateThread.start();
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
    JPanel volumePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
    volumeButton = new JButton();
    Insets margin = volumeButton.getMargin();
    margin.left = Math.min(2, margin.left);
    margin.right = Math.min(2, margin.left);
    volumeButton.setMargin(margin);
    volumeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getVLCAudio().setMute(!getVLCAudio().isMute());
        adjustVolumePanel();
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
    JPanel glue = new JPanel();
    glue.setPreferredSize(new Dimension(volumePanel.getPreferredSize().width, 0));
    gridBag.setConstraints(glue, cons);
    buttonBarPanel.add(glue);
    cons.gridx++;
    cons.fill = GridBagConstraints.HORIZONTAL;
    cons.weightx = 1.0;
    gridBag.setConstraints(buttonPanel, cons);
    buttonBarPanel.add(buttonPanel);
    cons.gridx++;
    cons.weightx = 0.0;
    cons.fill = GridBagConstraints.NONE;
    volumePanel.setMaximumSize(volumePanel.getPreferredSize());
    volumePanel.setMinimumSize(volumePanel.getPreferredSize());
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
  
  public String getURL() {
    return webBrowserObject.getURL();
  }
  
  /**
   * The player is initialized after a call to setURL() or to initialize().
   */
  public void setURL(String url) {
    setURL(url, null);
  }
  
  /**
   * The player is initialized after a call to setURL() or to initialize().
   */
  public void initialize() {
    setURL_("", null);
  }
  
  private VLCLoadingOptions loadingOptions;
  
  /**
   * The player is initialized after a call to setURL() or to initialize().
   */
  public void setURL(String url, VLCLoadingOptions loadingOptions) {
    if("".equals(url)) {
      url = null;
    }
    setURL_(url, loadingOptions);
  }
  
  private void setURL_(String url, VLCLoadingOptions loadingOptions) {
    if(loadingOptions == null) {
      loadingOptions = new VLCLoadingOptions();
    }
    this.loadingOptions = loadingOptions;
    webBrowserObject.setURL(url);
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
    private JWebBrowser webBrowser;
    private VLCAudio(JVLCPlayer vlcPlayer) {
      this.vlcPlayer = vlcPlayer;
      this.webBrowser = vlcPlayer.webBrowser;
    }
    public void setMute(boolean isMute) {
      webBrowser.execute("getEmbeddedObject().audio.mute = " + isMute + ";");
      vlcPlayer.adjustVolumePanel();
    }
    public boolean isMute() {
      String commnand = "isMute";
      return "true".equals(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().audio.mute);"));
    }
    public void setVolume(int volume) {
      if(volume < 0 || volume > 100) {
        throw new IllegalArgumentException("The volume must be between 0 and 100");
      }
      webBrowser.execute("getEmbeddedObject().audio.volume = " + Math.round((volume * 1.99 + 1)) + ";");
      vlcPlayer.adjustVolumePanel();
    }
    public int getVolume() {
      String commnand = "getVolume";
      try {
        return Math.max(0, (int)Math.round((Integer.parseInt(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().audio.volume);")) - 1) / 1.99));
      } catch(Exception e) {
      }
      return -1;
    }
    public void setTrack(int track) {
      webBrowser.execute("getEmbeddedObject().audio.track = " + track + ";");
    }
    public int getTrack() {
      String commnand = "getTrack";
      try {
        return Integer.parseInt(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().audio.track);"));
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
      webBrowser.execute("getEmbeddedObject().audio.channel = " + value + ";");
    }
    public VLCChannel getChannel() {
      String commnand = "getChannel";
      try {
        int value = Integer.parseInt(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().audio.channel);"));
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
  }
  
  private VLCAudio vlcAudio = new VLCAudio(this);
  
  public VLCAudio getVLCAudio() {
    return vlcAudio;
  }
  
  public static class VLCInput {
    private JWebBrowser webBrowser;
    private VLCInput(JWebBrowser webBrowser) {
      this.webBrowser = webBrowser;
    }
    /**
     * @return the length in milliseconds.
     */
    public int getLength() {
      String commnand = "getLength";
      try {
        return Integer.parseInt(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().input.length);"));
      } catch(Exception e) {
      }
      return -1;
    }
    public float getFPS() {
      String commnand = "getFPS";
      try {
        return Float.parseFloat(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().input.fps);"));
      } catch(Exception e) {
      }
      return Float.NaN;
    }
    public boolean isVideoDisplayed() {
      String commnand = "isVideoDisplayed";
      return "true".equals(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().input.isVout);"));
    }
    /**
     * @param position A value between 0.0 and 1.0.
     */
    public void setPosition(float position) {
      if(position < 0 || position > 1) {
        throw new IllegalArgumentException("The position must be between 0.0 and 1.0");
      }
      webBrowser.execute("getEmbeddedObject().input.position = " + position + ";");
    }
    public float getPosition() {
      String commnand = "getPosition";
      try {
        return Float.parseFloat(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().input.position);"));
      } catch(Exception e) {
      }
      return Float.NaN;
    }
    /**
     * @param time The time in milliseconds.
     */
    public void setTime(int time) {
      webBrowser.execute("getEmbeddedObject().input.time = " + time + ";");
    }
    /**
     * @return the time in milliseconds.
     */
    public int getTime() {
      String commnand = "getTime";
      try {
        return Integer.parseInt(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().input.time);"));
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
      webBrowser.execute("getEmbeddedObject().input.state = " + value + ";");
    }
    public VLCState getState() {
      String commnand = "getState";
      try {
        int value = Integer.parseInt(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().input.state);"));
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
      webBrowser.execute("getEmbeddedObject().input.rate = " + rate + ";");
    }
    public float getRate() {
      String commnand = "getRate";
      try {
        return Float.parseFloat(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().input.rate);"));
      } catch(Exception e) {
      }
      return Float.NaN;
    }
  }
  
  private VLCInput vlcInput = new VLCInput(webBrowser);
  
  public VLCInput getVLCInput() {
    return vlcInput;
  }
  
  public class VLCPlaylist {
    private VLCPlaylist() {}
    public int getItemCount() {
      String commnand = "getItemCount";
      try {
        return Integer.parseInt(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().playlist.itemCount);"));
      } catch(Exception e) {
      }
      return -1;
    }
    public boolean isPlaying() {
      String commnand = "isPlaying";
      return "true".equals(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().playlist.isPlaying);"));
    }
    public void add(String url) {
      if(!webBrowserObject.hasContent()) {
        initialize();
      }
      File file = Utils.getLocalFile(url);
      if(file != null) {
        url = webBrowserObject.getLocalFileURL(file);
      }
      webBrowser.execute("getEmbeddedObject().playlist.add(decodeURIComponent('" + Utils.encodeURL(url) + "'));");
    }
    public void play() {
      webBrowser.execute("getEmbeddedObject().playlist.play();");
    }
    public void togglePause() {
      webBrowser.execute("getEmbeddedObject().playlist.togglePause();");
    }
    public void stop() {
      webBrowser.execute("getEmbeddedObject().playlist.stop();");
    }
    public void next() {
      webBrowser.execute("getEmbeddedObject().playlist.next();");
    }
    public void prev() {
      webBrowser.execute("getEmbeddedObject().playlist.prev();");
    }
    public void clear() {
      webBrowser.execute("getEmbeddedObject().playlist.clear();");
    }
    public void removeItem(int item) {
      webBrowser.execute("getEmbeddedObject().playlist.removeItem(" + item + ");");
    }
  }
  
  private VLCPlaylist vlcPlaylist = new VLCPlaylist();
  
  public VLCPlaylist getVLCPlaylist() {
    return vlcPlaylist;
  }
  
  public static class VLCVideo {
    private JWebBrowser webBrowser;
    private VLCVideo(JWebBrowser webBrowser) {
      this.webBrowser = webBrowser;
    }
    public int getWidth() {
      String commnand = "getWidth";
      try {
        return Integer.parseInt(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().video.width);"));
      } catch(Exception e) {
      }
      return -1;
    }
    public int getHeight() {
      String commnand = "getHeight";
      try {
        return Integer.parseInt(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().video.height);"));
      } catch(Exception e) {
      }
      return -1;
    }
    public void setFullScreen(boolean isFullScreen) {
      webBrowser.execute("getEmbeddedObject().video.fullscreen = " + isFullScreen + ";");
    }
    public boolean isFullScreen() {
      String commnand = "isFullScreen";
      return "true".equals(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().video.fullscreen);"));
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
      webBrowser.execute("getEmbeddedObject().video.aspectRatio = " + value + ";");
    }
    public VLCAspectRatio getAspectRatio() {
      String commnand = "getAspectRatio";
      String value = webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().video.aspectRatio);");
      if("1:1".equals(value)) return VLCAspectRatio._1x1;
      if("4:3".equals(value)) return VLCAspectRatio._4x3;
      if("16:9".equals(value)) return VLCAspectRatio._16x9;
      if("16:10".equals(value)) return VLCAspectRatio._16x10;
      if("221:100".equals(value)) return VLCAspectRatio._221x100;
      if("5:4".equals(value)) return VLCAspectRatio._5x4;
      return null;
    }
    public void toggleFullScreen() {
      webBrowser.execute("getEmbeddedObject().video.toggleFullscreen();");
    }
  }
  
  private VLCVideo vlcVideo = new VLCVideo(webBrowser);
  
  public VLCVideo getVLCVideo() {
    return vlcVideo;
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
