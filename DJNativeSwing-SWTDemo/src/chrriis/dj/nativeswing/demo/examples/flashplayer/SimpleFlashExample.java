/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.demo.examples.flashplayer;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import chrriis.common.WebServer;
import chrriis.dj.nativeswing.ui.JFlashPlayer;

/**
 * @author Christopher Deckers
 */
public class SimpleFlashExample extends JPanel {

  public SimpleFlashExample() {
    super(new BorderLayout(0, 0));
    JFlashPlayer flashPlayer = new JFlashPlayer();
    String resourceURL = WebServer.getDefaultWebServer().getClassPathResourceURL(SimpleFlashExample.class.getName(), "resource/Movement-pointer_or_click.swf");
    flashPlayer.setURL(resourceURL);
    add(flashPlayer, BorderLayout.CENTER);
  }
  
}
