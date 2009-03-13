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
 * A VLC object responsible for input-related actions.
 * @author Christopher Deckers
 */
public class VLCInput {

  private WebBrowserObject webBrowserObject;

  VLCInput(JVLCPlayer vlcPlayer) {
    webBrowserObject = vlcPlayer.getWebBrowserObject();
  }

  /**
   * Get the duration in milliseconds of the current media.
   * @return the duration in milliseconds, or -1 in case of failure.
   */
  public int getDuration() {
    Object value = webBrowserObject.getObjectProperty("input.length");
    return value == null? -1: ((Number)value).intValue();
  }

  /**
   * Get the number of frames per second.
   * @return the number of frames per second, or NaN in case of failure.
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
   * @return the current position in milliseconds, or -1 in case of failure.
   */
  public int getAbsolutePosition() {
    Object value = webBrowserObject.getObjectProperty("input.time");
    return value == null? -1: ((Number)value).intValue();
  }

  /**
   * The state of a media.
   * @author Christopher Deckers
   */
  public enum VLCMediaState {
    IDLE_CLOSE, OPENING, BUFFERING, PLAYING, PAUSED, STOPPING, ERROR,
  }

  /**
   * Get the state.
   * @return the state, or null in case of failure.
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
  public void setPlaySpeedFactor(float speedFactor) {
    webBrowserObject.setObjectProperty("input.rate", speedFactor);
  }

  /**
   * Get the speed factor that is applied when a media is played.
   * @return the speed factor, or NaN in case of failure.
   */
  public float getPlaySpeedFactor() {
    Object value = webBrowserObject.getObjectProperty("input.rate");
    return value == null? Float.NaN: ((Number)value).floatValue();
  }

}