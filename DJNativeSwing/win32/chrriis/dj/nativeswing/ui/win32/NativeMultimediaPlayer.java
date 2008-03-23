/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui.win32;

import java.awt.Component;

import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleClientSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Christopher Deckers
 */
class NativeMultimediaPlayer extends OleNativeComponent {
  
  public NativeMultimediaPlayer() {
    setAutoStart(true);
    setControlBarVisible(true);
    setErrorDialogsEnabled(false);
    new Thread() {
      @Override
      public void run() {
        try {
          sleep(10000);
        } catch(Exception e) {
        }
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
//            dumpProperties();
            System.err.println("in: Pause");
            pause();
            System.err.println("in2");
          }
        });
      }
    }.start();
  }
  
  protected static Control createControl(Shell shell) {
    OleFrame frame = new OleFrame(shell, SWT.NONE);
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
  
  public void setErrorDialogsEnabled(boolean isErrorDialogEnabled) {
    setProperty(new String[] {"settings", "enableErrorDialogs"}, isErrorDialogEnabled);
  }
  
  public String getLoadedResource() {
    return (String)getProperty(new String[] {"url"});
  }
  
  public void load(String resourcePath) {
    setProperty("url", resourcePath == null? "": resourcePath);
  }
  
  public void setControlBarVisible(boolean isControlBarVisible) {
    setProperty("uiMode", isControlBarVisible? "full": "none");
  }

  public boolean isControlBarVisible() {
    return Boolean.TRUE.equals("full".equals(getProperty("uiMode")));
  }
  
  public void setVolume(int volume) {
    if(volume < 0 || volume > 100) {
      throw new IllegalArgumentException("The volume must be between 0 and 100");
    }
    setProperty(new String[] {"settings", "volume"}, volume);
  }
  
  public int getVolume() {
    try {
      return (Integer)getProperty(new String[] {"settings", "volume"});
    } catch(Exception e) {
      return -1;
    }
  }
  
  public void setStereoBalance(int stereoBalance) {
    if(stereoBalance < 100 || stereoBalance > 100) {
      throw new IllegalArgumentException("The stereo balance must be between -100 and 100");
    }
    setProperty(new String[] {"settings", "balance"}, stereoBalance);
  }
  
  public int getStereoBalance() {
    try {
      return (Integer)getProperty(new String[] {"settings", "balance"});
    } catch(Exception e) {
      return -1;
    }
  }
  
  public void setAutoStart(boolean isAutoStart) {
    setProperty(new String[] {"settings", "autoStart"}, isAutoStart);
  }
  
  public boolean isAutoStart() {
    return Boolean.TRUE.equals(getProperty(new String[] {"settings", "autoStart"}));
  }
  
  public void setMute(boolean isMute) {
    setProperty(new String[] {"settings", "mute"}, isMute);
  }
  
  public boolean isMute() {
    return Boolean.TRUE.equals(getProperty(new String[] {"settings", "mute"}));
  }
  
  public boolean isPlayEnabled() {
    return Boolean.TRUE.equals(getProperty(new String[] {"controls", "isAvailable"}, "Play"));
  }
  
  public void play() {
    invokeOleFunction(new String[] {"controls", "Play"});
  }
  
  public boolean isStopEnabled() {
    return Boolean.TRUE.equals(getProperty(new String[] {"controls", "isAvailable"}, "Stop"));
  }
  
  public void stop() {
    invokeOleFunction(new String[] {"controls", "Stop"});
  }
  
  public boolean isPauseEnabled() {
    return Boolean.TRUE.equals(getProperty(new String[] {"controls", "isAvailable"}, "Pause"));
  }
  
  public void pause() {
    invokeOleFunction(new String[] {"controls", "Pause"});
  }
  
  protected Component createEmbeddableComponent() {
    return super.createEmbeddableComponent();
  }
  
}
