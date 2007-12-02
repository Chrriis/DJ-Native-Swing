/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo;

import javax.swing.JComponent;

class Example {
  
  protected String name;
  protected Class<? extends JComponent> componentClass;
  protected String description;
  protected boolean isShowingSources;
  
  public Example(String name, Class<? extends JComponent> componentClass, String description, boolean isShowingSources) {
    this.name = name;
    this.componentClass = componentClass;
    this.description = description;
    this.isShowingSources = isShowingSources;
  }
  
  public Class<? extends JComponent> getComponentClass() {
    return componentClass;
  }
  
  public String getDescription() {
    return description;
  }
  
  public boolean isShowingSources() {
    return isShowingSources;
  }
  
  @Override
  public String toString() {
    return name;
  }
  
}