/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.win32;

import chrriis.dj.nativeswing.swtimpl.internal.IOleNativeComponent;

/**
 * A Media Player object responsible for settings-related actions.
 * @author Christopher Deckers
 */
public class WMPSettings {

  private IOleNativeComponent nativeComponent;

  WMPSettings(JWMediaPlayer wMediaPlayer) {
    nativeComponent = (IOleNativeComponent)wMediaPlayer.getNativeComponent();
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
   * @return the volume, between 0 and 100, or -1 in case of failure. When mute, the volume is still returned.
   */
  public int getVolume() {
    try {
      return (Integer)nativeComponent.getOleProperty(new String[] {"settings", "volume"});
    } catch(IllegalStateException e) {
      // Invalid UI thread is an illegal state
      throw e;
    } catch(Exception e) {
      return -1;
    }
  }

  /**
   * Set the play count.
   * @param playCount the new playCount, with a value stricly greater than 0.
   */
  public void setPlayCount(int playCount) {
    if(playCount <= 0) {
      throw new IllegalArgumentException("The play count must be strictly greater than 0");
    }
    nativeComponent.setOleProperty(new String[] {"settings", "playCount"}, playCount);
  }

  /**
   * Get the playCount, as a value strictly greater than 0.
   * @return the play count, strictly greater than 0, or -1 in case of failure.
   */
  public int getPlayCount() {
    try {
      return (Integer)nativeComponent.getOleProperty(new String[] {"settings", "playCount"});
    } catch(IllegalStateException e) {
      // Invalid UI thread is an illegal state
      throw e;
    } catch(Exception e) {
      return -1;
    }
  }

  /**
   * Set the speed factor that is applied when a media is played.
   * @param speedFactor the speed factor, with a value strictly greater than 0.
   */
  public void setPlaySpeedFactor(float speedFactor) {
    if(speedFactor <= 0) {
      throw new IllegalArgumentException("The rate must be strictly greater than 0!");
    }
    nativeComponent.setOleProperty(new String[]{"settings", "rate"}, (double)speedFactor);
  }

  /**
   * Get the speed factor that is applied when a media is played, as a value strictly greater than 0.
   * @return the speed factor, strictly greater than 0, or NaN in case of failure.
   */
  public float getPlaySpeedFactor() {
    try {
      return ((Double)nativeComponent.getOleProperty(new String[]{"settings", "rate"})).floatValue();
    } catch(IllegalStateException e) {
      // Invalid UI thread is an illegal state
      throw e;
    } catch (Exception e) {
      return Float.NaN;
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
   * @return the stereo balance, between -100 and 100, with 0 being the default, or -1 in case of failure. When mute, the balance is still returned.
   */
  public int getStereoBalance() {
    try {
      return (Integer)nativeComponent.getOleProperty(new String[] {"settings", "balance"});
    } catch(IllegalStateException e) {
      // Invalid UI thread is an illegal state
      throw e;
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