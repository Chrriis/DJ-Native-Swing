/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chrriis.common.Utils;

/**
 * A class that allows to set HTTP headers and POST data to use during navigation requests.
 * @author Christopher Deckers
 */
public class WebBrowserNavigationParameters {

  private String[] headers;

  /**
   * Set the headers using a map of key/value pairs.
   * @param keyValueMap The key/value pair map containing the headers.
   */
  public void setHeaders(Map<String, String> keyValueMap) {
    if(keyValueMap == null || keyValueMap.isEmpty()) {
      headers = null;
      return;
    }
    List<String> headerList = new ArrayList<String>();
    for(String key: keyValueMap.keySet()) {
      if(key != null && key.length() > 0) {
        headerList.add(key + ": " + keyValueMap.get(key));
      }
    }
    headers = headerList.toArray(new String[0]);
  }

  public String[] getHeaders() {
    return headers;
  }

  private String postData;

  /**
   * Set the POST data.
   * @param postData The raw data to use as the body of the POST request.
   */
  public void setPostData(String postData) {
    this.postData = postData;
  }

  /**
   * Set the POST data using a map of key/value pairs.
   * @param keyValueMap The key/value pair map containing the POST data.
   */
  public void setPostData(Map<String, String> keyValueMap) {
    if(keyValueMap == null || keyValueMap.isEmpty()) {
      postData = null;
      return;
    }
    StringBuilder sb = new StringBuilder();
    for(String key: keyValueMap.keySet()) {
      if(sb.length() > 0) {
        sb.append('&');
      }
      if(key != null && key.length() > 0) {
        sb.append(Utils.encodeURL(key));
        sb.append('=');
      }
      sb.append(Utils.encodeURL(keyValueMap.get(key)));
    }
    postData = sb.toString();
  }

  public String getPostData() {
    return postData;
  }

}
