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
 * A Media Player object responsible for media-related actions.
 * @author Christopher Deckers
 * @author Matt Brooke-Smith
 */
public class WMPMedia {

  private IOleNativeComponent nativeComponent;

  WMPMedia(JWMediaPlayer wMediaPlayer) {
    nativeComponent = (IOleNativeComponent)wMediaPlayer.getNativeComponent();
  }

  /**
   * Get the duration in milliseconds of the current media.
   * @return the duration in milliseconds, or -1 in case of failure.
   */
  public int getDuration() {
    try {
      return (int)Math.round((Double)nativeComponent.getOleProperty(new String[]{"currentMedia", "duration"}) * 1000);
    } catch(IllegalStateException e) {
      // Invalid UI thread is an illegal state
      throw e;
    } catch (Exception e) {
      return -1;
    }
  }


}
