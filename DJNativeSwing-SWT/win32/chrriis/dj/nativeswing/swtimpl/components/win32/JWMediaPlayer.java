/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.win32;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import chrriis.common.WebServer;
import chrriis.dj.nativeswing.NSOption;
import chrriis.dj.nativeswing.swtimpl.NSPanelComponent;
import chrriis.dj.nativeswing.swtimpl.NativeComponent;
import chrriis.dj.nativeswing.swtimpl.components.win32.internal.INativeWMediaPlayer;
import chrriis.dj.nativeswing.swtimpl.internal.NativeCoreObjectFactory;

/**
 * A multimedia player, based on the Windows Media Player (only available on the Windows operating system).<br/>
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fails, the methods will not have any effect. The results from methods have relevant values only when the component is valid.
 * @author Christopher Deckers
 */
public class JWMediaPlayer extends NSPanelComponent {

  private INativeWMediaPlayer nativeComponent;

  /**
   * Construct a Windows Media Player.
   * @param options the options to configure the behavior of this component.
   */
  public JWMediaPlayer(NSOption... options) {
    nativeComponent = NativeCoreObjectFactory.create(INativeWMediaPlayer.class, "chrriis.dj.nativeswing.swtimpl.components.win32.core.NativeWMediaPlayer", new Class<?>[0], new Object[0]);
    initialize((NativeComponent)nativeComponent);
    wmpSettings = new WMPSettings(this);
    wmpControls = new WMPControls(this);
    wmpMedia = new WMPMedia(this);
    add(nativeComponent.createEmbeddableComponent(NSOption.createOptionMap(options)), BorderLayout.CENTER);
    wmpSettings.setAutoStart(true);
    wmpSettings.setErrorDialogsEnabled(false);
    setControlBarVisible(true);
  }

  private WMPSettings wmpSettings;

  /**
   * Get the Media Player object responsible for settings-related actions.
   * @return the Media Player settings object.
   */
  public WMPSettings getWMPSettings() {
    return wmpSettings;
  }

  private WMPControls wmpControls;

  /**
   * Get the Media Player object responsible for controls-related actions.
   * @return the Media Player controls object.
   */
  public WMPControls getWMPControls() {
    return wmpControls;
  }

  private WMPMedia wmpMedia;

  /**
   * Get the Media Player object responsible for media-related actions.
   * @return the Media Player media object.
   */
  public WMPMedia getWMPMedia() {
    return wmpMedia;
  }

//  public String getLoadedResource() {
//    return (String)nativeComponent.getOleProperty(new String[] {"url"});
//  }

  /**
   * Load a file.
   * @param resourcePath the path or URL to the file.
   */
  public void load(String resourcePath) {
    nativeComponent.setOleProperty("url", resourcePath == null? "": resourcePath);
  }

  /**
   * Load a file from the classpath.
   * @param clazz the reference clazz of the file to load.
   * @param resourcePath the path to the file.
   */
  public void load(Class<?> clazz, String resourcePath) {
    addReferenceClassLoader(clazz.getClassLoader());
    load(WebServer.getDefaultWebServer().getClassPathResourceURL(clazz.getName(), resourcePath));
  }

  /**
   * Set whether the control bar is visible.
   * @param isControlBarVisible true if the control bar should be visible, false otherwise.
   */
  public void setControlBarVisible(boolean isControlBarVisible) {
    nativeComponent.setOleProperty("uiMode", isControlBarVisible? "full": "none");
  }

  /**
   * Indicate whether the control bar is visible.
   * @return true if the control bar is visible.
   */
  public boolean isControlBarVisible() {
    return Boolean.TRUE.equals("full".equals(nativeComponent.getOleProperty("uiMode")));
  }

  /**
   * Set whether the video is playing in full screen mode.
   * @param isFullScreen true if the full screen mode should be active, false otherwise.
   */
  public void setFullScreen(boolean isFullScreen) {
    nativeComponent.setOleProperty("fullScreen", isFullScreen);
  }

  /**
   * Indicate whether the video is in full screen mode.
   * @return true if the video is in full screen mode.
   */
  public boolean isFullScreen() {
    return Boolean.TRUE.equals(nativeComponent.getOleProperty("fullScreen"));
  }

  /**
   * Set whether the video is stretched to fit.
   * @param isStretchToFit true if the video is stretched to fit, false otherwise.
   */
  public void setStretchToFit(boolean isStretchToFit) {
    nativeComponent.setOleProperty("stretchToFit", isStretchToFit);
  }

  /**
   * Indicate whether the video is stretched to fit.
   * @return true if the video is stretched to fit.
   */
  public boolean isStretchToFit() {
    return Boolean.TRUE.equals(nativeComponent.getOleProperty("stretchToFit"));
  }

  /**
   * The state of a media.
   * @author Christopher Deckers
   */
  public static enum WMPMediaState {
    UNDEFINED, STOPPED, PAUSED, PLAYING, SCAN_FORWARD, SCAN_REVERSE, BUFFERING, WAITING, MEDIA_ENDED, TRANSITIONING, READY, RECONNECTING
  }

  /**
   * Get the state of the media.
   * @return the state of the media.
   */
  public WMPMediaState getMediaState() {
    try {
      switch((Integer)nativeComponent.getOleProperty("playState")) {
        case 1: return WMPMediaState.STOPPED;
        case 2: return WMPMediaState.PAUSED;
        case 3: return WMPMediaState.PLAYING;
        case 4: return WMPMediaState.SCAN_FORWARD;
        case 5: return WMPMediaState.SCAN_REVERSE;
        case 6: return WMPMediaState.BUFFERING;
        case 7: return WMPMediaState.WAITING;
        case 8: return WMPMediaState.MEDIA_ENDED;
        case 9: return WMPMediaState.TRANSITIONING;
        case 10: return WMPMediaState.READY;
        case 11: return WMPMediaState.RECONNECTING;
      }
    } catch(IllegalStateException e) {
      // Invalid UI thread is an illegal state
      throw e;
    } catch(Exception e) {
    }
    return WMPMediaState.UNDEFINED;
  }

  private List<ClassLoader> referenceClassLoaderList = new ArrayList<ClassLoader>(1);

  private void addReferenceClassLoader(ClassLoader referenceClassLoader) {
    if(referenceClassLoader == null || referenceClassLoader == getClass().getClassLoader() || referenceClassLoaderList.contains(referenceClassLoader)) {
      return;
    }
    // If a different class loader is used to locate a resource, we need to allow th web server to find that resource
    referenceClassLoaderList.add(referenceClassLoader);
    WebServer.getDefaultWebServer().addReferenceClassLoader(referenceClassLoader);
  }

  @Override
  protected void finalize() throws Throwable {
    for(ClassLoader referenceClassLoader: referenceClassLoaderList) {
      WebServer.getDefaultWebServer().removeReferenceClassLoader(referenceClassLoader);
    }
    referenceClassLoaderList.clear();
    super.finalize();
  }

}
