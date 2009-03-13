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
 * A VLC object responsible for video-related actions.
 * @author Christopher Deckers
 */
public class VLCVideo {

  private WebBrowserObject webBrowserObject;

  VLCVideo(JVLCPlayer vlcPlayer) {
    webBrowserObject = vlcPlayer.getWebBrowserObject();
  }

  /**
   * Get the width of the video.
   * @return the width, or -1 in case of failure.
   */
  public int getWidth() {
    Object value = webBrowserObject.getObjectProperty("video.width");
    return value == null? -1: ((Number)value).intValue();
  }

  /**
   * Get the height of the video.
   * @return the height, or -1 in case of failure.
   */
  public int getHeight() {
    Object value = webBrowserObject.getObjectProperty("video.height");
    return value == null? -1: ((Number)value).intValue();
  }

  /**
   * Set whether the video is playing in full screen mode.
   * @param isFullScreen true if the full screen mode should be active, false otherwise.
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

  /**
   * An aspect ratio.
   * @author Christopher Deckers
   */
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
   * @return the aspect ratio, or null in case of failure.
   */
  public VLCAspectRatio getAspectRatio() {
    String value = (String)webBrowserObject.getObjectProperty("video.aspectRatio");
    if("1:1".equals(value)) {
      return VLCAspectRatio._1x1;
    }
    if("4:3".equals(value)) {
      return VLCAspectRatio._4x3;
    }
    if("16:9".equals(value)) {
      return VLCAspectRatio._16x9;
    }
    if("16:10".equals(value)) {
      return VLCAspectRatio._16x10;
    }
    if("221:100".equals(value)) {
      return VLCAspectRatio._221x100;
    }
    if("5:4".equals(value)) {
      return VLCAspectRatio._5x4;
    }
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
   * @return the track of the subtitles, or 0 if disabled, or -1 in case of failure.
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