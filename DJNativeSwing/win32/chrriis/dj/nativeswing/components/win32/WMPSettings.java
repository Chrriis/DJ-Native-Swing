/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components.win32;

/**
 * A Media Player object responsible for settings-related actions.
 * @author Christopher Deckers
 */
public class WMPSettings {
  
  private NativeWMediaPlayer nativeComponent;
  
  WMPSettings(JWMediaPlayer multiMediaPlayer) {
    this.nativeComponent = (NativeWMediaPlayer)multiMediaPlayer.getNativeComponent();
  }
  
  void setErrorDialogsEnabled(boolean isErrorDialogEnabled) {
    nativeComponent.setOleProperty(new String[] {"settings", "enableErrorDialogs"}, isErrorDialogEnabled);
  }
  
  /**
   * Set the volume.
   * @param volume the new volume, with a value between 0 and 100.
   */
  public void setVolume(int volume) {
    if(volume < 0 || volume > 100) {
      throw new IllegalArgumentException("The volume must be between 0 and 100");
    }
    nativeComponent.setOleProperty(new String[] {"settings", "volume"}, volume);
  }
  
  /**
   * Get the volume, as a value between 0 and 100.
   * @return The volume, between 0 and 100. When mute, the volume is still returned.
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
   * Get the stereo balance.
   * @return The stereo balance, between -100 and 100, with 0 being the default. When mute, the balance is still returned.
   */
  public int getStereoBalance() {
    try {
      return (Integer)nativeComponent.getOleProperty(new String[] {"settings", "balance"});
    } catch(Exception e) {
      return -1;
    }
  }
  
  /**
   * Set whether loaded media should automatically start.
   * @param isAutoStart true if the media should start playing automatically when loaded, false otherwise.
   */
  public void setAutoStart(boolean isAutoStart) {
    nativeComponent.setOleProperty(new String[] {"settings", "autoStart"}, isAutoStart);
  }
  
  /**
   * Indicate whether loading some media should start playing automatically.
   * @return true if loading some media should start playing automatically.
   */
  public boolean isAutoStart() {
    return Boolean.TRUE.equals(nativeComponent.getOleProperty(new String[] {"settings", "autoStart"}));
  }
  
  /**
   * Set whether audio is mute.
   * @param isMute true if audio should be mute, false otherwise.
   */
  public void setMute(boolean isMute) {
    nativeComponent.setOleProperty(new String[] {"settings", "mute"}, isMute);
  }
  
  /**
   * Indicate whether audio is mute.
   * @return true if audio is mute.
   */
  public boolean isMute() {
    return Boolean.TRUE.equals(nativeComponent.getOleProperty(new String[] {"settings", "mute"}));
  }
  
}