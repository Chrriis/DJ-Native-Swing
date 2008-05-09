/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components.win32;

/**
 * A Media Player object responsible for media-related actions.
 * @author Christopher Deckers
 * @author Matt Brooke-Smith
 */
public class WMPMedia {

  private NativeWMediaPlayer nativeComponent;
  
  WMPMedia(JWMediaPlayer wMediaPlayer) {
    this.nativeComponent = (NativeWMediaPlayer)wMediaPlayer.getNativeComponent();
  }
  
  /**
   * Get the duration in milliseconds of the current media.
   * @return the duration in milliseconds, or -1 in case of failure.
   */
  public int getDuration() {
    try {
      return (int)Math.round((Double)nativeComponent.getOleProperty(new String[]{"currentMedia", "duration"}) * 1000);
    } catch (Exception e) {
      return -1;
    }
  }

  
}
