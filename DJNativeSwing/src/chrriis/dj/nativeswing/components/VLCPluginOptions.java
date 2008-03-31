/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.components;

import java.util.HashMap;
import java.util.Map;

/**
 * The options for the VLC player to better configure the initialization of the VLC plugin.
 * @author Christopher Deckers
 */
public class VLCPluginOptions {
  
  /**
   * Construct new VLC options.
   */
  public VLCPluginOptions() {
  }
  
  private Map<String, String> keyToValueParameterMap = new HashMap<String, String>();

  /**
   * Get the VLC plugin HTML parameters.
   * @return the parameters.
   */
  public Map<String, String> getParameters() {
    return keyToValueParameterMap;
  }
  
  /**
   * Set the VLC HTML parameters that will be used when the plugin is created.
   * @param keyToValueParameterMap the map of key/value pairs.
   */
  public void setParameters(Map<String, String> keyToValueParameterMap) {
    if(keyToValueParameterMap == null) {
      keyToValueParameterMap = new HashMap<String, String>();
    }
    this.keyToValueParameterMap = keyToValueParameterMap;
  }
  
}