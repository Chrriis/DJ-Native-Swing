/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

/**
 * Options that native components use when they are created, set per next instance creation or as defaults.
 * @author Christopher Deckers
 */
public class NativeComponentOptions implements Cloneable {
  
  private static NativeComponentOptions defaultOptions;
  
  /**
   * Get the default options, that are used when creating new native components.
   * @return The default options.
   */
  public static NativeComponentOptions getDefaultOptions() {
    if(defaultOptions == null) {
      defaultOptions = new NativeComponentOptions();
    }
    return defaultOptions;
  }
  
  public static void setDefaultOptions(NativeComponentOptions defaultOptions) {
    NativeComponentOptions.defaultOptions = defaultOptions;
  }
  
  private static NativeComponentOptions nextInstanceOptions;
  
  /**
   * Get the options that will be used when creating the next instance of a native component. The next instance options are a copy of the default options from the moment this method is called the first time before a new instance is created.
   * @return the next instance options.
   */
  public static NativeComponentOptions getNextInstanceOptions() {
    if(nextInstanceOptions == null) {
      nextInstanceOptions = (NativeComponentOptions)getDefaultOptions().clone();
    }
    return nextInstanceOptions;
  }
  
  public static void setNextInstanceOptions(NativeComponentOptions nextInstanceOptions) {
    NativeComponentOptions.nextInstanceOptions = nextInstanceOptions;
  }
  
  public static enum FiliationType {
    AUTO,
    DIRECT,
    COMPONENT_PROXYING,
    WINDOW_PROXYING,
  }
  
  private FiliationType filiationType = FiliationType.AUTO;
  
  /**
   * Proxied filiation allows re-parenting and change of component Z-order.
   */
  public void setFiliationType(FiliationType filiationType) {
    if(filiationType == null) {
      filiationType = FiliationType.AUTO;
    }
    this.filiationType = filiationType;
  }

  public FiliationType getFiliationType() {
    return filiationType;
  }
  
  public static enum DestructionTime {
    AUTO,
    ON_REMOVAL,
    ON_FINALIZATION,
  }
  
  private DestructionTime destructionTime = DestructionTime.AUTO;
  
  /**
   * Destruction on finalization allows removal and later re-addition to the user interface. It requires a proxied filiation, and will select one automatically if it is set to default. It is also possible to explicitely dispose the component rather than waiting until finalization.
   */
  public void setDestructionTime(DestructionTime destructionTime) {
    if(destructionTime == null) {
      destructionTime = DestructionTime.AUTO;
    }
    this.destructionTime = destructionTime;
  }
  
  public DestructionTime getDestructionTime() {
    return destructionTime;
  }
  
  public static enum VisibilityConstraint {
    AUTO,
    NONE,
    FULL_COMPONENT_TREE,
  }
  
  private VisibilityConstraint visibilityConstraint = VisibilityConstraint.AUTO;
  
  /**
   * Visibility constraints allow to superimpose native components and Swing components.
   */
  public void setVisibilityConstraint(VisibilityConstraint visibilityConstraint) {
    if(visibilityConstraint == null) {
      visibilityConstraint = VisibilityConstraint.AUTO;
    }
    this.visibilityConstraint = visibilityConstraint;
  }
  
  public VisibilityConstraint getVisibilityConstraint() {
    return visibilityConstraint;
  }
  
  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }
  
}