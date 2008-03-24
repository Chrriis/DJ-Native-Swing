/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui.win32;

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.JPanel;

import chrriis.common.Disposable;
import chrriis.dj.nativeswing.ui.NativeComponent;
import chrriis.dj.nativeswing.ui.event.InitializationEvent;
import chrriis.dj.nativeswing.ui.event.InitializationListener;

/**
 * A multimedia player.
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fail, the methods will not have any effect. The results from methods have relevant values only when the component is valid. 
 * @author Christopher Deckers
 */
public class JMultiMediaPlayer extends JPanel implements Disposable {

  private Component embeddableComponent;
  private NativeMultiMediaPlayer nativeComponent;
  
  private static class NInitializationListener implements InitializationListener {
    private Reference<JMultiMediaPlayer> multiMediaPlayer;
    private NInitializationListener(JMultiMediaPlayer multiMediaPlayer) {
      this.multiMediaPlayer = new WeakReference<JMultiMediaPlayer>(multiMediaPlayer);
    }
    public void objectInitialized(InitializationEvent e) {
      JMultiMediaPlayer multiMediaPlayer = this.multiMediaPlayer.get();
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
  
  public JMultiMediaPlayer() {
    setLayout(new BorderLayout(0, 0));
    nativeComponent = new NativeMultiMediaPlayer();
    wmpSettings = new WMPSettings(this);
    wmpControls = new WMPControls(this);
    nativeComponent.addInitializationListener(new NInitializationListener(this));
    embeddableComponent = nativeComponent.createEmbeddableComponent();
    add(embeddableComponent, BorderLayout.CENTER);
    wmpSettings.setAutoStart(true);
    wmpSettings.setErrorDialogsEnabled(false);
    setControlBarVisible(true);
  }
  
  public NativeComponent getNativeComponent() {
    return nativeComponent;
  }
  
  public void addInitializationListener(InitializationListener listener) {
    listenerList.add(InitializationListener.class, listener);
  }
  
  public void removeInitializationListener(InitializationListener listener) {
    listenerList.remove(InitializationListener.class, listener);
  }
  
  public InitializationListener[] getInitializationListeners() {
    return listenerList.getListeners(InitializationListener.class);
  }

  public static class WMPSettings {
    
    private NativeMultiMediaPlayer nativeComponent;
    
    private WMPSettings(JMultiMediaPlayer multiMediaPlayer) {
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
    
    private NativeMultiMediaPlayer nativeComponent;
    
    private WMPControls(JMultiMediaPlayer multiMediaPlayer) {
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
     * @param time The time in milliseconds.
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

  public void dispose() {
    if(embeddableComponent instanceof Disposable) {
      ((Disposable)embeddableComponent).dispose();
    }
  }
  
  public boolean isDisposed() {
    return nativeComponent.isDisposed();
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
  
  public static enum WMPState {
    UNDEFINED, STOPPED, PAUSED, PLAYING, SCAN_FORWARD, SCAN_REVERSE, BUFFERING, WAITING, MEDIA_ENDED, TRANSITIONING, READY, RECONNECTING
  }
  
  public WMPState getState() {
    try {
      switch((Integer)nativeComponent.getOleProperty("playState")) {
        case 1: return WMPState.STOPPED;
        case 2: return WMPState.PAUSED;
        case 3: return WMPState.PLAYING;
        case 4: return WMPState.SCAN_FORWARD;
        case 5: return WMPState.SCAN_REVERSE;
        case 6: return WMPState.BUFFERING;
        case 7: return WMPState.WAITING;
        case 8: return WMPState.MEDIA_ENDED;
        case 9: return WMPState.TRANSITIONING;
        case 10: return WMPState.READY;
        case 11: return WMPState.RECONNECTING;
      }
    } catch(Exception e) {
    }
    return WMPState.UNDEFINED;
  }

}
