/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import chrriis.dj.nativeswing.swtimpl.WebBrowserObject;

/**
 * A VLC object responsible for audio-related actions.
 * @author Christopher Deckers
 */
public class VLCAudio {

  private WebBrowserObject webBrowserObject;

  VLCAudio(JVLCPlayer vlcPlayer) {
    webBrowserObject = vlcPlayer.getWebBrowserObject();
  }

  /**
   * Set whether audio is mute.
   * @param isMute true if audio should be mute, false otherwise.
   */
  public void setMute(boolean isMute) {
    webBrowserObject.setObjectProperty("audio.mute", isMute);
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
  }

  /**
   * Get the volume, as a value between 0 and 100.
   * @return the volume, or -1 in case of failure.
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
   * @return the audio track, or -1 in case of failure.
   */
  public int getTrack() {
    Object value = webBrowserObject.getObjectProperty("audio.track");
    return value == null? -1: ((Number)value).intValue();
  }

  /**
   * An audio channel.
   * @author Christopher Deckers
   */
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
   * @return the audio channel, or null in case of failure.
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
  }

}