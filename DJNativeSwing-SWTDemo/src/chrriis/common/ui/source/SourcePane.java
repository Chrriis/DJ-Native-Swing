/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common.ui.source;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.Reader;

import javax.swing.JEditorPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;

public class SourcePane extends JEditorPane {

  public SourcePane(Reader reader) throws Exception {
    setFont(new Font("Monospaced", Font.PLAIN, 12));
    setEditorKit(RegExJavaTypes.getEditorKit());
    read(new TabFilterReader(reader), null);
    setEditable(false);
  }

  /**
   * Get the viewport size so that text does not wrap but horizontal scrollbar
   * appears instead.
   */
  public boolean getScrollableTracksViewportWidth() {
    if (getParent() instanceof JViewport) {
      return getParent().getWidth() > getUI().getPreferredSize(this).width;
    }
    return false;
  }
  
  @SuppressWarnings("deprecation")
  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    if(orientation == SwingConstants.VERTICAL) {
      return Toolkit.getDefaultToolkit().getFontMetrics(getFont()).getHeight();
    }
    return super.getScrollableUnitIncrement(visibleRect, orientation, direction);
  }

}
