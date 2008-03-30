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
  
  WMPControls(JWMediaPlayer multiMediaPlayer) {
    this.nativeComponent = (NativeWMediaPlayer)multiMediaPlayer.getNativeComponent();
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
   * @return The time in milliseconds.
   */
  public int getTime() {
    try {
      return (int)Math.round((Double)nativeComponent.getOleProperty(new String[] {"controls", "currentPosition"}) * 1000);
    } catch(Exception e) {
      return -1;
    }
  }
  
}