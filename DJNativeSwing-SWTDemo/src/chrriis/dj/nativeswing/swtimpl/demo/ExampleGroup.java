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
public class ExampleGroup {

  protected String name;
  protected Example[] examples;

  public ExampleGroup(String name, Example[] examples) {
    this.name = name;
    this.examples = examples;
  }

  public String getName() {
    return name;
  }

  public Example[] getExamples() {
    return examples;
  }

  @Override
  public String toString() {
    return name;
  }

}