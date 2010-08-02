/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import chrriis.common.WebServer;
import chrriis.dj.nativeswing.swtimpl.components.VLCInput.VLCMediaState;

/**
 * @author Christopher Deckers
 */
public class DefaultVLCPlayerDecorator extends VLCPlayerDecorator {

  public static enum VLCDecoratorComponentType {
    PLAY_BUTTON,
    PAUSE_BUTTON,
    STOP_BUTTON,
    VOLUME_BUTTON_ON,
    VOLUME_BUTTON_OFF,
  }

  private final ResourceBundle RESOURCES;

  {
    String className = JVLCPlayer.class.getName();
    RESOURCES = ResourceBundle.getBundle(className.substring(0, className.lastIndexOf('.')).replace('.', '/') + "/resource/VLCPlayer");
  }

  private int lastVolume = 50;

  public class VLCPlayerControlBar extends JPanel {

    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JSlider seekBarSlider;
    private volatile boolean isAdjustingSeekBar;
    private volatile Thread updateThread;
    private JLabel timeLabel;
    private JButton volumeButton;
    private JSlider volumeSlider;
    private boolean isAdjustingVolume;

    private WebBrowserAdapter webBrowserListener;

    VLCPlayerControlBar() {
      super(new BorderLayout());
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
      playButton = new JButton();
      configureComponent(playButton, VLCDecoratorComponentType.PLAY_BUTTON);
      playButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          vlcPlayer.getVLCPlaylist().play();
        }
      });
      pauseButton = new JButton();
      configureComponent(pauseButton, VLCDecoratorComponentType.PAUSE_BUTTON);
      pauseButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          vlcPlayer.getVLCPlaylist().togglePause();
        }
      });
      stopButton = new JButton();
      configureComponent(stopButton, VLCDecoratorComponentType.STOP_BUTTON);
      stopButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          vlcPlayer.getVLCPlaylist().stop();
        }
      });
      addControlBarComponents(this, buttonPanel);
      seekBarSlider = new JSlider(0, 10000, 0);
      seekBarSlider.setVisible(false);
      seekBarSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if(!isAdjustingSeekBar) {
            vlcPlayer.getVLCInput().setRelativePosition(((float)seekBarSlider.getValue()) / 10000);
          }
        }
      });
      add(seekBarSlider, BorderLayout.NORTH);
      JPanel volumePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
      volumeButton = new JButton();
      Insets margin = volumeButton.getMargin();
      margin.left = Math.min(2, margin.left);
      margin.right = Math.min(2, margin.left);
      volumeButton.setMargin(margin);
      volumeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          vlcPlayer.getVLCAudio().toggleMute();
        }
      });
      volumePanel.add(volumeButton);
      volumeSlider = new JSlider();
      volumeSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if(!isAdjustingVolume) {
            vlcPlayer.getVLCAudio().setVolume(volumeSlider.getValue());
          }
        }
      });
      volumeSlider.setPreferredSize(new Dimension(60, volumeSlider.getPreferredSize().height));
      volumePanel.add(volumeSlider);
      volumeButton.setEnabled(false);
      volumeSlider.setEnabled(false);
      GridBagLayout gridBag = new GridBagLayout();
      GridBagConstraints cons = new GridBagConstraints();
      JPanel buttonBarPanel = new JPanel(gridBag);
      cons.gridx = 0;
      cons.gridy = 0;
      cons.weightx = 1.0;
      cons.anchor = GridBagConstraints.WEST;
      cons.fill = GridBagConstraints.HORIZONTAL;
      timeLabel = new JLabel(" ");
      timeLabel.setPreferredSize(new Dimension(0, timeLabel.getPreferredSize().height));
      gridBag.setConstraints(timeLabel, cons);
      buttonBarPanel.add(timeLabel);
      cons.gridx++;
      cons.weightx = 0.0;
      cons.anchor = GridBagConstraints.CENTER;
      cons.fill = GridBagConstraints.NONE;
      gridBag.setConstraints(buttonPanel, cons);
      buttonBarPanel.add(buttonPanel);
      buttonBarPanel.setMinimumSize(buttonBarPanel.getPreferredSize());
      cons.gridx++;
      cons.weightx = 1.0;
      cons.anchor = GridBagConstraints.EAST;
      cons.fill = GridBagConstraints.HORIZONTAL;
      volumePanel.setPreferredSize(new Dimension(0, volumePanel.getPreferredSize().height));
      gridBag.setConstraints(volumePanel, cons);
      buttonBarPanel.add(volumePanel);
      add(buttonBarPanel, BorderLayout.CENTER);
      adjustButtonState();
      updateControlBar();
      webBrowserListener = new WebBrowserAdapter() {
        @Override
        public void locationChanged(WebBrowserNavigationEvent e) {
          adjustButtonState();
        }
      };
      vlcPlayer.getWebBrowser().addWebBrowserListener(webBrowserListener);
      // TODO: add components through helper methods
//      addControlBarComponents(this);
    }

    void disconnect() {
      stopUpdateThread();
      vlcPlayer.getWebBrowser().removeWebBrowserListener(webBrowserListener);
    }

    void adjustButtonState() {
      String resourceLocation = vlcPlayer.getWebBrowser().getResourceLocation();
      boolean isEnabled = resourceLocation != null && resourceLocation.startsWith(WebServer.getDefaultWebServer().getURLPrefix());
      playButton.setEnabled(isEnabled);
      pauseButton.setEnabled(isEnabled);
      stopButton.setEnabled(isEnabled);
      volumeButton.setEnabled(isEnabled);
      volumeSlider.setEnabled(isEnabled);
      if(isEnabled) {
        adjustVolumePanel();
        startUpdateThread();
      }
    }

    private boolean isMute;
    private int volume = -2; // this will trigger proper computation of states

    void adjustVolumePanel() {
      VLCAudio vlcAudio = vlcPlayer.getVLCAudio();
      boolean isMute = vlcAudio.isMute();
      int volume = vlcAudio.getVolume();
      volumeButton.setEnabled(true);
      volumeSlider.setEnabled(!isMute);
      if(isMute == this.isMute && this.volume == volume) {
        return;
      }
      if(isMute) {
        configureComponent(volumeButton, VLCDecoratorComponentType.VOLUME_BUTTON_OFF);
      } else {
        configureComponent(volumeButton, VLCDecoratorComponentType.VOLUME_BUTTON_ON);
      }
      isAdjustingVolume = true;
      if(!isMute) {
        volumeSlider.setValue(volume);
        lastVolume = volume;
      } else {
        volumeSlider.setValue(lastVolume);
      }
      isAdjustingVolume = false;
      this.isMute = isMute;
      this.volume = volume;
    }

    public JButton getPlayButton() {
      return playButton;
    }

    public JButton getPauseButton() {
      return pauseButton;
    }

    public JButton getStopButton() {
      return stopButton;
    }

    @Override
    public void removeNotify() {
      stopUpdateThread();
      super.removeNotify();
    }

    @Override
    public void addNotify() {
      super.addNotify();
      adjustButtonState();
    }

    private void stopUpdateThread() {
      updateThread = null;
    }

    private void startUpdateThread() {
      if(updateThread != null) {
        return;
      }
      if(vlcPlayer.isNativePeerDisposed()) {
        return;
      }
      updateThread = new Thread("NativeSwing - VLC Player control bar update") {
        @Override
        public void run() {
          final Thread currentThread = this;
          while(currentThread == updateThread) {
            if(vlcPlayer.isNativePeerDisposed()) {
              stopUpdateThread();
              return;
            }
            try {
              sleep(1000);
            } catch(Exception e) {}
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                if(currentThread != updateThread) {
                  return;
                }
                if(!vlcPlayer.isNativePeerValid()) {
                  return;
                }
                updateControlBar();
              }
            });
          }
        }
      };
      updateThread.setDaemon(true);
      updateThread.start();
    }

    private void updateControlBar() {
      VLCInput vlcInput = vlcPlayer.getVLCInput();
      VLCMediaState state = vlcInput.getMediaState();
      boolean isValid = state == VLCMediaState.OPENING || state == VLCMediaState.BUFFERING || state == VLCMediaState.PLAYING || state == VLCMediaState.PAUSED || state == VLCMediaState.STOPPING;
      if(isValid) {
        int time = vlcInput.getAbsolutePosition();
        int length = vlcInput.getDuration();
        isValid = time >= 0 && length > 0;
        if(isValid) {
          isAdjustingSeekBar = true;
          seekBarSlider.setValue(Math.round(time * 10000f / length));
          isAdjustingSeekBar = false;
          timeLabel.setText(getTimeDisplay(time, length));
        }
      }
      if(!isValid) {
        timeLabel.setText("");
      }
      seekBarSlider.setVisible(isValid);
      adjustVolumePanel();
    }

  }

  private JVLCPlayer vlcPlayer;
  private VLCPlayerControlBar controlBar;

  public DefaultVLCPlayerDecorator(JVLCPlayer vlcPlayer, Component renderingComponent) {
    this.vlcPlayer = vlcPlayer;
    nativeComponentBorderContainerPane = new JPanel(new BorderLayout());
    nativeComponentBorderContainerPane.add(renderingComponent, BorderLayout.CENTER);
    add(nativeComponentBorderContainerPane, BorderLayout.CENTER);
    setControlBarVisible(false);
  }

  protected JVLCPlayer getFlashPlayer() {
    return vlcPlayer;
  }

  private JPanel nativeComponentBorderContainerPane;

  private void adjustBorder() {
    nativeComponentBorderContainerPane.setBorder(getInnerAreaBorder());
  }

  /**
   * Return the border to use for the inner area, which by default return a border if at least one of the bars is visible.
   * Note that this method is called every time the visibility of a bar changes.
   */
  protected Border getInnerAreaBorder() {
    Border border;
    if(isControlBarVisible()) {
      border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
    } else {
      border = null;
    }
    return border;
  }

  @Override
  public void setControlBarVisible(boolean isControlBarVisible) {
    if(isControlBarVisible == isControlBarVisible()) {
      return;
    }
    if(isControlBarVisible) {
      controlBar = new VLCPlayerControlBar();
      add(controlBar, BorderLayout.SOUTH);
    } else {
      remove(controlBar);
      controlBar.disconnect();
      controlBar = null;
    }
    revalidate();
    repaint();
    adjustBorder();
  }

  @Override
  public boolean isControlBarVisible() {
    return controlBar != null;
  }

  /**
   * Get the time to display in the time display area.
   * @param currentTime The current playing time.
   * @param totalTime The total playing time.
   * @return a text, which by default returns a time in the form "02:45 / 04:23".
   */
  protected String getTimeDisplay(int currentTime, int totalTime) {
    boolean showHours = totalTime >= 3600000;
    return formatTime(currentTime, showHours) + " / " + formatTime(totalTime, showHours);
  }

  private String formatTime(int milliseconds, boolean showHours) {
    int seconds = milliseconds / 1000;
    int hours = seconds / 3600;
    int minutes = (seconds % 3600) / 60;
    seconds = seconds % 60;
    StringBuilder sb = new StringBuilder();
    if(hours != 0 || showHours) {
      sb.append(hours).append(':');
    }
    sb.append(minutes < 10? "0": "").append(minutes).append(':');
    sb.append(seconds < 10? "0": "").append(seconds);
    return sb.toString();
  }

  /**
   * Add the components that compose the control bar.
   * Overriden versions do not need to call their super implementation, instead they can selectively add certain default components, for example: <code>buttonContainer.add(controlBar.getPlayButton())</code>.
   */
  protected void addControlBarComponents(VLCPlayerControlBar controlBar, JComponent buttonContainer) {
    buttonContainer.add(controlBar.getPlayButton());
    buttonContainer.add(controlBar.getPauseButton());
    buttonContainer.add(controlBar.getStopButton());
  }

  /**
   * Configure a component (its text, icon, tooltip, etc.).
   */
  protected void configureComponent(JComponent c, VLCDecoratorComponentType componentType) {
    switch(componentType) {
      case PLAY_BUTTON: {
        ((AbstractButton)c).setIcon(createIcon("PlayIcon"));
        ((AbstractButton)c).setToolTipText(RESOURCES.getString("PlayText"));
        return;
      }
      case PAUSE_BUTTON: {
        ((AbstractButton)c).setIcon(createIcon("PauseIcon"));
        ((AbstractButton)c).setToolTipText(RESOURCES.getString("PauseText"));
        return;
      }
      case STOP_BUTTON: {
        ((AbstractButton)c).setIcon(createIcon("StopIcon"));
        ((AbstractButton)c).setToolTipText(RESOURCES.getString("StopText"));
        return;
      }
      case VOLUME_BUTTON_OFF: {
        ((AbstractButton)c).setIcon(createIcon("VolumeOffIcon"));
        ((AbstractButton)c).setToolTipText(RESOURCES.getString("VolumeOffText"));
        return;
      }
      case VOLUME_BUTTON_ON: {
        ((AbstractButton)c).setIcon(createIcon("VolumeOnIcon"));
        ((AbstractButton)c).setToolTipText(RESOURCES.getString("VolumeOnText"));
        return;
      }
    }
    throw new IllegalStateException("Type not handled: " + componentType);
  }

  private Icon createIcon(String resourceKey) {
    String value = RESOURCES.getString(resourceKey);
    return value.length() == 0? null: new ImageIcon(JVLCPlayer.class.getResource(value));
  }

}
