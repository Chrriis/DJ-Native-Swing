/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import chrriis.dj.nativeswing.NSOption.NSOptionGroup;

/**
 * @author Christopher Deckers
 */
public interface NSComponentOptions {

  public static final NSOptionGroup DESTRUCTION_TIME_OPTION_GROUP = new NSOptionGroup("Destruction Time");
  public static final NSOption DESTROY_ON_FINALIZATION = new NSOption(DESTRUCTION_TIME_OPTION_GROUP);
  
  public static final NSOptionGroup FILIATION_TYPE_OPTION_GROUP = new NSOptionGroup("Filiation Type");
  public static final NSOption PROXY_COMPONENT_HIERARCHY = new NSOption(FILIATION_TYPE_OPTION_GROUP);
  
  public static final NSOptionGroup VISIBILITY_CONSTRAINT_OPTION_GROUP = new NSOptionGroup("Visibility Constraint");
  public static final NSOption CONSTRAIN_VISIBILITY = new NSOption(VISIBILITY_CONSTRAINT_OPTION_GROUP);
  
}
