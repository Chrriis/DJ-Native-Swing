/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common;

/**
 * A generic filter suitable for flat and tree like structures.
 * @author Christopher Deckers
 */
public interface Filter<T> {

  public static enum Acceptance {
    YES,
    NO,
    TEST_CHILDREN,
  }

  /**
   * Test whether the element should be accepted.
   */
  public Acceptance accept(T element);

}
