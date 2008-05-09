/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components;

import java.io.File;

import chrriis.common.Utils;
import chrriis.common.WebServer;
import chrriis.dj.nativeswing.WebBrowserObject;

/**
 * A VLC object responsible for playlist-related actions.
 * @author Christopher Deckers
 */
public class VLCPlaylist {
  
  private JVLCPlayer vlcPlayer;
  private WebBrowserObject webBrowserObject;
  
  VLCPlaylist(JVLCPlayer vlcPlayer) {
    this.vlcPlayer = vlcPlayer;
    this.webBrowserObject = vlcPlayer.getWebBrowserObject();
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
   * Add an item from the classpath to the playlist and get its ID for future manipulation.
   * @param clazz the reference clazz of the file to load.
   * @param resourcePath the path to the file.
   */
  public int addItem(Class<?> clazz, String resourcePath) {
    vlcPlayer.addReferenceClassLoader(clazz.getClassLoader());
    return addItem(WebServer.getDefaultWebServer().getClassPathResourceURL(clazz.getName(), resourcePath));
  }
  
  /**
   * Add an item to the playlist and get its ID for future manipulation.
   * @param resourcePath the path or URL to the file.
   * @return the item ID, which can be used to add play or remove an item from the playlist.
   */
  public int addItem(String resourcePath) {
    if(!webBrowserObject.hasContent()) {
      vlcPlayer.load();
      clear();
    }
    File file = Utils.getLocalFile(resourcePath);
    if(file != null) {
      resourcePath = webBrowserObject.getLocalFileURL(file);
    }
    Object value = webBrowserObject.invokeObjectFunctionWithResult("playlist.add", resourcePath);
    return value == null? -1: ((Number)value).intValue();
  }
  
  /**
   * Start playing the playlist.
   */
  public void play() {
    webBrowserObject.invokeObjectFunction("playlist.play");
  }
  
  /**
   * Start playing an item from the playlist using its ID.
   * @param itemID the ID of the item to play.
   */
  public void playItem(int itemID) {
    webBrowserObject.invokeObjectFunction("playlist.playItem", itemID);
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
    webBrowserObject.invokeObjectFunction("playlist.stop");
  }
  
  /**
   * Move to the next item of the playlist.
   */
  public void goNext() {
    webBrowserObject.invokeObjectFunction("playlist.next");
  }
  
  /**
   * Move to the previous item of the playlist.
   */
  public void goPrevious() {
    webBrowserObject.invokeObjectFunction("playlist.prev");
  }
  
  /**
   * Clear the playlist.
   */
  public void clear() {
    webBrowserObject.invokeObjectFunction("playlist.items.clear");
  }
  
  /**
   * Remove an item using its ID.
   * @param itemID the ID of the item.
   */
  public void removeItem(int itemID) {
    webBrowserObject.invokeObjectFunction("playlist.items.removeItem", itemID);
  }
  
}