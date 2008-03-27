/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components.win32;

import java.awt.BorderLayout;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import chrriis.dj.nativeswing.InitializationEvent;
import chrriis.dj.nativeswing.InitializationListener;
import chrriis.dj.nativeswing.NSPanelComponent;
import chrriis.dj.nativeswing.NativeComponent;

/**
 * A multimedia player, based on the Window Media Player (only avaialable on the Windows operating system).<br/>
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fail, the methods will not have any effect. The results from methods have relevant values only when the component is valid. 
 * @author Christopher Deckers
 */
public class JWMediaPlayer extends NSPanelComponent {

  private NativeWMediaPlayer nativeComponent;
  
  private static class NInitializationListener implements InitializationListener {
    private Reference<JWMediaPlayer> multiMediaPlayer;
    private NInitializationListener(JWMediaPlayer multiMediaPlayer) {
      this.multiMediaPlayer = new WeakReference<JWMediaPlayer>(multiMediaPlayer);
    }
    public void objectInitialized(InitializationEvent e) {
      JWMediaPlayer multiMediaPlayer = this.multiMediaPlayer.get();
      if(multiMediaPlayer == null) {
        return;
      }
      Object[] listeners = multiMediaPlayer.listenerList.getListenerList();
      e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == InitializationListener.class) {
          if(e == null) {
            e = new InitializationEvent(multiMediaPlayer);
          }
          ((InitializationListener)listeners[i + 1]).objectInitialized(e);
        }
      }
    }
  }
  
  public JWMediaPlayer() {
    nativeComponent = new NativeWMediaPlayer();
    initialize(nativeComponent);
    wmpSettings = new WMPSettings(this);
    wmpControls = new WMPControls(this);
    nativeComponent.addInitializationListener(new NInitializationListener(this));
    add(nativeComponent.createEmbeddableComponent(), BorderLayout.CENTER);
    wmpSettings.setAutoStart(true);
    wmpSettings.setErrorDialogsEnabled(false);
    setControlBarVisible(true);
  }
  
  public NativeComponent getNativeComponent() {
    return nativeComponent;
  }
  
  public static class WMPSettings {
    
    private NativeWMediaPlayer nativeComponent;
    
    private WMPSettings(JWMediaPlayer multiMediaPlayer) {
      this.nativeComponent = multiMediaPlayer.nativeComponent;
    }
    
    public void setErrorDialogsEnabled(boolean isErrorDialogEnabled) {
      nativeComponent.setOleProperty(new String[] {"settings", "enableErrorDialogs"}, isErrorDialogEnabled);
    }
    
    public void setVolume(int volume) {
      if(volume < 0 || volume > 100) {
        throw new IllegalArgumentException("The volume must be between 0 and 100");
      }
      nativeComponent.setOleProperty(new String[] {"settings", "volume"}, volume);
    }
    
    /**
     * @return The volume, between 0 and 100. When mute, the volume is still returned. -1 indicate that it could not be accessed.
     */
    public int getVolume() {
      try {
        return (Integer)nativeComponent.getOleProperty(new String[] {"settings", "volume"});
      } catch(Exception e) {
        return -1;
      }
    }
    
    /**
     * @param stereoBalance The stereo balance between -100 and 100, with 0 being the default.
     */
    public void setStereoBalance(int stereoBalance) {
      if(stereoBalance < 100 || stereoBalance > 100) {
        throw new IllegalArgumentException("The stereo balance must be between -100 and 100");
      }
      nativeComponent.setOleProperty(new String[] {"settings", "balance"}, stereoBalance);
    }
    
    /**
     * @return The stereo balance, between -100 and 100, with 0 being the default. When mute, the balance is still returned.
     */
    public int getStereoBalance() {
      try {
        return (Integer)nativeComponent.getOleProperty(new String[] {"settings", "balance"});
      } catch(Exception e) {
        return -1;
      }
    }
    
    public void setAutoStart(boolean isAutoStart) {
      nativeComponent.setOleProperty(new String[] {"settings", "autoStart"}, isAutoStart);
    }
    
    public boolean isAutoStart() {
      return Boolean.TRUE.equals(nativeComponent.getOleProperty(new String[] {"settings", "autoStart"}));
    }
    
    public void setMute(boolean isMute) {
      nativeComponent.setOleProperty(new String[] {"settings", "mute"}, isMute);
    }
    
    public boolean isMute() {
      return Boolean.TRUE.equals(nativeComponent.getOleProperty(new String[] {"settings", "mute"}));
    }
    
    public boolean isPlayEnabled() {
      return Boolean.TRUE.equals(nativeComponent.getOleProperty(new String[] {"controls", "isAvailable"}, "Play"));
    }
    
  }
  
  private WMPSettings wmpSettings;
  
  public WMPSettings getWMPSettings() {
    return wmpSettings;
  }

  public static class WMPControls {
    
    private NativeWMediaPlayer nativeComponent;
    
    private WMPControls(JWMediaPlayer multiMediaPlayer) {
      this.nativeComponent = multiMediaPlayer.nativeComponent;
    }
    
    public void play() {
      nativeComponent.invokeOleFunction(new String[] {"controls", "Play"});
    }
    
    public boolean isStopEnabled() {
      return Boolean.TRUE.equals(nativeComponent.getOleProperty(new String[] {"controls", "isAvailable"}, "Stop"));
    }
    
    public void stop() {
      nativeComponent.invokeOleFunction(new String[] {"controls", "Stop"});
    }
    
    public boolean isPauseEnabled() {
      return Boolean.TRUE.equals(nativeComponent.getOleProperty(new String[] {"controls", "isAvailable"}, "Pause"));
    }
    
    public void pause() {
      nativeComponent.invokeOleFunction(new String[] {"controls", "Pause"});
    }
    
    /**
     * @param time The time in milliseconds.
     */
    public void setTime(int time) {
      nativeComponent.setOleProperty(new String[] {"controls", "currentPosition"}, time / 1000d);
    }
    
    /**
     * @return The time in milliseconds.
     */
    public int getTime() {
      try {
        return (int)Math.round((Double)nativeComponent.getOleProperty(new String[] {"controls", "currentPosition"}) * 1000);
      } catch(Exception e) {
        return -1;
      }
    }
    
  }
  
  private WMPControls wmpControls;
  
  public WMPControls getWMPControls() {
    return wmpControls;
  }

  public String getLoadedResource() {
    return (String)nativeComponent.getOleProperty(new String[] {"url"});
  }
  
  public void load(String resourcePath) {
    nativeComponent.setOleProperty("url", resourcePath == null? "": resourcePath);
  }
  
  public void setControlBarVisible(boolean isControlBarVisible) {
    nativeComponent.setOleProperty("uiMode", isControlBarVisible? "full": "none");
  }

  public boolean isControlBarVisible() {
    return Boolean.TRUE.equals("full".equals(nativeComponent.getOleProperty("uiMode")));
  }
  
  public static enum WMPMediaState {
    UNDEFINED, STOPPED, PAUSED, PLAYING, SCAN_FORWARD, SCAN_REVERSE, BUFFERING, WAITING, MEDIA_ENDED, TRANSITIONING, READY, RECONNECTING
  }
  
  public WMPMediaState getMediaState() {
    try {
      switch((Integer)nativeComponent.getOleProperty("playState")) {
        case 1: return WMPMediaState.STOPPED;
        case 2: return WMPMediaState.PAUSED;
        case 3: return WMPMediaState.PLAYING;
        case 4: return WMPMediaState.SCAN_FORWARD;
        case 5: return WMPMediaState.SCAN_REVERSE;
        case 6: return WMPMediaState.BUFFERING;
        case 7: return WMPMediaState.WAITING;
        case 8: return WMPMediaState.MEDIA_ENDED;
        case 9: return WMPMediaState.TRANSITIONING;
        case 10: return WMPMediaState.READY;
        case 11: return WMPMediaState.RECONNECTING;
      }
    } catch(Exception e) {
    }
    return WMPMediaState.UNDEFINED;
  }

}
