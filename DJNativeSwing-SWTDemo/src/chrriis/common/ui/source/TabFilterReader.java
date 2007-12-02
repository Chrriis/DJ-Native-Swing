/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common.ui.source;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public class TabFilterReader extends FilterReader {

  protected Reader reader;
  protected StringBuffer sb = new StringBuffer();

  public TabFilterReader(Reader reader) {
    super(reader);
    this.reader = reader;
  }
  public int read() throws IOException {
    char c = (char)super.read();
    if(c == '\t') {
      sb.append(' ');
      return ' ';
    }
    if(sb.length() > 0) {
      c = sb.charAt(0);
      sb.deleteCharAt(0);
    }
    return c;
  }
  public int read(char[] cbuf, int off, int len) throws IOException {
    int length = sb.length();
    if(length > 0) {
      int count = Math.min(len, length);
      sb.getChars(0, count, cbuf, off);
      sb.delete(0, count);
      return count;
    }
    int result = super.read(cbuf, off, len);
    if(result > 0) {
      StringBuffer sb2 = new StringBuffer();
      for(int i=off; i<result + off; i++) {
        char c = cbuf[i];
        if(c == '\t') {
          sb2.append("  ");
        } else {
          sb2.append(c);
        }
      }
      if(sb2.length() == result) {
        return result;
      }
      int count = Math.min(len, sb2.length());
      sb2.getChars(0, count, cbuf, off);
      sb2.delete(0, count);
      return count;
    }
    return result;
  }
}
