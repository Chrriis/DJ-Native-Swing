/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * A VLC player decorator is a component that wraps the rendering component and is added to the VLC player
 * to provide the various button bars, fields and menus.<br>
 * Generally, it is not needed to create a custom subclass: it is usually enough to subclass a default VLC
 * player decorator and override certain methods.
 * @author Christopher Deckers
 */
public abstract class VLCPlayerDecorator extends JPanel {

  public VLCPlayerDecorator() {
    super(new BorderLayout());
  }

  /**
   * Set whether the control bar is visible.
   * @param isControlBarVisible true if the control bar should be visible, false otherwise.
   */
  public abstract void setControlBarVisible(boolean isControlBarVisible);

  /**
   * Indicate whether the control bar is visible.
   * @return true if the control bar is visible.
   */
  public abstract boolean isControlBarVisible();

}
