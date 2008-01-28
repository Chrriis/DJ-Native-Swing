/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui.win32;

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.JPanel;

import chrriis.dj.nativeswing.Disposable;
import chrriis.dj.nativeswing.ui.event.InitializationEvent;
import chrriis.dj.nativeswing.ui.event.InitializationListener;

/**
 * A multimedia player.
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fail, the methods will not have any effect. The results from methods have relevant values only when the component is valid. 
 * @author Christopher Deckers
 */
public class JMultiMediaPlayer extends JPanel implements Disposable {

  protected Component embeddableComponent;
  protected NativeMultimediaPlayer nativeComponent;
  
  protected static class NInitializationListener implements InitializationListener {
    protected Reference<JMultiMediaPlayer> multiMediaPlayer;
    protected NInitializationListener(JMultiMediaPlayer multiMediaPlayer) {
      this.multiMediaPlayer = new WeakReference<JMultiMediaPlayer>(multiMediaPlayer);
    }
    public void componentInitialized(InitializationEvent e) {
      JMultiMediaPlayer multiMediaPlayer = this.multiMediaPlayer.get();
      if(multiMediaPlayer == null) {
        return;
      }
      Object[] listeners = multiMediaPlayer.listenerList.getListenerList();
      e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == InitializationEvent.class) {
          if(e == null) {
            e = new InitializationEvent(multiMediaPlayer);
          }
          ((InitializationListener)listeners[i + 1]).componentInitialized(e);
        }
      }
    }
  }
  
  public JMultiMediaPlayer() {
    setLayout(new BorderLayout(0, 0));
    nativeComponent = new NativeMultimediaPlayer();
    nativeComponent.addInitializationListener(new NInitializationListener(this));
    embeddableComponent = nativeComponent.createEmbeddableComponent();
    add(embeddableComponent, BorderLayout.CENTER);
  }
  
  /**
   * The display component is the component that actually shows the web browser content.
   * This access is useful to attach listeners (key, mouse) to trap events happening in that area.
   */
  public Component getDisplayComponent() {
    return nativeComponent;
  }
  
  public void addInitializationListener(InitializationListener listener) {
    listenerList.add(InitializationListener.class, listener);
  }
  
  public void removeWebBrowserListener(InitializationListener listener) {
    listenerList.remove(InitializationListener.class, listener);
  }
  
  public InitializationListener[] getInitializationListeners() {
    return listenerList.getListeners(InitializationListener.class);
  }

  /**
   * @return true if the control was initialized. If the initialization failed, this would return true but isValidControl would return false.
   */
  public boolean isInitialized() {
    return nativeComponent.isInitialized();
  }
  
  /**
   * @return true if the component is initialized and is properly created.
   */
  public boolean isValidControl() {
    return nativeComponent.isValidControl();
  }

  public boolean setURL(String url) {
    return nativeComponent.setURL(url);
  }
  
  public String getURL() {
    return nativeComponent.getURL();
  }
  
  public boolean setControlBarVisible(boolean isControlBarVisible) {
    return nativeComponent.setControlBarVisible(isControlBarVisible);
  }
  
  public boolean isControlBarVisible() {
    return nativeComponent.isControlBarVisible();
  }
  
  public boolean setVolume(int volume) {
    return nativeComponent.setVolume(volume);
  }

  /**
   * @return The volume, between 0 and 100. When mute, the volume is still returned. -1 indicate that it could not be accessed.
   */
  public int getVolume() {
    return nativeComponent.getVolume();
  }
  
  /**
   * @param stereoBalance The stereo balance between -100 and 100, with 0 being the default.
   */
  public boolean setStereoBalance(int stereoBalance) {
    return nativeComponent.setStereoBalance(stereoBalance);
  }
  
  /**
   * @return The stereo balance, between -100 and 100, with 0 being the default. When mute, the balance is still returned.
   */
  public int getStereoBalance() {
    return nativeComponent.getStereoBalance();
  }
  
  public boolean setAutoStart(boolean isAutoStart) {
    return nativeComponent.setAutoStart(isAutoStart);
  }
  
  public boolean isAutoStart() {
    return nativeComponent.isAutoStart();
  }
  
//  public boolean setFullScreen(boolean isFullScreen) {
//    return nativeComponent.setFullScreen(isFullScreen);
//  }
//  
//  public boolean isFullScreen() {
//    return nativeComponent.isFullScreen();
//  }
  
  public boolean setMute(boolean isMute) {
    return nativeComponent.setMute(isMute);
  }
  
  public boolean isMute() {
    return nativeComponent.isMute();
  }
  
  public boolean isPlayEnabled() {
    return nativeComponent.isPlayEnabled();
  }
  
  public boolean play() {
    return nativeComponent.play();
  }
  
  public boolean isStopEnabled() {
    return nativeComponent.isStopEnabled();
  }
  
  public boolean stop() {
    return nativeComponent.stop();
  }
  
  public boolean isPauseEnabled() {
    return nativeComponent.isPauseEnabled();
  }
  
  public boolean pause() {
    return nativeComponent.pause();
  }
  
  public void dispose() {
    if(embeddableComponent instanceof Disposable) {
      ((Disposable)embeddableComponent).dispose();
    }
  }
  
}
