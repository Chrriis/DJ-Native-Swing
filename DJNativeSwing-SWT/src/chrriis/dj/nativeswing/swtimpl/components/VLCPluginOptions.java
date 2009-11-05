/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.util.Collections;
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
    setParameters(null);
  }

  private Map<String, String> keyToValueParameterMap;

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
      this.keyToValueParameterMap = Collections.synchronizedMap(new HashMap<String, String>());
    } else {
      this.keyToValueParameterMap = Collections.synchronizedMap(new HashMap<String, String>(keyToValueParameterMap));
    }
  }

}