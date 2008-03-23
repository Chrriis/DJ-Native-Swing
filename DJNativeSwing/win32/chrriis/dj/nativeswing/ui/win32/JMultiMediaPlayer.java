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

import chrriis.common.Disposable;
import chrriis.dj.nativeswing.ui.NativeComponent;
import chrriis.dj.nativeswing.ui.event.InitializationEvent;
import chrriis.dj.nativeswing.ui.event.InitializationListener;

/**
 * A multimedia player.
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fail, the methods will not have any effect. The results from methods have relevant values only when the component is valid. 
 * @author Christopher Deckers
 */
public class JMultiMediaPlayer extends JPanel implements Disposable {

  private Component embeddableComponent;
  private NativeMultimediaPlayer nativeComponent;
  
  private static class NInitializationListener implements InitializationListener {
    protected Reference<JMultiMediaPlayer> multiMediaPlayer;
    protected NInitializationListener(JMultiMediaPlayer multiMediaPlayer) {
      this.multiMediaPlayer = new WeakReference<JMultiMediaPlayer>(multiMediaPlayer);
    }
    public void objectInitialized(InitializationEvent e) {
      JMultiMediaPlayer multiMediaPlayer = this.multiMediaPlayer.get();
      if(multiMediaPlayer == null) {
        return;
      }
      Object[] listeners = multiMediaPlayer.listenerList.getListenerList();
      e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == InitializationListener.class) {
          if(e == null) {
            e = new InitializationEvent(multiMediaPlayer);
          }
          ((InitializationListener)listeners[i + 1]).objectInitialized(e);
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
  public NativeComponent getDisplayComponent() {
    return nativeComponent;
  }
  
  public void addInitializationListener(InitializationListener listener) {
    listenerList.add(InitializationListener.class, listener);
  }
  
  public void removeInitializationListener(InitializationListener listener) {
    listenerList.remove(InitializationListener.class, listener);
  }
  
  public InitializationListener[] getInitializationListeners() {
    return listenerList.getListeners(InitializationListener.class);
  }

  public void load(String resourcePath) {
    nativeComponent.load(resourcePath);
  }
  
//  public String getLoadedResource() {
//    return nativeComponent.getLoadedResource();
//  }
  
  public void setControlBarVisible(boolean isControlBarVisible) {
    nativeComponent.setControlBarVisible(isControlBarVisible);
  }
  
  public boolean isControlBarVisible() {
    return nativeComponent.isControlBarVisible();
  }
  
  public void setVolume(int volume) {
    nativeComponent.setVolume(volume);
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
  public void setStereoBalance(int stereoBalance) {
    nativeComponent.setStereoBalance(stereoBalance);
  }
  
  /**
   * @return The stereo balance, between -100 and 100, with 0 being the default. When mute, the balance is still returned.
   */
  public int getStereoBalance() {
    return nativeComponent.getStereoBalance();
  }
  
//  public boolean setAutoStart(boolean isAutoStart) {
//    return nativeComponent.setAutoStart(isAutoStart);
//  }
//  
//  public boolean isAutoStart() {
//    return nativeComponent.isAutoStart();
//  }
  
//  public boolean setFullScreen(boolean isFullScreen) {
//    return nativeComponent.setFullScreen(isFullScreen);
//  }
//  
//  public boolean isFullScreen() {
//    return nativeComponent.isFullScreen();
//  }
  
  public void setMute(boolean isMute) {
    nativeComponent.setMute(isMute);
  }
  
  public boolean isMute() {
    return nativeComponent.isMute();
  }
  
  public boolean isPlayEnabled() {
    return nativeComponent.isPlayEnabled();
  }
  
  public void play() {
    nativeComponent.play();
  }
  
  public boolean isStopEnabled() {
    return nativeComponent.isStopEnabled();
  }
  
  public void stop() {
    nativeComponent.stop();
  }
  
  public boolean isPauseEnabled() {
    return nativeComponent.isPauseEnabled();
  }
  
  public void pause() {
    nativeComponent.pause();
  }
  
  public void dispose() {
    if(embeddableComponent instanceof Disposable) {
      ((Disposable)embeddableComponent).dispose();
    }
  }
  
  public boolean isDisposed() {
    return nativeComponent.isDisposed();
  }
  
}
