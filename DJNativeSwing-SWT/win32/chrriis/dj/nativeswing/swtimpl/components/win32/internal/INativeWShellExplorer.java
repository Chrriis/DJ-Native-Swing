/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.win32.internal;

import java.awt.Component;
import java.util.Map;

import chrriis.dj.nativeswing.swtimpl.components.win32.ShellExplorerListener;
import chrriis.dj.nativeswing.swtimpl.internal.IOleNativeComponent;

/**
 * @author Christopher Deckers
 */
public interface INativeWShellExplorer extends IOleNativeComponent {

  public Component createEmbeddableComponent(Map<Object, Object> optionMap);

  public void addShellExplorerListener(ShellExplorerListener listener);

  public void removeShellExplorerListener(ShellExplorerListener listener);

}
