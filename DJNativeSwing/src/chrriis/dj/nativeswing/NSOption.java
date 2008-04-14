/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christopher Deckers
 */
public class NSOption {

  /**
   * Create a map from option key to option value. If a key is present several times, the latest value is retained.
   * @param options the options for which to construct the map.
   */
  public static Map<Object, Object> createOptionMap(NSOption... options) {
    Map<Object, Object> keyToValueMap = new HashMap<Object, Object>();
    if(options == null) {
      return keyToValueMap;
    }
    for(NSOption option: options) {
      keyToValueMap.put(option.getOptionKey(), option.getOptionValue());
    }
    return keyToValueMap;
  }
  
  private Object key;
  
  /**
   * Create an option.
   * @param key the key, or null for a default key to be created.
   */
  public NSOption(Object key) {
    this.key = key == null? getClass().getName(): key;
  }
  
  /**
   * Get the key of this option.
   * @return the key of this option.
   */
  public Object getOptionKey() {
    return key;
  }
  
  /**
   * Get the value of this option. The default is to return this object, assuming the instance is a singleton. This method should be overriden if a different value should be considered.
   * @return the value of this option.
   */
  public Object getOptionValue() {
    return this;
  }
  
  @Override
  public String toString() {
    return getOptionKey() + "=" + getOptionValue();
  }
  
}
