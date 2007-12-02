/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common.ui.source;

import java.awt.Color;
import java.awt.Font;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledEditorKit;


/**
 * A definition of tokens.
 * @version 1.0 2004.02.09
 * @author Christopher Deckers (chrriis@nextencia.net)
 */
public class RegExJavaTypes extends RegExTypes {
  public static final String CHAR = "char";
  public static final String TEXT = "text";
  public static final String ATOM = "atom";
  // public static final String NUMBER = "number";
  public static final String JAVADOC_COMMENT = "JavadocComment";
  public static final String MULTI_LINE_COMMENT = "MultiLineComment";
  public static final String SINGLE_LINE_COMMENT = "SingleLineComment";
  
  protected static final String STYLE = "Style";

  public RegExJavaTypes() {
    addTokenType(JAVADOC_COMMENT, "/\\*\\*[^/](?:(?!\\*/).)*(?:\\*/)?", new Color(63, 95, 191), Font.PLAIN);
    addTokenType(MULTI_LINE_COMMENT, "/\\*(?:(?!\\*/).)*(?:\\*/)?", new Color(63, 127, 95), Font.PLAIN);
    addTokenType(SINGLE_LINE_COMMENT, "//(?:[^\n]*)", new Color(63, 127, 95), Font.PLAIN);
    addTokenType(ATOM, "[a-zA-Z]\\w*", null, Font.PLAIN);
    addTokenType(CHAR, "'(?>(?:\\\\.)|.)'", new Color(42, 0, 255), Font.PLAIN);
    addTokenType(TEXT, "\"(?:(\\\\.)*(?:(?![\"\n]).))*[\"]?", new Color(42, 0, 255), Font.PLAIN);
//    addTokenType(CHAR, "'(?:\\\\[^']+|[^'])'", Color.blue, Font.PLAIN);
    // addTokenType(NUMBER, "[0-9]+(\\.[0-9]+)?", Color.magenta, Font.PLAIN);

    putStyleName("void", STYLE);
    putStyleName("null", STYLE);
    putStyleName("boolean", STYLE);
    putStyleName("byte", STYLE);
    putStyleName("char", STYLE);
    putStyleName("short", STYLE);
    putStyleName("int", STYLE);
    putStyleName("long", STYLE);
    putStyleName("float", STYLE);
    putStyleName("double", STYLE);
    putStyleName("import", STYLE);
    putStyleName("package", STYLE);
    putStyleName("class", STYLE);
    putStyleName("interface", STYLE);
    putStyleName("extends", STYLE);
    putStyleName("implements", STYLE);
    putStyleName("public", STYLE);
    putStyleName("protected", STYLE);
    putStyleName("private", STYLE);
    putStyleName("abstract", STYLE);
    putStyleName("static", STYLE);
    putStyleName("final", STYLE);
    putStyleName("native", STYLE);
    putStyleName("return", STYLE);
    putStyleName("volatile", STYLE);
    putStyleName("transient", STYLE);
    putStyleName("throws", STYLE);
    putStyleName("synchronized", STYLE);
    putStyleName("try", STYLE);
    putStyleName("catch", STYLE);
    putStyleName("finally", STYLE);
    putStyleName("throw", STYLE);
    putStyleName("new", STYLE);
    putStyleName("super", STYLE);
    putStyleName("this", STYLE);
    putStyleName("true", STYLE);
    putStyleName("false", STYLE);
    putStyleName("if", STYLE);
    putStyleName("else", STYLE);
    putStyleName("for", STYLE);
    putStyleName("do", STYLE);
    putStyleName("while", STYLE);
    putStyleName("switch", STYLE);
    putStyleName("case", STYLE);
    putStyleName("break", STYLE);
    putStyleName("continue", STYLE);
    putStyleName("goto", STYLE);
    putStyleName("instanceof", STYLE);
    putStyleName("strictfp", STYLE);
    putStyleName("const", STYLE);
    putStyleName("default", STYLE);
    addWordStyle(STYLE, new Color(120, 0, 100), Font.BOLD);
  }

  public static EditorKit getEditorKit() {
    return new StyledEditorKit() {
      public Document createDefaultDocument() {
        return new EditorKitDocument(new RegExJavaTypes()) {
          public void insertString(int offset, String text, AttributeSet style) throws BadLocationException {
            super.insertString(offset, text, style);
          }
        };
      }
    };
  }

}
