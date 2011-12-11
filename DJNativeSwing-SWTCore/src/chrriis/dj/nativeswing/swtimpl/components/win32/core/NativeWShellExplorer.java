/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components.win32.core;

import java.awt.Component;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleControlSite;
import org.eclipse.swt.ole.win32.OleEvent;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.OleListener;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import chrriis.dj.nativeswing.swtimpl.components.win32.JWShellExplorer;
import chrriis.dj.nativeswing.swtimpl.components.win32.ShellExplorerDocumentCompleteEvent;
import chrriis.dj.nativeswing.swtimpl.components.win32.ShellExplorerListener;
import chrriis.dj.nativeswing.swtimpl.components.win32.internal.INativeWShellExplorer;
import chrriis.dj.nativeswing.swtimpl.core.ControlCommandMessage;
import chrriis.dj.nativeswing.swtimpl.core.SWTOleNativeComponent;

/**
 * @author Christopher Deckers
 */
class NativeWShellExplorer extends SWTOleNativeComponent implements INativeWShellExplorer {

  private static String IID_DWebBrowserEvents2 = "{34A715A0-6587-11D0-924A-0020AFC7AC4D}";
  // Event ID
  private static int DocumentComplete = 0x00000103;
  
  private static class CMJ_sendDocumentCompleteEvent extends ControlCommandMessage {
    @Override
    public Object run(Object[] args) {
      NativeWShellExplorer nativeShellExplorer = (NativeWShellExplorer)getNativeComponent();
      JWShellExplorer shellExplorer = nativeShellExplorer == null? null: nativeShellExplorer.shellExplorer.get();
      if(shellExplorer == null) {
        return null;
      }
      Object[] listeners = nativeShellExplorer.listenerList.getListenerList();
      ShellExplorerDocumentCompleteEvent e = null;
      for(int i=listeners.length-2; i>=0; i-=2) {
        if(listeners[i] == ShellExplorerListener.class) {
          if(e == null) {
            e = new ShellExplorerDocumentCompleteEvent(shellExplorer, (String)args[0]);
          }
          ((ShellExplorerListener)listeners[i + 1]).documentComplete(e);
        }
      }
      return null;
    }
  }

  protected static Control createControl(Composite parent, Object[] parameters) {
    final OleFrame frame = new OleFrame(parent, SWT.NONE);
    OleControlSite site;
    try {
      site = new OleControlSite(frame, SWT.NONE, "Shell.Explorer");
      configureOleFrame(site, frame);
      // Add a listener
      OleAutomation shellExplorer = new OleAutomation(site);
      int[] dispIDs = shellExplorer.getIDsOfNames(new String[] {"Application"});
      Variant pVarResult = shellExplorer.getProperty(dispIDs[0]);
      final OleAutomation application = pVarResult.getAutomation();
      frame.addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent e) {
          application.dispose();
        }
      });
      pVarResult.dispose();
      shellExplorer.dispose();
      OleListener listener = new OleListener() {
        public void handleEvent (OleEvent e) {
          Variant[] args = e.arguments;
          String url = args[1].getString();
          // two arguments which must be released
          for (int i = 0; i < args.length; i++) {
            args[i].dispose();
          }
          new CMJ_sendDocumentCompleteEvent().asyncExec(frame, url);
        }
      };
      site.addEventListener(application, IID_DWebBrowserEvents2, DocumentComplete, listener);
    } catch(SWTException e) {
      e.printStackTrace();
      frame.dispose();
      return null;
    }
    site.doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
    return frame;
  }
  
  private Reference<JWShellExplorer> shellExplorer;

  public NativeWShellExplorer(JWShellExplorer shellExplorer) {
    this.shellExplorer = new WeakReference<JWShellExplorer>(shellExplorer);
  }

  public void addShellExplorerListener(ShellExplorerListener listener) {
    listenerList.add(ShellExplorerListener.class, listener);
  }

  public void removeShellExplorerListener(ShellExplorerListener listener) {
    listenerList.remove(ShellExplorerListener.class, listener);
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
