/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo;

import javax.swing.JComponent;

/**
 * @author Christopher Deckers
 */
public class Example {

  protected String name;
  protected Class<? extends JComponent> componentClass;
  protected String description;
  protected boolean isShowingSources;
  protected boolean isAvailable;
  protected String notAvailableMessage;

  public Example(String name, Class<? extends JComponent> componentClass, String description, boolean isShowingSources) {
    this(name, componentClass, description, isShowingSources, true, null);
  }

  public Example(String name, Class<? extends JComponent> componentClass, String description, boolean isShowingSources, boolean isAvailable, String notAvailableMessage) {
    this.name = name;
    this.componentClass = componentClass;
    this.description = description;
    this.isShowingSources = isShowingSources;
    this.isAvailable = isAvailable;
    this.notAvailableMessage = notAvailableMessage;
  }

  public Class<? extends JComponent> getComponentClass() {
    return componentClass;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public boolean isShowingSources() {
    return isShowingSources;
  }

  public boolean isAvailable() {
    return isAvailable;
  }

  public String getNotAvailableMessage() {
    return notAvailableMessage;
  }

  @Override
  public String toString() {
    return name;
  }

}