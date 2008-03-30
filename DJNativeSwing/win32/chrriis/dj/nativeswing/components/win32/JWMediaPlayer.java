/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components.win32;

import java.awt.BorderLayout;

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
  
  /**
   * Construct a Windows Media Player.
   */
  public JWMediaPlayer() {
    nativeComponent = new NativeWMediaPlayer();
    initialize(nativeComponent);
    wmpSettings = new WMPSettings(this);
    wmpControls = new WMPControls(this);
    add(nativeComponent.createEmbeddableComponent(), BorderLayout.CENTER);
    wmpSettings.setAutoStart(true);
    wmpSettings.setErrorDialogsEnabled(false);
    setControlBarVisible(true);
  }
  
  /**
   * Get the native component.
   * @return the native component.
   */
  public NativeComponent getNativeComponent() {
    return nativeComponent;
  }
  
  private WMPSettings wmpSettings;
  
  /**
   * Get the Media Player object responsible for settings-related actions.
   * @return the Media Player settings object.
   */
  public WMPSettings getWMPSettings() {
    return wmpSettings;
  }

  private WMPControls wmpControls;
  
  /**
   * Get the Media Player object responsible for controls-related actions.
   * @return the Media Player controls object.
   */
  public WMPControls getWMPControls() {
    return wmpControls;
  }

//  public String getLoadedResource() {
//    return (String)nativeComponent.getOleProperty(new String[] {"url"});
//  }
  
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
  
  /**
   * Get the state of the media.
   * @return the state of the media.
   */
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
