/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christopher Deckers
 */
public class Registry {

  protected Registry() {}

  private Thread cleanUpThread;
  
  private void startThread() {
    synchronized (LOCK) {
      if(cleanUpThread != null) {
        return;
      }
      cleanUpThread = new Thread("Registry cleanup thread") {
        @Override
        public void run() {
          while(true) {
            try {
              sleep(5000);
            } catch(Exception e) {
            }
            synchronized (LOCK) {
              for(Integer instanceID: instanceIDToObjectReferenceMap.keySet().toArray(new Integer[0])) {
                if(instanceIDToObjectReferenceMap.get(instanceID).get() == null) {
                  instanceIDToObjectReferenceMap.remove(instanceID);
                }
              }
              if(instanceIDToObjectReferenceMap.isEmpty()) {
                cleanUpThread = null;
                return;
              }
            }
          }
        }
      };
    }
    cleanUpThread.setDaemon(true);
    cleanUpThread.start();
  }
  
  protected Object LOCK = new Object();
  private int nextInstanceID;
  private Map<Integer, WeakReference<Object>> instanceIDToObjectReferenceMap = new HashMap<Integer, WeakReference<Object>>();
  
  /**
   * @return an instance ID that is strictly greater than 0.
   */
  public int add(Object o) {
    synchronized (LOCK) {
      int instanceID = nextInstanceID++;
      if(o == null) {
        return instanceID;
      }
      instanceIDToObjectReferenceMap.put(instanceID, new WeakReference<Object>(o));
      startThread();
      return instanceID;
    }
  }
  
  public Object get(int instanceID) {
    synchronized (LOCK) {
      WeakReference<Object> weakReference = instanceIDToObjectReferenceMap.get(instanceID);
      if(weakReference == null) {
        return null;
      }
      Object o = weakReference.get();
      if(o == null) {
        instanceIDToObjectReferenceMap.remove(instanceID);
      }
      return o;
    }
  }
  
  public void remove(int instanceID) {
    instanceIDToObjectReferenceMap.remove(instanceID);
  }
  
  private static Registry registry = new Registry();
  
  public static Registry getInstance() {
    return registry;
  }
  
}
