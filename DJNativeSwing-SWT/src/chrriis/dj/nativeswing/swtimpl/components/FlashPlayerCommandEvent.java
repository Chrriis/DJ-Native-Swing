/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.components;

import java.util.EventObject;


/**
 * @author Christopher Deckers
 */
public class FlashPlayerCommandEvent extends EventObject {

  private JFlashPlayer flashPlayer;
  private String command;
  private Object[] parameters;

  public FlashPlayerCommandEvent(JFlashPlayer flashPlayer, String command, Object[] parameters) {
    super(flashPlayer);
    this.flashPlayer = flashPlayer;
    this.command = command;
    this.parameters = parameters;
  }

  public JFlashPlayer getFlashPlayer() {
    return flashPlayer;
  }

  public String getCommand() {
    return command;
  }

  public Object[] getParameters() {
    return parameters;
  }

}
