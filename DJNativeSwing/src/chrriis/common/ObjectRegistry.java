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
 * A convenient class to register objects to an ID.
 * @author Christopher Deckers
 */
public class ObjectRegistry {

  private Thread cleanUpThread;

  private synchronized void startThread() {
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
          synchronized(ObjectRegistry.this) {
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
    cleanUpThread.setDaemon(true);
    cleanUpThread.start();
  }

  private int nextInstanceID = 1;
  private Map<Integer, WeakReference<Object>> instanceIDToObjectReferenceMap = new HashMap<Integer, WeakReference<Object>>();

  /**
   * Construct an object registry.
   */
  public ObjectRegistry() {
  }

  /**
   * Add an object to the registry.
   * @param o the object to add.
   * @return an unused instance ID that is strictly greater than 0.
   */
  public synchronized int add(Object o) {
    while(true) {
      int instanceID = nextInstanceID++;
      if(!instanceIDToObjectReferenceMap.containsKey(instanceID)) {
        if(o == null) {
          return instanceID;
        }
        instanceIDToObjectReferenceMap.put(instanceID, new WeakReference<Object>(o));
        startThread();
        return instanceID;
      }
    }
  }

  /**
   * Add an object to the registry, specifying its ID, wich throws an exception if the ID is already in use.
   * @param o the object to add.
   * @param instanceID the ID to associate the object to.
   */
  public synchronized void add(Object o, int instanceID) {
    Object o2 = get(instanceID);
    if(o2 != null && o2 != o) {
      throw new IllegalStateException("An object is already registered with the id \"" + instanceID + "\" for object: " + o);
    }
    instanceIDToObjectReferenceMap.put(instanceID, new WeakReference<Object>(o));
    startThread();
  }

  /**
   * Get an object using its ID.
   * @return the object, or null.
   */
  public synchronized Object get(int instanceID) {
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

  /**
   * Remove an object from the registry using its instance ID.
   * @param instanceID the ID of the object to remove.
   */
  public synchronized void remove(int instanceID) {
    instanceIDToObjectReferenceMap.remove(instanceID);
    if(instanceIDToObjectReferenceMap.isEmpty() && cleanUpThread != null) {
      cleanUpThread.interrupt();
      cleanUpThread = null;
    }
  }

  /**
   * Get all the instance IDs that are used in this registry.
   * @return the instance IDs.
   */
  public int[] getInstanceIDs() {
    Object[] instanceIDObjects = instanceIDToObjectReferenceMap.keySet().toArray();
    int[] instanceIDs = new int[instanceIDObjects.length];
    for(int i=0; i<instanceIDObjects.length; i++) {
      instanceIDs[i] = (Integer)instanceIDObjects[i];
    }
    return instanceIDs;
  }

  private static ObjectRegistry registry = new ObjectRegistry();

  /**
   * Get the default shared instance of a registry.
   */
  public static ObjectRegistry getInstance() {
    return registry;
  }

}
