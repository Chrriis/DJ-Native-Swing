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
    // Set the default properties
    setAutoStart(true);
    setControlBarVisible(true);
    setErrorDialogsEnabled(false);
  }

  private OleClientSite site;
  
  private int type;
  private static final int WM_PLAYER = 1;
  private static final int MEDIA_PLAYER = 2;

  @Override
  protected Control createControl(Shell shell) {
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
    } catch(SWTException e) {
      System.err.print(getClass().getName() + " [" + hashCode() + "]: ");
      e.printStackTrace();
      frame.dispose();
      return null;
    }
//    clientSite.doVerb(OLE.OLEIVERB_SHOW);
    site.doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
//    OleAutomation automation = new OleAutomation(site);
//    int[] ids = automation.getIDsOfNames(new String[] {"settings"});
//    if(ids != null) {
//      automation = automation.getProperty(ids[0]).getAutomation();
//      for(int i=0; ; i++) {
//        OleFunctionDescription functionDescription = automation.getFunctionDescription(i);
//        if(functionDescription == null) {
//          break;
//        }
//        System.err.println(functionDescription.name + ": " + functionDescription.documentation);
//      }
//    }
    return frame;
  }
  
  private boolean setErrorDialogsEnabled(final boolean isErrorDialogEnabled) {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"settings"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"enableErrorDialogs"});
              if(ids != null) {
                result[0] = automation.setProperty(ids[0], new Variant(isErrorDialogEnabled));
              }
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public String getURL() {
    final String[] result = new String[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"url"});
            if(ids != null) {
              result[0] = automation.getProperty(ids[0]).getString();
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"fileName"});
            if(ids != null) {
              result[0] = automation.getProperty(ids[0]).getString();
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public boolean setURL(final String url) {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"url"});
            if(ids != null) {
              result[0] = automation.setProperty(ids[0], new Variant(url == null? "": url));
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"fileName"});
            if(ids != null) {
              result[0] = automation.setProperty(ids[0], new Variant(url == null? "": url));
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public boolean setControlBarVisible(final boolean isControlBarVisible) {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"uiMode"});
            if(ids != null) {
              automation.setProperty(ids[0], new Variant[] {new Variant(isControlBarVisible? "full": "none")});
              result[0] = true;
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"showControls"});
            if(ids != null) {
              automation.setProperty(ids[0], new Variant[] {new Variant(isControlBarVisible)});
              result[0] = true;
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }

  public boolean isControlBarVisible() {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"uiMode"});
            if(ids != null) {
              result[0] = "full".equals(automation.getProperty(ids[0]).getString());
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"showControls"});
            if(ids != null) {
              result[0] = automation.getProperty(ids[0]).getBoolean();
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
//  public boolean setDisplayVisible(final boolean isDisplayVisible) {
//    final boolean[] result = new boolean[1];
//    run(new Runnable() {
//      @Override
//      public void run() {
//        OleAutomation automation = new OleAutomation(clientSite);
//        int[] ids = automation.getIDsOfNames(new String[] {"ShowDisplay"});
//        if(ids != null) {
//          automation.setProperty(ids[0], new Variant[] {new Variant(isDisplayVisible)});
//          result[0] = true;
//        }
//        automation.dispose();
//      }
//    });
//    return result[0];
//  }
//  
//  public boolean isDisplayVisible() {
//    final boolean[] result = new boolean[1];
//    run(new Runnable() {
//      @Override
//      public void run() {
//        OleAutomation automation = new OleAutomation(clientSite);
//        int[] ids = automation.getIDsOfNames(new String[] {"ShowDisplay"});
//        if(ids != null) {
//          result[0] = automation.getProperty(ids[0]).getBoolean();
//        }
//        automation.dispose();
//      }
//    });
//    return result[0];
//  }
  
  public boolean setVolume(final int volume) {
    if(volume < 0 || volume > 100) {
      throw new IllegalArgumentException("The volume must be between 0 and 100");
    }
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"settings"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"volume"});
              if(ids != null) {
                result[0] = automation.setProperty(ids[0], new Variant(volume));
              }
            }
            break;
          }
          case MEDIA_PLAYER: {
            int volume_ = -(int)Math.round(Math.pow((100 - volume) / 2.0, 2));
            int[] ids = automation.getIDsOfNames(new String[] {"volume"});
            if(ids != null) {
              result[0] = automation.setProperty(ids[0], new Variant(volume_));
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public int getVolume() {
    final int[] result = new int[] {-1};
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"settings"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"volume"});
              if(ids != null) {
                result[0] = automation.getProperty(ids[0]).getInt();
              }
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"volume"});
            if(ids != null) {
              int volume = automation.getProperty(ids[0]).getInt();
              volume = 100 - (int)Math.round(Math.sqrt(-volume) * 2);
              result[0] = volume;
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public boolean setStereoBalance(final int stereoBalance) {
    if(stereoBalance < 100 || stereoBalance > 100) {
      throw new IllegalArgumentException("The stereo balance must be between -100 and 100");
    }
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"settings"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"balance"});
              if(ids != null) {
                result[0] = automation.setProperty(ids[0], new Variant(stereoBalance));
              }
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"balance"});
            if(ids != null) {
              result[0] = automation.setProperty(ids[0], new Variant(stereoBalance));
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public int getStereoBalance() {
    final int[] result = new int[] {0};
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"settings"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"balance"});
              if(ids != null) {
                result[0] = automation.getProperty(ids[0]).getInt();
              }
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"balance"});
            if(ids != null) {
              result[0] = automation.getProperty(ids[0]).getInt();
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public boolean setAutoStart(final boolean isAutoStart) {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"settings"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"autoStart"});
              if(ids != null) {
                result[0] = automation.setProperty(ids[0], new Variant(isAutoStart));
              }
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"autoStart"});
            if(ids != null) {
              result[0] = automation.setProperty(ids[0], new Variant(isAutoStart));
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public boolean isAutoStart() {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"settings"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"autoStart"});
              if(ids != null) {
                result[0] = automation.getProperty(ids[0]).getBoolean();
              }
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"autoStart"});
            if(ids != null) {
              result[0] = automation.getProperty(ids[0]).getBoolean();
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
//  public boolean setFullScreen(final boolean isFullScreen) {
//    final boolean[] result = new boolean[1];
//    run(new Runnable() {
//      public void run() {
//        OleAutomation automation = new OleAutomation(clientSite);
//        int[] ids = automation.getIDsOfNames(new String[] {"fullScreen"});
//        if(ids != null) {
//          automation.setProperty(ids[0], new Variant(isFullScreen));
//          result[0] = true;
//        }
//        automation.dispose();
//      }
//    });
//    return result[0];
//  }
//  
//  public boolean isFullScreen() {
//    final boolean[] result = new boolean[1];
//    run(new Runnable() {
//      public void run() {
//        OleAutomation automation = new OleAutomation(clientSite);
//        int[] ids = automation.getIDsOfNames(new String[] {"fullScreen"});
//        if(ids != null) {
//          result[0] = automation.getProperty(ids[0]).getBoolean();
//        }
//        automation.dispose();
//      }
//    });
//    return result[0];
//  }
  
  public boolean setMute(final boolean isMute) {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"settings"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"mute"});
              if(ids != null) {
                result[0] = automation.setProperty(ids[0], new Variant(isMute));
              }
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"mute"});
            if(ids != null) {
              result[0] = automation.setProperty(ids[0], new Variant(isMute));
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public boolean isMute() {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"settings"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"mute"});
              if(ids != null) {
                result[0] = automation.getProperty(ids[0]).getBoolean();
              }
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"mute"});
            if(ids != null) {
              result[0] = automation.getProperty(ids[0]).getBoolean();
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public boolean isPlayEnabled() {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"controls"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"isAvailable"});
              if(ids != null) {
                result[0] = automation.getProperty(ids[0], new Variant[] {new Variant("Play")}).getBoolean();
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
                  result[0] = true;
              }
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public boolean play() {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"controls"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"Play"});
              if(ids != null) {
                automation.invoke(ids[0]);
                result[0] = true;
              }
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"Play"});
            if(ids != null) {
              automation.invoke(ids[0]);
              result[0] = true;
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public boolean isStopEnabled() {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"controls"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"isAvailable"});
              if(ids != null) {
                result[0] = automation.getProperty(ids[0], new Variant[] {new Variant("Stop")}).getBoolean();
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
                  result[0] = true;
              }
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public boolean stop() {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"controls"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"Stop"});
              if(ids != null) {
                automation.invoke(ids[0]);
                result[0] = true;
              }
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"Stop"});
            if(ids != null) {
              automation.invoke(ids[0]);
              result[0] = true;
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public boolean isPauseEnabled() {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"controls"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"isAvailable"});
              if(ids != null) {
                result[0] = automation.getProperty(ids[0], new Variant[] {new Variant("Pause")}).getBoolean();
              }
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"playState"});
            if(ids != null) {
              switch(automation.getProperty(ids[0]).getInt()) {
                case 2: // File loaded and playing
                  result[0] = true;
              }
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  public boolean pause() {
    final boolean[] result = new boolean[1];
    run(new Runnable() {
      public void run() {
        OleAutomation automation = new OleAutomation(site);
        switch(type) {
          case WM_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"controls"});
            if(ids != null) {
              automation = automation.getProperty(ids[0]).getAutomation();
              ids = automation.getIDsOfNames(new String[] {"Pause"});
              if(ids != null) {
                automation.invoke(ids[0]);
                result[0] = true;
              }
            }
            break;
          }
          case MEDIA_PLAYER: {
            int[] ids = automation.getIDsOfNames(new String[] {"Pause"});
            if(ids != null) {
              automation.invoke(ids[0]);
              result[0] = true;
            }
            break;
          }
        }
        automation.dispose();
      }
    });
    return result[0];
  }
  
  protected Component createEmbeddableComponent() {
    return super.createEmbeddableComponent();
  }
  
}
