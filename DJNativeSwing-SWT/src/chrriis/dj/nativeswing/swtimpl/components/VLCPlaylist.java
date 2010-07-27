/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.io.File;

import javax.swing.SwingUtilities;

import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.dj.nativeswing.swtimpl.NSSystemPropertySWT;
import chrriis.dj.nativeswing.swtimpl.WebBrowserObject;
import chrriis.dj.nativeswing.swtimpl.components.VLCInput.VLCMediaState;

/**
 * A VLC object responsible for playlist-related actions.
 * @author Christopher Deckers
 */
public class VLCPlaylist {

  private JVLCPlayer vlcPlayer;
  private WebBrowserObject webBrowserObject;

  VLCPlaylist(JVLCPlayer vlcPlayer) {
    this.vlcPlayer = vlcPlayer;
    webBrowserObject = vlcPlayer.getWebBrowserObject();
  }

  /**
   * Get the number of items in the playlist.
   * @return the item count, or -1 in case of failure.
   */
  public int getItemCount() {
    Object value = webBrowserObject.getObjectProperty("playlist.items.count");
    return value == null? -1: ((Number)value).intValue();
  }

  /**
   * Indicate whether the playlist is currently playing an item.
   * @return true if an item is being played.
   */
  public boolean isPlaying() {
    return Boolean.TRUE.equals(webBrowserObject.getObjectProperty("playlist.isPlaying"));
  }

  /**
   * Add an item from the classpath to the playlist.
   * @param clazz the reference clazz of the file to load.
   * @param resourcePath the path to the file.
   */
  public void addItem(Class<?> clazz, String resourcePath) {
    addItem(clazz, resourcePath, null);
  }

  /**
   * Add an item from the classpath to the playlist.
   * @param clazz the reference clazz of the file to load.
   * @param resourcePath the path to the file.
   * @param options VLC options, for example ":start-time=30 :no-audio".
   */
  public void addItem(Class<?> clazz, String resourcePath, String options) {
    vlcPlayer.addReferenceClassLoader(clazz.getClassLoader());
    addItem(WebServer.getDefaultWebServer().getClassPathResourceURL(clazz.getName(), resourcePath), options);
  }

  /**
   * Add an item to the playlist.
   * @param resourcePath the path or URL to the file.
   */
  public void addItem(String resourcePath) {
    addItem(resourcePath, null);
  }

  /**
   * Add an item to the playlist.
   * @param resourcePath the path or URL to the file.
   * @param options VLC options, for example ":start-time=30 :no-audio".
   */
  public void addItem(String resourcePath, String options) {
    if(!webBrowserObject.hasContent()) {
      vlcPlayer.load();
      clear();
    }
    File file = Utils.getLocalFile(resourcePath);
    if(file != null) {
      resourcePath = webBrowserObject.getLocalFileURL(file);
    }
    webBrowserObject.invokeObjectFunction("playlist.add", resourcePath, resourcePath, options);
  }

  /**
   * Start playing the playlist.
   */
  public void play() {
    setPlaylistFixActive(false);
    webBrowserObject.invokeObjectFunction("playlist.play");
    setPlaylistFixActive(true);
  }

  /**
   * Start playing an item from the playlist using its ID.
   * @param itemID the ID of the item to play.
   */
  public void playItem(int itemID) {
    setPlaylistFixActive(false);
    webBrowserObject.invokeObjectFunction("playlist.playItem", itemID);
    setPlaylistFixActive(true);
  }

  /**
   * Toggle the pause state.
   */
  public void togglePause() {
    webBrowserObject.invokeObjectFunction("playlist.togglePause");
  }

  /**
   * Stop playing.
   */
  public void stop() {
    setPlaylistFixActive(false);
    webBrowserObject.invokeObjectFunction("playlist.stop");
  }

  /**
   * Move to the next item of the playlist.
   */
  public void goNext() {
    setPlaylistFixActive(false);
    webBrowserObject.invokeObjectFunction("playlist.next");
    setPlaylistFixActive(true);
  }

  /**
   * Move to the previous item of the playlist.
   */
  public void goPrevious() {
    setPlaylistFixActive(false);
    webBrowserObject.invokeObjectFunction("playlist.prev");
    setPlaylistFixActive(true);
  }

  /**
   * Clear the playlist.
   */
  public void clear() {
    setPlaylistFixActive(false);
    webBrowserObject.invokeObjectFunction("playlist.items.clear");
  }

  /**
   * Remove an item using its index.
   * @param index the index of the item.
   */
  public void removeItem(int index) {
    webBrowserObject.invokeObjectFunction("playlist.items.removeItem", index);
  }

  private volatile Thread playlistFixThread;

  /**
   * VLC seems to have a bug: it does queue items but does not auto play the next one when the current one has finished playing.
   */
  private void setPlaylistFixActive(boolean isActive) {
    if(playlistFixThread != null == isActive) {
      return;
    }
    if(isActive) {
      if(!Boolean.parseBoolean(NSSystemPropertySWT.VLCPLAYER_FIXPLAYLISTAUTOPLAYNEXT.get("true"))) {
        return;
      }
      playlistFixThread = new Thread("NativeSwing - VLC Player playlist fix") {
        @Override
        public void run() {
          final Thread currentThread = this;
          boolean isFirst = true;
          while(currentThread == playlistFixThread) {
            if(vlcPlayer.isNativePeerDisposed()) {
              setPlaylistFixActive(false);
              return;
            }
            try {
              sleep(isFirst? 3000: 500);
              isFirst = false;
            } catch(Exception e) {}
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                if(currentThread != playlistFixThread) {
                  return;
                }
                if(!vlcPlayer.isNativePeerValid()) {
                  return;
                }
                if(vlcPlayer.getVLCInput().getMediaState() == VLCMediaState.ERROR) {
                  goNext();
                }
              }
            });
          }
        }
      };
      playlistFixThread.setDaemon(true);
      playlistFixThread.start();
    } else {
      playlistFixThread = null;
    }
  }

}