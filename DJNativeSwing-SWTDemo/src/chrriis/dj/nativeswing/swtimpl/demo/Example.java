/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.demo;


/**
 * @author Christopher Deckers
 */
public class Example {

  protected String name;
  protected Class<?> exampleClass;
  protected String description;
  protected boolean isShowingSources;
  protected boolean isAvailable;
  protected String notAvailableMessage;

  public Example(String name, Class<?> exampleClass, String description, boolean isShowingSources) {
    this(name, exampleClass, description, isShowingSources, true, null);
  }

  public Example(String name, Class<?> exampleClass, String description, boolean isShowingSources, boolean isAvailable, String notAvailableMessage) {
    this.name = name;
    this.exampleClass = exampleClass;
    this.description = description;
    this.isShowingSources = isShowingSources;
    this.isAvailable = isAvailable;
    this.notAvailableMessage = notAvailableMessage;
  }

  public Class<?> getExampleClass() {
    return exampleClass;
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