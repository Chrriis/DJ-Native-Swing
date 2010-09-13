/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.dj.nativeswing.NSOption;
import chrriis.dj.nativeswing.swtimpl.NSPanelComponent;
import chrriis.dj.nativeswing.swtimpl.WebBrowserObject;

/**
 * A native multimedia player. It is a browser-based component, which relies on the VLC plugin.<br/>
 * Methods execute when this component is initialized. If the component is not initialized, methods will be executed as soon as it gets initialized.
 * If the initialization fails, the methods will not have any effect. The results from methods have relevant values only when the component is valid.
 * @author Christopher Deckers
 */
public class JVLCPlayer extends NSPanelComponent {

  /**
   * A factory that creates the decorators for VLC players.
   * @author Christopher Deckers
   */
  public static interface VLCPlayerDecoratorFactory {
    /**
     * Create the decorator for a VLC player, which adds the rendering component to its component hierarchy and will itself be added to the VLC player.
     * @param vlcPlayer the VLC player for which to create the decorator.
     * @param renderingComponent the component that renders the VLC player's content.
     * @return the decorator.
     */
    public VLCPlayerDecorator createVLCPlayerDecorator(JVLCPlayer vlcPlayer, Component renderingComponent);
  }

  private static VLCPlayerDecoratorFactory vlcPlayerDecoratorFactory;

  /**
   * Set the decorator that will be used for future vlc player instances.
   * @param vlcPlayerDecoratorFactory the factory that creates the decorators, or null for default decorators.
   */
  public static void setVLCPlayerDecoratorFactory(VLCPlayerDecoratorFactory vlcPlayerDecoratorFactory) {
    JVLCPlayer.vlcPlayerDecoratorFactory = vlcPlayerDecoratorFactory;
  }

  private VLCPlayerDecorator vlcPlayerDecorator;

  VLCPlayerDecorator getVLCPlayerDecorator() {
    return vlcPlayerDecorator;
  }

  /**
   * Create a decorator for this vlc player. This method can be overriden so that the vlc player uses a different decorator.
   * @param renderingComponent the component to add to the decorator's component hierarchy.
   * @return the decorator that was created.
   */
  protected VLCPlayerDecorator createVLCPlayerDecorator(Component renderingComponent) {
    if(vlcPlayerDecoratorFactory != null) {
      VLCPlayerDecorator vlcPlayerDecorator = vlcPlayerDecoratorFactory.createVLCPlayerDecorator(this, renderingComponent);
      if(vlcPlayerDecorator != null) {
        return vlcPlayerDecorator;
      }
    }
    return new DefaultVLCPlayerDecorator(this, renderingComponent);
  }

  private JWebBrowser webBrowser;

  private static class NWebBrowserObject extends WebBrowserObject {

    private final JVLCPlayer vlcPlayer;

    public NWebBrowserObject(JVLCPlayer vlcPlayer) {
      super(vlcPlayer.webBrowser);
      this.vlcPlayer = vlcPlayer;
    }

    @Override
    protected ObjectHTMLConfiguration getObjectHtmlConfiguration() {
      ObjectHTMLConfiguration objectHTMLConfiguration = new ObjectHTMLConfiguration();
      if(vlcPlayer.options != null) {
        // Possible when debugging and calling the same URL again. No options but better than nothing.
        objectHTMLConfiguration.setHTMLParameters(vlcPlayer.options.getParameters());
      }
      objectHTMLConfiguration.setWindowsClassID("9BE31822-FDAD-461B-AD51-BE1D1C159921");
      objectHTMLConfiguration.setWindowsInstallationURL("http://downloads.videolan.org/pub/videolan/vlc/latest/win32/axvlc.cab");
      objectHTMLConfiguration.setMimeType("application/x-vlc-plugin");
      objectHTMLConfiguration.setInstallationURL("http://www.videolan.org");
//      objectHTMLConfiguration.setWindowsParamName("Src");
//      objectHTMLConfiguration.setParamName("target");
      objectHTMLConfiguration.setVersion("VideoLAN.VLCPlugin.2");
//      vlcPlayer.options = null;
      return objectHTMLConfiguration;
    }

    @Override
    public String getLocalFileURL(File localFile) {
      String absolutePath = localFile.getAbsolutePath();
      if(absolutePath.startsWith("\\\\")) {
        return absolutePath;
      }
      String s;
      try {
        s = "file://" + localFile.toURI().toURL().toString().substring("file:".length());
      } catch (Exception e) {
        s = "file:///" + absolutePath;
        if(Utils.IS_WINDOWS) {
          s = s.replace('\\', '/');
        }
      }
      // We have to convert all special remaining characters (e.g. letters with accents).
      return encodeSpecialCharacters(s);
    }

    private String encodeSpecialCharacters(String s) {
      StringBuilder sb = new StringBuilder();
      for(int i=0; i<s.length(); i++) {
        char c = s.charAt(i);
        boolean isToEncode = false;
        if((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9')) {
          switch(c) {
            case '.':
            case '-':
            case '*':
            case '_':
            case '+':
            case '%':
            case ':':
            case '/':
              break;
            default:
              isToEncode = true;
              break;
          }
        }
        if(isToEncode) {
          sb.append(Utils.encodeURL(String.valueOf(c)));
        } else {
          sb.append(c);
        }
      }
      return sb.toString();
    }

  }

  private WebBrowserObject webBrowserObject;

  WebBrowserObject getWebBrowserObject() {
    return webBrowserObject;
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    cleanup();
  }

  /**
   * Construct a VLC player.
   * @param options the options to configure the behavior of this component.
   */
  public JVLCPlayer(NSOption... options) {
    webBrowser = new JWebBrowser(options);
    initialize(webBrowser.getNativeComponent());
    webBrowserObject = new NWebBrowserObject(this);
    vlcAudio = new VLCAudio(this);
    vlcInput = new VLCInput(this);
    vlcPlaylist = new VLCPlaylist(this);
    vlcVideo = new VLCVideo(this);
    vlcPlayerDecorator = createVLCPlayerDecorator(webBrowser);
    add(vlcPlayerDecorator, BorderLayout.CENTER);
  }

  /**
   * Get the web browser that contains this component. The web browser should only be used to add listeners, for example to listen to window creation events.
   * @return the web browser.
   */
  public JWebBrowser getWebBrowser() {
    return webBrowser;
  }

//  public String getLoadedResource() {
//    return webBrowserObject.getLoadedResource();
//  }

  /**
   * Load the player, with no content.
   */
  public void load() {
    load((VLCPluginOptions)null);
  }

  /**
   * Load a file.
   * @param resourceLocation the path or URL to the file.
   */
  public void load(String resourceLocation) {
    load(resourceLocation, null);
  }

  /**
   * Load the player, with no content.
   * @param options the options to better configure the initialization of the VLC plugin.
   */
  public void load(VLCPluginOptions options) {
    load_("", options);
  }

  /**
   * Load a file from the classpath.
   * @param clazz the reference clazz of the file to load.
   * @param resourcePath the path to the file.
   */
  public void load(Class<?> clazz, String resourcePath) {
    load(clazz, resourcePath, null);
  }

  /**
   * Load a file from the classpath.
   * @param clazz the reference clazz of the file to load.
   * @param resourcePath the path to the file.
   * @param options the options to better configure the initialization of the VLC plugin.
   */
  public void load(Class<?> clazz, String resourcePath, VLCPluginOptions options) {
    addReferenceClassLoader(clazz.getClassLoader());
    load(WebServer.getDefaultWebServer().getClassPathResourceURL(clazz.getName(), resourcePath), options);
  }

  private volatile VLCPluginOptions options;

  /**
   * Load a file.
   * @param resourceLocation the path or URL to the file.
   * @param options the options to better configure the initialization of the VLC plugin.
   */
  public void load(String resourceLocation, VLCPluginOptions options) {
    if("".equals(resourceLocation)) {
      resourceLocation = null;
    }
    load_(resourceLocation, options);
  }

  private void load_(String resourceLocation, VLCPluginOptions options) {
    if(options == null) {
      options = new VLCPluginOptions();
    }
    this.options = options;
    webBrowserObject.load(resourceLocation);
    VLCPlaylist playlist = getVLCPlaylist();
    if(resourceLocation != null && !"".equals(resourceLocation)) {
      playlist.stop();
      playlist.clear();
      playlist.addItem(resourceLocation);
      playlist.play();
    }
  }

  /**
   * Indicate whether the control bar is visible.
   * @return true if the control bar is visible.
   */
  public boolean isControlBarVisible() {
    return vlcPlayerDecorator.isControlBarVisible();
  }

  /**
   * Set whether the control bar is visible.
   * @param isControlBarVisible true if the control bar should be visible, false otherwise.
   */
  public void setControlBarVisible(boolean isControlBarVisible) {
    vlcPlayerDecorator.setControlBarVisible(isControlBarVisible);
  }

  /* ------------------------- VLC API exposed ------------------------- */

  private VLCAudio vlcAudio;

  /**
   * Get the VLC object responsible for audio-related actions.
   * @return the VLC audio object.
   */
  public VLCAudio getVLCAudio() {
    return vlcAudio;
  }

  private VLCInput vlcInput;

  /**
   * Get the VLC object responsible for input-related actions.
   * @return the VLC input object.
   */
  public VLCInput getVLCInput() {
    return vlcInput;
  }

  private VLCPlaylist vlcPlaylist;

  /**
   * Get the VLC object responsible for playlist-related actions.
   * @return the VLC playlist object.
   */
  public VLCPlaylist getVLCPlaylist() {
    return vlcPlaylist;
  }

  private VLCVideo vlcVideo;

  /**
   * Get the VLC object responsible for video-related actions.
   * @return the VLC video object.
   */
  public VLCVideo getVLCVideo() {
    return vlcVideo;
  }

  private List<ClassLoader> referenceClassLoaderList = new ArrayList<ClassLoader>(1);

  void addReferenceClassLoader(ClassLoader referenceClassLoader) {
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

  @Override
  public void disposeNativePeer() {
    super.disposeNativePeer();
    cleanup();
  }

  private void cleanup() {
    if(isNativePeerDisposed()) {
      webBrowserObject.load(null);
    }
  }

}
