/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common;

import java.io.UnsupportedEncodingException;

/**
 * @author Christopher Deckers
 */
class Base64 {

  private Base64() {}

  private static char toChar(int index, boolean isURLSafe) {
    char c;
    switch(index) {
      case 0: c = 'A'; break;
      case 1: c = 'B'; break;
      case 2: c = 'C'; break;
      case 3: c = 'D'; break;
      case 4: c = 'E'; break;
      case 5: c = 'F'; break;
      case 6: c = 'G'; break;
      case 7: c = 'H'; break;
      case 8: c = 'I'; break;
      case 9: c = 'J'; break;
      case 10: c = 'K'; break;
      case 11: c = 'L'; break;
      case 12: c = 'M'; break;
      case 13: c = 'N'; break;
      case 14: c = 'O'; break;
      case 15: c = 'P'; break;
      case 16: c = 'Q'; break;
      case 17: c = 'R'; break;
      case 18: c = 'S'; break;
      case 19: c = 'T'; break;
      case 20: c = 'U'; break;
      case 21: c = 'V'; break;
      case 22: c = 'W'; break;
      case 23: c = 'X'; break;
      case 24: c = 'Y'; break;
      case 25: c = 'Z'; break;
      case 26: c = 'a'; break;
      case 27: c = 'b'; break;
      case 28: c = 'c'; break;
      case 29: c = 'd'; break;
      case 30: c = 'e'; break;
      case 31: c = 'f'; break;
      case 32: c = 'g'; break;
      case 33: c = 'h'; break;
      case 34: c = 'i'; break;
      case 35: c = 'j'; break;
      case 36: c = 'k'; break;
      case 37: c = 'l'; break;
      case 38: c = 'm'; break;
      case 39: c = 'n'; break;
      case 40: c = 'o'; break;
      case 41: c = 'p'; break;
      case 42: c = 'q'; break;
      case 43: c = 'r'; break;
      case 44: c = 's'; break;
      case 45: c = 't'; break;
      case 46: c = 'u'; break;
      case 47: c = 'v'; break;
      case 48: c = 'w'; break;
      case 49: c = 'x'; break;
      case 50: c = 'y'; break;
      case 51: c = 'z'; break;
      case 52: c = '0'; break;
      case 53: c = '1'; break;
      case 54: c = '2'; break;
      case 55: c = '3'; break;
      case 56: c = '4'; break;
      case 57: c = '5'; break;
      case 58: c = '6'; break;
      case 59: c = '7'; break;
      case 60: c = '8'; break;
      case 61: c = '9'; break;
      case 62: c = isURLSafe? '-': '+'; break;
      case 63: c = isURLSafe? '_': '/'; break;
      default: throw new RuntimeException("Cannot happen.");
    }
    return c;
  }

  private static int fromChar(char c) {
    int index;
    switch(c) {
      case 'A': index = 0; break;
      case 'B': index = 1; break;
      case 'C': index = 2; break;
      case 'D': index = 3; break;
      case 'E': index = 4; break;
      case 'F': index = 5; break;
      case 'G': index = 6; break;
      case 'H': index = 7; break;
      case 'I': index = 8; break;
      case 'J': index = 9; break;
      case 'K': index = 10; break;
      case 'L': index = 11; break;
      case 'M': index = 12; break;
      case 'N': index = 13; break;
      case 'O': index = 14; break;
      case 'P': index = 15; break;
      case 'Q': index = 16; break;
      case 'R': index = 17; break;
      case 'S': index = 18; break;
      case 'T': index = 19; break;
      case 'U': index = 20; break;
      case 'V': index = 21; break;
      case 'W': index = 22; break;
      case 'X': index = 23; break;
      case 'Y': index = 24; break;
      case 'Z': index = 25; break;
      case 'a': index = 26; break;
      case 'b': index = 27; break;
      case 'c': index = 28; break;
      case 'd': index = 29; break;
      case 'e': index = 30; break;
      case 'f': index = 31; break;
      case 'g': index = 32; break;
      case 'h': index = 33; break;
      case 'i': index = 34; break;
      case 'j': index = 35; break;
      case 'k': index = 36; break;
      case 'l': index = 37; break;
      case 'm': index = 38; break;
      case 'n': index = 39; break;
      case 'o': index = 40; break;
      case 'p': index = 41; break;
      case 'q': index = 42; break;
      case 'r': index = 43; break;
      case 's': index = 44; break;
      case 't': index = 45; break;
      case 'u': index = 46; break;
      case 'v': index = 47; break;
      case 'w': index = 48; break;
      case 'x': index = 49; break;
      case 'y': index = 50; break;
      case 'z': index = 51; break;
      case '0': index = 52; break;
      case '1': index = 53; break;
      case '2': index = 54; break;
      case '3': index = 55; break;
      case '4': index = 56; break;
      case '5': index = 57; break;
      case '6': index = 58; break;
      case '7': index = 59; break;
      case '8': index = 60; break;
      case '9': index = 61; break;
      case '-':
      case '+':
        index = 62;
        break;
      case '_':
      case '/':
        index = 63;
        break;
      default: throw new RuntimeException("Cannot happen.");
    }
    return index;
  }

  public static String encode(String s, boolean isURLSafe) {
    byte[] bytes;
    try {
      bytes = s.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    char[] chars = new char[(bytes.length + 2) / 3 * 4 - (2 - ((bytes.length + 2) % 3))];
    for(int i=0; i<chars.length; i++) {
      int index;
      switch(i % 4) {
        case 0: {
          int n = i / 4 * 3;
          index = (bytes[n] & 0xFF) >> 2;
          break;
        }
        case 1: {
          int n = (i - 1) / 4 * 3;
          if(n + 1 < bytes.length) {
            index = ((bytes[n] & 0x3) << 4) | ((bytes[n + 1] & 0xFF) >> 4);
          } else {
            index = (bytes[n] & 0x3) << 4;
          }
          break;
        }
        case 2: {
          int n = (i - 2) / 4 * 3 + 1;
          if(n + 1 < bytes.length) {
            index = ((bytes[n] & 0xF) << 2) | ((bytes[n + 1] & 0xFF) >> 6);
          } else {
            index = (bytes[n] & 0xF) << 2;
          }
          break;
        }
        case 3: {
          int n = (i - 3) / 4 * 3 + 2;
          index = bytes[n] & 0x3F;
          break;
        }
        default:
          throw new RuntimeException("Cannot happen.");
      }
      chars[i] = toChar(index, isURLSafe);
    }
    return new String(chars);
  }

  public static String decode(String s) {
    int length = s.length();
    byte[] bytes = new byte[(length + 2) / 4 * 3 - (2 - ((length + 2) % 4))];
    for(int i=0; i<bytes.length; i++) {
      switch(i % 3) {
        case 0: {
          int n = i / 3 * 4;
          bytes[i] = (byte)((fromChar(s.charAt(n)) << 2) | (fromChar(s.charAt(n + 1)) >> 4));
          break;
        }
        case 1: {
          int n = i / 3 * 4 + 1;
          bytes[i] = (byte)(((fromChar(s.charAt(n)) & 0xF) << 4) | (fromChar(s.charAt(n + 1)) >> 2));
          break;
        }
        case 2: {
          int n = i / 3 * 4 + 2;
          bytes[i] = (byte)(((fromChar(s.charAt(n)) & 0x3) << 6) | fromChar(s.charAt(n + 1)));
          break;
        }
      }
    }
    try {
      return new String(bytes, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

}
