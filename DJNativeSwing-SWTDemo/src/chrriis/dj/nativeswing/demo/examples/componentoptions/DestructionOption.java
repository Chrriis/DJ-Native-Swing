/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.componentoptions;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import chrriis.common.WebServer;
import chrriis.dj.nativeswing.Disposable;
import chrriis.dj.nativeswing.demo.examples.flashplayer.SimpleFlashExample;
import chrriis.dj.nativeswing.ui.JFlashPlayer;
import chrriis.dj.nativeswing.ui.NativeComponent;
import chrriis.dj.nativeswing.ui.NativeComponent.Options.Destruction;

/**
 * @author Christopher Deckers
 */
public class DestructionOption extends JPanel implements Disposable {

  protected JFlashPlayer flashPlayer;
  
  public DestructionOption() {
    super(new BorderLayout(0, 0));
    NativeComponent.getNextInstanceOptions().setDestruction(Destruction.ON_FINALIZATION);
    flashPlayer = new JFlashPlayer();
    flashPlayer.setControlBarVisible(false);
    String resourceURL = WebServer.getDefaultWebServer().getClassPathResourceURL(SimpleFlashExample.class.getName(), "resource/Movement-pointer_or_click.swf");
    flashPlayer.setURL(resourceURL);
    add(flashPlayer, BorderLayout.CENTER);
    JButton addRemoveButton = new JButton("Add/Remove component");
    addRemoveButton.addActionListener(new ActionListener() {
      protected boolean isAdded = true;
      public void actionPerformed(ActionEvent e) {
        if(isAdded) {
          remove(flashPlayer);
        } else {
          add(flashPlayer, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
        isAdded = !isAdded;
      }
    });
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    buttonPanel.add(addRemoveButton);
    add(buttonPanel, BorderLayout.SOUTH);
  }
  
  public void dispose() {
    flashPlayer.dispose();
  }
  
}
