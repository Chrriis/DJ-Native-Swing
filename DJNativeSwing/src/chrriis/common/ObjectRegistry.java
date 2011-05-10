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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import chrriis.dj.nativeswing.NSSystemProperty;

/**
 * A convenient class to register objects to an ID.
 * @author Christopher Deckers
 */
public class ObjectRegistry {

  private static Thread cleanUpThread;
  private static Set<ObjectRegistry> registrySet = new HashSet<ObjectRegistry>();

  private static int nextThreadNumber;

  private static void startThread(ObjectRegistry objectRegistry) {
    synchronized (registrySet) {
      registrySet.add(objectRegistry);
      if(cleanUpThread != null) {
        return;
      }
      cleanUpThread = new Thread("Registry cleanup thread-" + nextThreadNumber++) {
        @Override
        public void run() {
          while(true) {
            try {
              sleep(1000);
            } catch(Exception e) {
            }
            ObjectRegistry[] registries;
            synchronized (registrySet) {
              registries = registrySet.toArray(new ObjectRegistry[0]);
            }
            for(ObjectRegistry registry: registries) {
              synchronized(registry) {
                for(Integer instanceID: registry.instanceIDToObjectReferenceMap.keySet().toArray(new Integer[0])) {
                  if(registry.instanceIDToObjectReferenceMap.get(instanceID).get() == null) {
                    registry.instanceIDToObjectReferenceMap.remove(instanceID);
                  }
                }
                if(registry.instanceIDToObjectReferenceMap.isEmpty()) {
                  synchronized (registrySet) {
                    registrySet.remove(registry);
                  }
                }
              }
            }
            synchronized (registrySet) {
              if(registrySet.isEmpty()) {
                cleanUpThread = null;
                return;
              }
            }
          }
        }
      };
      boolean isApplet = "applet".equals(NSSystemProperty.DEPLOYMENT_TYPE.get());
      cleanUpThread.setDaemon(!isApplet);
      cleanUpThread.start();
    }
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
  public int add(Object o) {
    boolean isStartingThread = false;
    int instanceID;
    synchronized (this) {
      while(true) {
        instanceID = nextInstanceID++;
        if(!instanceIDToObjectReferenceMap.containsKey(instanceID)) {
          if(o != null) {
            instanceIDToObjectReferenceMap.put(instanceID, new WeakReference<Object>(o));
            isStartingThread = true;
          }
          break;
        }
      }
    }
    if(isStartingThread) {
      startThread(this);
    }
    return instanceID;
  }

  /**
   * Add an object to the registry, specifying its ID, wich throws an exception if the ID is already in use.
   * @param o the object to add.
   * @param instanceID the ID to associate the object to.
   */
  public void add(Object o, int instanceID) {
    synchronized (this) {
      Object o2 = get(instanceID);
      if(o2 != null && o2 != o) {
        throw new IllegalStateException("An object is already registered with the id \"" + instanceID + "\" for object: " + o);
      }
      instanceIDToObjectReferenceMap.put(instanceID, new WeakReference<Object>(o));
    }
    startThread(this);
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
  }

  /**
   * Get all the instance IDs that are used in this registry.
   * @return the instance IDs.
   */
  public synchronized int[] getInstanceIDs() {
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
