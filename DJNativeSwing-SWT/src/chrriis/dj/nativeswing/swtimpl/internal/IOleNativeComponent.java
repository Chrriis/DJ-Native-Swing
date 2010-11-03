/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.internal;


public interface IOleNativeComponent {

  public void invokeOleFunction(String functionName, Object... args);

  public void invokeOleFunction(String[] functionPath, Object... args);

  public Object invokeOleFunctionWithResult(String functionName, Object... args);

  public Object invokeOleFunctionWithResult(String[] functionPath, Object... args);

  public void setOleProperty(String property, Object... args);

  public void setOleProperty(String[] propertyPath, Object... args);

  public Object getOleProperty(String property, Object... args);

  public Object getOleProperty(String[] propertyPath, Object... args);

  public void dumpOleInterfaceDefinitions();

}
