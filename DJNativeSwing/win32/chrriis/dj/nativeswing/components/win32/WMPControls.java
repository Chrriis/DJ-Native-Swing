/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components.win32;

/**
 * A Media Player object responsible for controls-related actions.
 * @author Christopher Deckers
 */
public class WMPControls {
  
  private NativeWMediaPlayer nativeComponent;
  
  WMPControls(JWMediaPlayer wMediaPlayer) {
    this.nativeComponent = (NativeWMediaPlayer)wMediaPlayer.getNativeComponent();
  }
  
  /**
   * Indicate if the play functionality is enabled.
   * @return true if the play functionality is enabled.
   */
  public boolean isPlayEnabled() {
    return Boolean.TRUE.equals(nativeComponent.getOleProperty(new String[] {"controls", "isAvailable"}, "Play"));
  }
  
  /**
   * Start playing the loaded media.
   */
  public void play() {
    nativeComponent.invokeOleFunction(new String[] {"controls", "Play"});
  }
  
  /**
   * Indicate if the stop functionality is enabled.
   * @return true if the stop functionality is enabled.
   */
  public boolean isStopEnabled() {
    return Boolean.TRUE.equals(nativeComponent.getOleProperty(new String[] {"controls", "isAvailable"}, "Stop"));
  }
  
  /**
   * Stop the currently playing media.
   */
  public void stop() {
    nativeComponent.invokeOleFunction(new String[] {"controls", "Stop"});
  }
  
  /**
   * Indicate if the pause functionality is enabled.
   * @return true if the pause functionality is enabled.
   */
  public boolean isPauseEnabled() {
    return Boolean.TRUE.equals(nativeComponent.getOleProperty(new String[] {"controls", "isAvailable"}, "Pause"));
  }
  
  /**
   * Pause the currently playing media.
   */
  public void pause() {
    nativeComponent.invokeOleFunction(new String[] {"controls", "Pause"});
  }
  
  /**
   * Set the current position on the timeline.
   * @param time The current position in milliseconds.
   */
  public void setAbsolutePosition(int time) {
    nativeComponent.setOleProperty(new String[] {"controls", "currentPosition"}, time / 1000d);
  }
  
  /**
   * Get the current position on the timeline.
   * @return the current position in milliseconds, or -1 in case of failure.
   */
  public int getAbsolutePosition() {
    try {
      return (int)Math.round((Double)nativeComponent.getOleProperty(new String[] {"controls", "currentPosition"}) * 1000);
    } catch(Exception e) {
      return -1;
    }
  }
  
}