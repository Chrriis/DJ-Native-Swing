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
  }
  
  private Map<String, String> keyToValueVariableMap = new HashMap<String, String>();
  
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
      keyToValueVariableMap = new HashMap<String, String>();
    }
    this.keyToValueVariableMap = keyToValueVariableMap;
  }
  
  private Map<String, String> keyToValueParameterMap = new HashMap<String, String>();
  
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
      keyToValueParameterMap = new HashMap<String, String>();
    }
    this.keyToValueParameterMap = keyToValueParameterMap;
  }
  
  Map<String, String> getHTMLParameters() {
    HashMap<String, String> htmlParameters = new HashMap<String, String>(getParameters());
    StringBuffer variablesSB = new StringBuffer();
    for(Entry<String, String> variable: getVariables().entrySet()) {
      if(variablesSB.length() > 0) {
        variablesSB.append('&');
      }
      variablesSB.append(Utils.escapeXML(variable.getKey())).append('=').append(Utils.escapeXML(variable.getValue()));
    }
    if(variablesSB.length() > 0) {
      htmlParameters.put("flashvars", variablesSB.toString());
    }
    htmlParameters.put("allowScriptAccess", "always");
    htmlParameters.put("swliveconnect", "true");
    return htmlParameters;
  }
  
}