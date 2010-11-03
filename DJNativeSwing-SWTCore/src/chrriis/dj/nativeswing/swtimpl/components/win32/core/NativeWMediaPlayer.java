/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.win32.core;

import java.awt.Component;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleClientSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import chrriis.dj.nativeswing.swtimpl.components.win32.internal.INativeWMediaPlayer;
import chrriis.dj.nativeswing.swtimpl.core.SWTOleNativeComponent;

/**
 * @author Christopher Deckers
 */
class NativeWMediaPlayer extends SWTOleNativeComponent implements INativeWMediaPlayer {

  protected static Control createControl(Composite parent, Object[] parameters) {
    OleFrame frame = new OleFrame(parent, SWT.NONE);
    OleClientSite site;
    try {
      site = new OleClientSite(frame, SWT.NONE, "WMPlayer.OCX");
      configureOleFrame(site, frame);
    } catch(SWTException e) {
      e.printStackTrace();
      frame.dispose();
      return null;
    }
    site.doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
    return frame;
  }

  @Override
  public Component createEmbeddableComponent(Map<Object, Object> optionMap) {
    return super.createEmbeddableComponent(optionMap);
  }

  @Override
  protected void disposeNativePeer() {
    super.disposeNativePeer();
  }

}
