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
  
  private JSlider slider;
  private volatile boolean isAdjusting;
  private volatile Thread updateThread;

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
              float position = getVLCInput().getPosition();
              boolean isValid = !Float.isNaN(position);
              if(position == 0 && getVLCInput().getLength() == 0) {
                isValid = false;
              }
              if(slider.isVisible() != isValid) {
                slider.setVisible(isValid);
              }
              if(isValid) {
                isAdjusting = true;
                slider.setValue(Math.round(position * 10000));
                isAdjusting = false;
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
    slider = new JSlider(0, 10000, 0);
    slider.setVisible(false);
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if(!isAdjusting) {
          getVLCInput().setPosition(((float)slider.getValue()) / 10000);
        }
      }
    });
    controlBarPane.add(slider, BorderLayout.NORTH);
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
    playButton = new JButton(createIcon("PlayIcon"));
    playButton.setToolTipText(RESOURCES.getString("PlayText"));
    playButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getVLCPlaylist().play();
      }
    });
    buttonPanel.add(playButton);
    pauseButton = new JButton(createIcon("PauseIcon"));
    pauseButton.setToolTipText(RESOURCES.getString("PauseText"));
    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getVLCPlaylist().togglePause();
      }
    });
    buttonPanel.add(pauseButton);
    stopButton = new JButton(createIcon("StopIcon"));
    stopButton.setToolTipText(RESOURCES.getString("StopText"));
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getVLCPlaylist().stop();
      }
    });
    buttonPanel.add(stopButton);
    controlBarPane.add(buttonPanel, BorderLayout.CENTER);
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
    startUpdateThread();
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
  
  public void addInitializationListener(InitializationListener listener) {
    webBrowserObject.addInitializationListener(listener);
  }
  
  public void removeInitializationListener(InitializationListener listener) {
    webBrowserObject.removeInitializationListener(listener);
  }
  
  public InitializationListener[] getInitializationListeners() {
    return webBrowserObject.getInitializationListeners();
  }

  /* ------------------------- VLC API exposed ------------------------- */
  
  public static class VLCAudio {
    private JWebBrowser webBrowser;
    private VLCAudio(JWebBrowser webBrowser) {}
    public void setMute(boolean isMute) {
      webBrowser.execute("getEmbeddedObject().audio.mute = " + isMute + ";");
    }
    public boolean isMute() {
      String commnand = "isMute";
      return "true".equals(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().audio.mute);"));
    }
    public void setVolume(int volume) {
      if(volume < 0 || volume > 100) {
        throw new IllegalArgumentException("The volume must be between 0 and 100");
      }
      webBrowser.execute("getEmbeddedObject().audio.volume = " + (volume * 2) + ";");
    }
    public int getVolume() {
      String commnand = "getVolume";
      try {
        return Integer.parseInt(webBrowser.executeAndWaitForCommandResult(commnand, "sendCommand('" + commnand + ":' + getEmbeddedObject().audio.volume);")) / 2;
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
  
  private VLCAudio vlcAudio = new VLCAudio(webBrowser);
  
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
  
}
