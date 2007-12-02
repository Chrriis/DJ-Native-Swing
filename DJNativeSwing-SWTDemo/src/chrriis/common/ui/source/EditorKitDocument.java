/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common.ui.source;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

public class EditorKitDocument extends DefaultStyledDocument {

  protected RegExTypes types;

  public EditorKitDocument(RegExTypes types) {
    this.types = types;
    Style defaultStyle = getStyle("default");
    StyleConstants.setFontFamily(defaultStyle, "Monospaced");
    StyleConstants.setFontSize(defaultStyle, 13);
    types.setStyles(this);
  }

  public void insertString(int offset, String text, AttributeSet style) throws BadLocationException {
    super.insertString(offset, text, style);
    highlightSyntax();
  }

  public void remove(int offset, int length) throws BadLocationException {
    super.remove(offset, length);
    highlightSyntax();
  }

  public void highlightSyntax() {
    try {
      int length = getLength();
      String text = getText(0, length);
      setCharacterAttributes(0, length, getStyle("default"), true);
      RegExTokenizer tokenizer = new RegExTokenizer(types, text);
      int typeCount = types.getTypeCount();
      for (RegExTokenizer.Token token; (token = tokenizer.nextToken()) != null; ) {
        int position = token.getPosition();
        String type = token.getType();
        String word = token.getToken();
        int wLength = word.length();
        for (int i = 0; i < typeCount; i++) {
          String name = types.getName(i);
          if (type.equals(name)) {
            if (types.getColor(i) == null) {
              String style = types.getStyleName(word);
              if (style != null) {
                setCharacterAttributes(position, wLength, getStyle(style), false);
              }
            } else {
              setCharacterAttributes(position, wLength, getStyle(name), false);
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
