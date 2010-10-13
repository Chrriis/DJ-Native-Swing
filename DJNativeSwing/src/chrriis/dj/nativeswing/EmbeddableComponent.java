/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing;

import javax.swing.JPanel;

/**
 * @author Christopher Deckers
 */
class EmbeddableComponent extends JPanel {

  public EmbeddableComponent() {
    super(new ClipLayout());
  }

}
