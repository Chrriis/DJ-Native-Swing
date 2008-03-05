/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui.win32;

import java.awt.Component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleClientSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import chrriis.dj.nativeswing.ui.NativeComponent;

/**
 * @author Christopher Deckers
 */
class NativeMultimediaPlayer extends NativeComponent {
  
  public NativeMultimediaPlayer() {
    setAutoStart(true);
    setControlBarVisible(true);
    setErrorDialogsEnabled(false);
  }
  
  private static final int WM_PLAYER = 1;
  private static final int MEDIA_PLAYER = 2;

  protected static Control createControl(Shell shell) {
    int type;
    OleClientSite site;
    OleFrame frame = new OleFrame(shell, SWT.NONE);
    try {
      try {
        site = new OleClientSite(frame, SWT.NONE, "WMPlayer.OCX");
        type = WM_PLAYER;
      } catch(SWTException e) {
        // WMP 10 is fine but WMP 11 is crap: it does not integrate properly and generates an exception. So we fallback on the old media player.
        site = new OleClientSite(frame, SWT.NONE, "MediaPlayer.MediaPlayer.1");
        type = MEDIA_PLAYER;
      }
      frame.setData("NS_type", type);
      frame.setData("NS_site", site);
    } catch(SWTException e) {
      e.printStackTrace();
      frame.dispose();
      return null;
    }
    site.doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
    return frame;
  }
  
  private static class CMN_setErrorDialogsEnabled extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"settings"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"enableErrorDialogs"});
            if(ids != null) {
              result = automation.setProperty(ids[0], new Variant((Boolean)args[0]));
            }
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }
  
  public void setErrorDialogsEnabled(boolean isErrorDialogEnabled) {
    runAsync(new CMN_setErrorDialogsEnabled(), isErrorDialogEnabled);
  }
  
  private static class CMN_getURL extends ControlCommandMessage {
    @Override
    public Object run() {
      String result = null;
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"url"});
          if(ids != null) {
            result = automation.getProperty(ids[0]).getString();
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"fileName"});
          if(ids != null) {
            result = automation.getProperty(ids[0]).getString();
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }

  public String getURL() {
    return (String)runSync(new CMN_getURL());
  }
  
  private static class CMN_setURL extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      String url = (String)args[0];
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"url"});
          if(ids != null) {
            result = automation.setProperty(ids[0], new Variant(url == null? "": url));
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"fileName"});
          if(ids != null) {
            result = automation.setProperty(ids[0], new Variant(url == null? "": url));
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }

  public void setURL(final String url) {
    runAsync(new CMN_setURL(), url);
  }
  
  private static class CMN_setControlBarVisible extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      boolean isControlBarVisible = (Boolean)args[0];
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"uiMode"});
          if(ids != null) {
            automation.setProperty(ids[0], new Variant[] {new Variant(isControlBarVisible? "full": "none")});
            result = true;
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"showControls"});
          if(ids != null) {
            automation.setProperty(ids[0], new Variant[] {new Variant(isControlBarVisible)});
            result = true;
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }
  
  public void setControlBarVisible(final boolean isControlBarVisible) {
    runAsync(new CMN_setControlBarVisible(), isControlBarVisible);
  }

  private static class CMN_isControlBarVisible extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"uiMode"});
          if(ids != null) {
            result = "full".equals(automation.getProperty(ids[0]).getString());
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"showControls"});
          if(ids != null) {
            result = automation.getProperty(ids[0]).getBoolean();
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }
  
  public boolean isControlBarVisible() {
    return Boolean.TRUE.equals(runSync(new CMN_isControlBarVisible()));
  }
  
  private static class CMN_setVolume extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      int volume = (Integer)args[0];
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"settings"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"volume"});
            if(ids != null) {
              result = automation.setProperty(ids[0], new Variant(volume));
            }
          }
          break;
        }
        case MEDIA_PLAYER: {
          int volume_ = -(int)Math.round(Math.pow((100 - volume) / 2.0, 2));
          int[] ids = automation.getIDsOfNames(new String[] {"volume"});
          if(ids != null) {
            result = automation.setProperty(ids[0], new Variant(volume_));
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }

  public void setVolume(final int volume) {
    if(volume < 0 || volume > 100) {
      throw new IllegalArgumentException("The volume must be between 0 and 100");
    }
    runAsync(new CMN_setVolume());
  }
  
  private static class CMN_getVolume extends ControlCommandMessage {
    @Override
    public Object run() {
      int result = -1;
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"settings"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"volume"});
            if(ids != null) {
              result = automation.getProperty(ids[0]).getInt();
            }
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"volume"});
          if(ids != null) {
            int volume = automation.getProperty(ids[0]).getInt();
            volume = 100 - (int)Math.round(Math.sqrt(-volume) * 2);
            result = volume;
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }

  public int getVolume() {
    Object result = runSync(new CMN_getVolume());
    return result == null? -1: (Integer)result;
  }
  
  private static class CMN_setStereoBalance extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      int stereoBalance = (Integer)args[0];
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"settings"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"balance"});
            if(ids != null) {
              result = automation.setProperty(ids[0], new Variant(stereoBalance));
            }
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"balance"});
          if(ids != null) {
            result = automation.setProperty(ids[0], new Variant(stereoBalance));
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }

  public void setStereoBalance(final int stereoBalance) {
    if(stereoBalance < 100 || stereoBalance > 100) {
      throw new IllegalArgumentException("The stereo balance must be between -100 and 100");
    }
    runAsync(new CMN_setStereoBalance());
  }
  
  private static class CMN_getStereoBalance extends ControlCommandMessage {
    @Override
    public Object run() {
      int result = -1;
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"settings"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"balance"});
            if(ids != null) {
              result = automation.getProperty(ids[0]).getInt();
            }
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"balance"});
          if(ids != null) {
            result = automation.getProperty(ids[0]).getInt();
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }

  public int getStereoBalance() {
    Object result = runSync(new CMN_getStereoBalance());
    return result == null? -1: (Integer)result;
  }
  
  private static class CMN_setAutoStart extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      boolean isAutoStart = (Boolean)args[0];
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"settings"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"autoStart"});
            if(ids != null) {
              result = automation.setProperty(ids[0], new Variant(isAutoStart));
            }
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"autoStart"});
          if(ids != null) {
            result = automation.setProperty(ids[0], new Variant(isAutoStart));
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }
  
  public void setAutoStart(final boolean isAutoStart) {
    runAsync(new CMN_setAutoStart(), isAutoStart);
  }
  
  private static class CMN_isAutoStart extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"settings"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"autoStart"});
            if(ids != null) {
              result = automation.getProperty(ids[0]).getBoolean();
            }
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"autoStart"});
          if(ids != null) {
            result = automation.getProperty(ids[0]).getBoolean();
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }
  
  public boolean isAutoStart() {
    return Boolean.TRUE.equals(runSync(new CMN_isAutoStart()));
  }
  
  private static class CMN_setMute extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      boolean isMute = (Boolean)args[0];
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"settings"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"mute"});
            if(ids != null) {
              result = automation.setProperty(ids[0], new Variant(isMute));
            }
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"mute"});
          if(ids != null) {
            result = automation.setProperty(ids[0], new Variant(isMute));
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }
  
  public void setMute(final boolean isMute) {
    runAsync(new CMN_setMute(), isMute);
  }
  
  private static class CMN_isMute extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"settings"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"mute"});
            if(ids != null) {
              result = automation.getProperty(ids[0]).getBoolean();
            }
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"mute"});
          if(ids != null) {
            result = automation.getProperty(ids[0]).getBoolean();
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }
  
  public boolean isMute() {
    return Boolean.TRUE.equals(runSync(new CMN_isMute()));
  }
  
  private static class CMN_isPlayEnabled extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"controls"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"isAvailable"});
            if(ids != null) {
              result = automation.getProperty(ids[0], new Variant[] {new Variant("Play")}).getBoolean();
            }
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"playState"});
          if(ids != null) {
            switch(automation.getProperty(ids[0]).getInt()) {
              case 0: // File loaded but not playing
              case 1: // Paused
                result = true;
            }
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }
  
  public boolean isPlayEnabled() {
    return Boolean.TRUE.equals(runSync(new CMN_isPlayEnabled()));
  }
  
  private static class CMN_play extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"controls"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"Play"});
            if(ids != null) {
              automation.invoke(ids[0]);
              result = true;
            }
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"Play"});
          if(ids != null) {
            automation.invoke(ids[0]);
            result = true;
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }
  
  public void play() {
    runAsync(new CMN_play());
  }
  
  private static class CMN_isStopEnabled extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"controls"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"isAvailable"});
            if(ids != null) {
              result = automation.getProperty(ids[0], new Variant[] {new Variant("Stop")}).getBoolean();
            }
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"playState"});
          if(ids != null) {
            switch(automation.getProperty(ids[0]).getInt()) {
              case 1: // Paused
              case 2: // File loaded and playing
                result = true;
            }
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }
  
  public boolean isStopEnabled() {
    return Boolean.TRUE.equals(runSync(new CMN_isStopEnabled()));
  }
  
  private static class CMN_stop extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"controls"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"Stop"});
            if(ids != null) {
              automation.invoke(ids[0]);
              result = true;
            }
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"Stop"});
          if(ids != null) {
            automation.invoke(ids[0]);
            result = true;
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }
  
  public void stop() {
    runAsync(new CMN_stop());
  }
  
  private static class CMN_isPauseEnabled extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"controls"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"isAvailable"});
            if(ids != null) {
              result = automation.getProperty(ids[0], new Variant[] {new Variant("Pause")}).getBoolean();
            }
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"playState"});
          if(ids != null) {
            switch(automation.getProperty(ids[0]).getInt()) {
              case 2: // File loaded and playing
                result = true;
            }
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }
  
  public boolean isPauseEnabled() {
    return Boolean.TRUE.equals(runSync(new CMN_isPauseEnabled()));
  }
  
  private static class CMN_pause extends ControlCommandMessage {
    @Override
    public Object run() {
      boolean result = false;
      OleFrame frame = (OleFrame)getControl();
      OleAutomation automation = new OleAutomation((OleClientSite)frame.getData("NS_site"));
      switch((Integer)frame.getData("NS_type")) {
        case WM_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"controls"});
          if(ids != null) {
            automation = automation.getProperty(ids[0]).getAutomation();
            ids = automation.getIDsOfNames(new String[] {"Pause"});
            if(ids != null) {
              automation.invoke(ids[0]);
              result = true;
            }
          }
          break;
        }
        case MEDIA_PLAYER: {
          int[] ids = automation.getIDsOfNames(new String[] {"Pause"});
          if(ids != null) {
            automation.invoke(ids[0]);
            result = true;
          }
          break;
        }
      }
      automation.dispose();
      return result;
    }
  }
  
  public void pause() {
    runAsync(new CMN_pause());
  }
  
  protected Component createEmbeddableComponent() {
    return super.createEmbeddableComponent();
  }
  
}
