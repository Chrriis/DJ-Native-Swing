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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class RegExTypes {

  public static class WordStyle {
    protected String name;
    protected Color color;
    protected int fontstyle;

    public WordStyle(String name2, Color color2, int fontstyle2) {
      name = name2;
      color = color2;
      fontstyle = fontstyle2;
    }
  }

  public static class Type {
    protected String name;
    protected String expression;
    protected Color color;
    protected int fontstyle;

    public Type(String name, String expression, Color color, int fontstyle) {
      this.name = name;
      this.expression = expression;
      this.color = color;
      this.fontstyle = fontstyle;
    }
  }

  protected List<Type> typeList;
  protected List<WordStyle> wordstyleList;

  public RegExTypes() {
    typeList = new ArrayList<Type>();
    wordstyleList = new ArrayList<WordStyle>();
  }

  public void addTokenType(String name, String expr, Color color, int fontstyle) {
    typeList.add(new Type(name, expr, color, fontstyle));
  }

  public void addWordStyle(String name, Color color, int fontstyle) {
    wordstyleList.add(new WordStyle(name, color, fontstyle));
  }

  protected Type getType(int index) {
    return typeList.get(index);
  }

  public String getExpr(int index) {
    return getType(index).expression;
  }

  public void setStyles(StyledDocument document) {
    for (int i = 0; i < typeList.size(); i++) {
      Type type = getType(i);
      String name = type.name;
      Color color = type.color;
      int fontStyle = type.fontstyle;
      setStyle(document, name, color, fontStyle);
    }

    for (int i = 0; i < wordstyleList.size(); i++) {
      WordStyle wordStyle = wordstyleList.get(i);
      String name = wordStyle.name;
      Color color = wordStyle.color;
      int fontStyle = wordStyle.fontstyle;
      setStyle(document, name, color, fontStyle);
    }

  }

  private void setStyle(StyledDocument doc, String name, Color color, int fontStyle) {
    if (color != null) {
      Style style = doc.addStyle(name, null);
      StyleConstants.setForeground(style, color);
      if (fontStyle == Font.BOLD)
        StyleConstants.setBold(style, true);
      if (fontStyle == Font.ITALIC)
        StyleConstants.setItalic(style, true);
    }
  }

  public String getExpression() {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < getTypeCount(); i++) {
      if (i > 0)
        buffer.append('|');
      buffer.append('(');
      buffer.append(getExpr(i));
      buffer.append(')');
    }
    return buffer.toString();
  }

  public Pattern getPattern() {
    return Pattern.compile(getExpression(), Pattern.DOTALL);
  }

  public Matcher getMatcher(String text) {
    return getPattern().matcher(text);
  }

  protected Map<String, String> wordToStyleMap = new HashMap<String, String>();

  public String getStyleName(String word) {
    return wordToStyleMap.get(word);
  }

  public void putStyleName(String word, String styleName) {
    wordToStyleMap.put(word, styleName);
  }

  public String getName(int index) {
    return typeList.get(index).name;
  }

  public int getTypeCount() {
    return typeList.size();
  }

  public Object getColor(int index) {
    return typeList.get(index).color;
  }
}
