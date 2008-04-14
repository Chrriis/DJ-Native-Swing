/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;


/**
 * @author Christopher Deckers
 */
public interface NSComponentOptions {

  public static final NSOption DESTROY_ON_FINALIZATION = new NSOption("Destruction Time");
  public static final NSOption PROXY_COMPONENT_HIERARCHY = new NSOption("Filiation Type");
  public static final NSOption CONSTRAIN_VISIBILITY = new NSOption("Visibility Constraint");
  
}
