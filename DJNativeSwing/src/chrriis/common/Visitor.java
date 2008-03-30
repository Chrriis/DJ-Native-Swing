/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common;

/**
 * A simple generic visitor.
 * @author Christopher Deckers
 */
public interface Visitor<T> {

  /**
   * Indicate whether the element is accepted.
   */
  public boolean accept(T element);

}
