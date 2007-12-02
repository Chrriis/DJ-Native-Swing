/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common.ui.source;

import java.util.regex.Matcher;

public class RegExTokenizer {

  public static class Token {
    public String token;
    public String type;
    protected int position;

    public Token(String token, String type, int position) {
      this.token = token;
      this.type = type;
      this.position = position;
    }

    public String getToken() {
      return token;
    }

    public String getType() {
      return type;
    }

    public int getPosition() {
      return position;
    }
  }

  protected RegExTypes types;
  protected Matcher matcher;

  public RegExTokenizer(RegExTypes types, String text) {
    this.types = types;
    matcher = types.getMatcher(text);
  }

  protected Token getToken(int position) {
    int count = types.getTypeCount();
    for (int i = 1; i <= count; i++) {
      String token = matcher.group(i);
      if (token != null) {
        String type = types.getName(i - 1);
        return new Token(token, type, position);
      }
    }
    return null;
  }

  public Token nextToken() {
    if (matcher.find()) {
      return getToken(matcher.start());
    }
    return null;
  }

}
