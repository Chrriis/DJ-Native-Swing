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
public class NSOption {

  /**
   * A group of options.
   * @author Christopher Deckers
   */
  public static class NSOptionGroup {
    
    private String groupName;
    
    public NSOptionGroup(String groupName) {
      this.groupName = groupName;
    }
    
    @Override
    public String toString() {
      return groupName;
    }
    
  }
  
  private NSOptionGroup group;
  
  /**
   * Create an option.
   * @param optionGroup the group to which this option belongs to, which may be null.
   */
  public NSOption(NSOptionGroup group) {
    this.group = group;
  }
  
  /**
   * Get the group of this option.
   * @return the group this option belongs to, or null if none is specified.
   */
  public NSOptionGroup getGroup() {
    return group;
  }
  
  /**
   * Get the value of this option. The default is to return this object, assuming the instance is a singleton. This method should be overriden if a different value should be considered.
   * @return the value of this option.
   */
  public Object getOptionValue() {
    return this;
  }
  
  @Override
  public String toString() {
    NSOptionGroup group = getGroup();
    if(group == null) {
      return getClass().getName() + "=" + getOptionValue();
    }
    return group + "=" + getOptionValue();
  }
  
}
