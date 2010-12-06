/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl;

import java.util.Map;

/**
 * An interface that can be implemented to provide a custom peer VM process creation when set on the <code>NativeInterfaceConfiguration</code>.
 * @author Christopher Deckers
 */
public interface PeerVMProcessFactory {

	public Process createProcess(String[] classpathItems, Map<String, String> systemPropertiesMap, String[] vmParams, String mainClass, String[] mainClassParameters);

}
