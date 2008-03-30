/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common;

/**
 * A generic filter.
 * @author Christopher Deckers
 */
public interface Filter<T> {

  /**
   * Test whether the element should be accepted.
   */
  public boolean accept(T element);

}
