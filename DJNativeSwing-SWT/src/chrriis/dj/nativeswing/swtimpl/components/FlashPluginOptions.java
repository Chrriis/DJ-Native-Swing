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
import java.util.Map.Entry;

import chrriis.common.Utils;

/**
 * The options for the Flash player to better configure the initialization of the Flash plugin.
 * @author Christopher Deckers
 */
public class FlashPluginOptions {

  /**
   * Construct new Flash plugin options.
   */
  public FlashPluginOptions() {
    setParameters(null);
    setVariables(null);
  }

  private Map<String, String> keyToValueVariableMap;

  /**
   * Get the Flash plugin variables.
   * @return the variables.
   */
  public Map<String, String> getVariables() {
    return keyToValueVariableMap;
  }

  /**
   * Set the Flash variables that will be set when the plugin is created.
   * @param keyToValueVariableMap the map of key/value pairs.
   */
  public void setVariables(Map<String, String> keyToValueVariableMap) {
    if(keyToValueVariableMap == null) {
      this.keyToValueVariableMap = Collections.synchronizedMap(new HashMap<String, String>());
    } else {
      this.keyToValueVariableMap = Collections.synchronizedMap(new HashMap<String, String>(keyToValueVariableMap));
    }
  }

  private Map<String, String> keyToValueParameterMap;

  /**
   * Get the Flash plugin HTML parameters.
   * @return the parameters.
   */
  public Map<String, String> getParameters() {
    return keyToValueParameterMap;
  }

  /**
   * Set the Flash HTML parameters that will be used when the plugin is created.
   * @param keyToValueParameterMap the map of key/value pairs.
   */
  public void setParameters(Map<String, String> keyToValueParameterMap) {
    if(keyToValueParameterMap == null) {
      this.keyToValueParameterMap = Collections.synchronizedMap(new HashMap<String, String>());
    } else {
      this.keyToValueParameterMap = Collections.synchronizedMap(new HashMap<String, String>(keyToValueParameterMap));
    }
  }

  Map<String, String> getHTMLParameters() {
    HashMap<String, String> htmlParameters = new HashMap<String, String>(getParameters());
    StringBuilder variablesSB = new StringBuilder();
    for(Entry<String, String> variable: getVariables().entrySet()) {
      if(variablesSB.length() > 0) {
        variablesSB.append('&');
      }
      variablesSB.append(Utils.encodeURL(variable.getKey())).append('=').append(Utils.encodeURL(variable.getValue()));
    }
    if(variablesSB.length() > 0) {
      htmlParameters.put("flashvars", variablesSB.toString());
    }
    htmlParameters.put("allowScriptAccess", "always");
    htmlParameters.put("swliveconnect", "true");
    return htmlParameters;
  }

}