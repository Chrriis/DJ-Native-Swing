/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 *
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.swtimpl.core;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Christopher Deckers
 */
public class SWTUtils {

  private SWTUtils() {}

  public static int translateSWTKeyCode(int key) {
    switch(key) {
      /* Keyboard and Mouse Masks */
      case SWT.ALT: return java.awt.event.KeyEvent.VK_ALT;
      case SWT.SHIFT: return java.awt.event.KeyEvent.VK_SHIFT;
      case SWT.CONTROL: return java.awt.event.KeyEvent.VK_CONTROL;
      case SWT.COMMAND: return java.awt.event.KeyEvent.VK_WINDOWS;

      /* NOT CURRENTLY USED */
//      case SWT.BUTTON1: return OS.VK_LBUTTON;
//      case SWT.BUTTON3: return OS.VK_MBUTTON;
//      case SWT.BUTTON2: return OS.VK_RBUTTON;

      /* Non-Numeric Keypad Keys */
      case SWT.ARROW_UP: return java.awt.event.KeyEvent.VK_UP;
      case SWT.ARROW_DOWN: return java.awt.event.KeyEvent.VK_DOWN;
      case SWT.ARROW_LEFT: return java.awt.event.KeyEvent.VK_LEFT;
      case SWT.ARROW_RIGHT: return java.awt.event.KeyEvent.VK_RIGHT;
      case SWT.PAGE_UP: return java.awt.event.KeyEvent.VK_PAGE_UP;
      case SWT.PAGE_DOWN: return java.awt.event.KeyEvent.VK_PAGE_DOWN;
      case SWT.HOME: return java.awt.event.KeyEvent.VK_HOME;
      case SWT.END: return java.awt.event.KeyEvent.VK_END;
      case SWT.INSERT: return java.awt.event.KeyEvent.VK_INSERT;

      /* Virtual and Ascii Keys */
      case SWT.BS: return java.awt.event.KeyEvent.VK_BACK_SPACE;
      case SWT.CR: return java.awt.event.KeyEvent.VK_ENTER;
      case SWT.DEL: return java.awt.event.KeyEvent.VK_DELETE;
      case SWT.ESC: return java.awt.event.KeyEvent.VK_ESCAPE;
      case SWT.LF: return java.awt.event.KeyEvent.VK_ENTER;
      case SWT.TAB: return java.awt.event.KeyEvent.VK_TAB;

      /* Functions Keys */
      case SWT.F1: return java.awt.event.KeyEvent.VK_F1;
      case SWT.F2: return java.awt.event.KeyEvent.VK_F2;
      case SWT.F3: return java.awt.event.KeyEvent.VK_F3;
      case SWT.F4: return java.awt.event.KeyEvent.VK_F4;
      case SWT.F5: return java.awt.event.KeyEvent.VK_F5;
      case SWT.F6: return java.awt.event.KeyEvent.VK_F6;
      case SWT.F7: return java.awt.event.KeyEvent.VK_F7;
      case SWT.F8: return java.awt.event.KeyEvent.VK_F8;
      case SWT.F9: return java.awt.event.KeyEvent.VK_F9;
      case SWT.F10: return java.awt.event.KeyEvent.VK_F10;
      case SWT.F11: return java.awt.event.KeyEvent.VK_F11;
      case SWT.F12: return java.awt.event.KeyEvent.VK_F12;
      case SWT.F13: return java.awt.event.KeyEvent.VK_F13;
      case SWT.F14: return java.awt.event.KeyEvent.VK_F14;
      case SWT.F15: return java.awt.event.KeyEvent.VK_F15;

      /* Numeric Keypad Keys */
      case SWT.KEYPAD_MULTIPLY: return java.awt.event.KeyEvent.VK_MULTIPLY;
      case SWT.KEYPAD_ADD: return java.awt.event.KeyEvent.VK_ADD;
      case SWT.KEYPAD_CR: return java.awt.event.KeyEvent.VK_ENTER;
      case SWT.KEYPAD_SUBTRACT: return java.awt.event.KeyEvent.VK_SUBTRACT;
      case SWT.KEYPAD_DECIMAL: return java.awt.event.KeyEvent.VK_DECIMAL;
      case SWT.KEYPAD_DIVIDE: return java.awt.event.KeyEvent.VK_DIVIDE;
      case SWT.KEYPAD_0: return java.awt.event.KeyEvent.VK_NUMPAD0;
      case SWT.KEYPAD_1: return java.awt.event.KeyEvent.VK_NUMPAD1;
      case SWT.KEYPAD_2: return java.awt.event.KeyEvent.VK_NUMPAD2;
      case SWT.KEYPAD_3: return java.awt.event.KeyEvent.VK_NUMPAD3;
      case SWT.KEYPAD_4: return java.awt.event.KeyEvent.VK_NUMPAD4;
      case SWT.KEYPAD_5: return java.awt.event.KeyEvent.VK_NUMPAD5;
      case SWT.KEYPAD_6: return java.awt.event.KeyEvent.VK_NUMPAD6;
      case SWT.KEYPAD_7: return java.awt.event.KeyEvent.VK_NUMPAD7;
      case SWT.KEYPAD_8: return java.awt.event.KeyEvent.VK_NUMPAD8;
      case SWT.KEYPAD_9: return java.awt.event.KeyEvent.VK_NUMPAD9;
//      case SWT.KEYPAD_EQUAL: return java.awt.event.KeyEvent.VK_????;

      /* Other keys */
      case SWT.CAPS_LOCK: return java.awt.event.KeyEvent.VK_CAPS_LOCK;
      case SWT.NUM_LOCK: return java.awt.event.KeyEvent.VK_NUM_LOCK;
      case SWT.SCROLL_LOCK: return java.awt.event.KeyEvent.VK_SCROLL_LOCK;
      case SWT.PAUSE: return java.awt.event.KeyEvent.VK_PAUSE;
      case SWT.BREAK: return java.awt.event.KeyEvent.VK_CANCEL;
      case SWT.PRINT_SCREEN: return java.awt.event.KeyEvent.VK_PRINTSCREEN;
      case SWT.HELP: return java.awt.event.KeyEvent.VK_HELP;
    }
    // TODO: return translateChar or something
    return 0;
  }

  public static int translateSWTMouseButton(int button) {
    switch(button) {
      case 1: return MouseEvent.BUTTON1;
      case 2: return MouseEvent.BUTTON2;
      case 3: return MouseEvent.BUTTON3;
    }
    return 0;
  }

  public static int translateSWTModifiers(int stateMask) {
    int modifiers = 0;
    if((stateMask & SWT.SHIFT) != 0) {
      modifiers |= KeyEvent.SHIFT_MASK;
    }
    if((stateMask & SWT.CONTROL) != 0) {
      modifiers |= KeyEvent.CTRL_MASK;
    }
    if((stateMask & SWT.ALT) != 0) {
      modifiers |= KeyEvent.ALT_MASK;
    }
    return modifiers;
  }

  public static ImageData convertAWTImage(Image image) {
    BufferedImage handle = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    Graphics g = handle.getGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    ColorModel colorModel = handle.getColorModel();
    PaletteData paletteData = new PaletteData(0xFF0000, 0xFF00, 0xFF);
    int width = handle.getWidth();
    ImageData imageData = new ImageData(width, handle.getHeight(), colorModel.getPixelSize(), paletteData);
    int height = handle.getHeight();
    byte[] maskData = new byte[(width + 7) / 8 * height];
    for(int x=width-1; x >= 0; x--) {
      for(int y=height-1; y >= 0; y--) {
        int rgb = handle.getRGB(x, y);
        int pixel = paletteData.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
        imageData.setPixel(x, y, pixel);
        int alpha = (rgb >> 24) & 0xFF;
        imageData.setAlpha(x, y, alpha);
        if(alpha != 0) {
          int index = x + y * ((width + 7) / 8) * 8;
          maskData[index / 8] |= (byte)(1 << (7 - (index % 8)));
        }
      }
    }
    imageData.maskPad = 1;
    imageData.maskData = maskData;
    return imageData;
  }

  public static BufferedImage convertSWTImage(ImageData data) {
    ColorModel colorModel = null;
    PaletteData palette = data.palette;
    if (palette.isDirect) {
      BufferedImage bufferedImage = new BufferedImage(data.width, data.height, BufferedImage.TYPE_INT_ARGB);
      ImageData transparencyMask = data.getTransparencyMask();
      for(int x=data.width-1; x >= 0; x--) {
        for(int y=data.height-1; y >= 0; y--) {
          RGB rgb = data.palette.getRGB(data.getPixel(x, y));
          int pixel = rgb.red << 16 | rgb.green << 8 | rgb.blue;
          rgb = transparencyMask.palette.getRGB(transparencyMask.getPixel(x, y));
          int mask = rgb.red << 16 | rgb.green << 8 | rgb.blue;
          if(mask != 0) {
            int alpha = data.getAlpha(x, y);
            if(alpha > 0) {
              pixel = pixel & 0x00FFFFFF | alpha << 24;
              bufferedImage.setRGB(x, y, pixel);
            }
          }
        }
      }
//      BufferedImage bufferedImage = new BufferedImage(data.width, data.height, BufferedImage.TYPE_INT_ARGB);
//      int transparencyType = data.getTransparencyType();
//      int transparentPixel = data.transparentPixel;
//      ImageData transparencyMask = data.getTransparencyMask();
//      for(int x=data.width-1; x >= 0; x--) {
//        for(int y=data.height-1; y >= 0; y--) {
//          int pixel = data.getPixel(x, y);
//          RGB rgb = data.palette.getRGB(pixel);
//          int iRGB = rgb.red << 16 | rgb.green << 8 | rgb.blue;
//          switch(transparencyType) {
//            case SWT.TRANSPARENCY_PIXEL: {
//              if(pixel != transparentPixel) {
//                bufferedImage.setRGB(x, y, iRGB);
//              }
//              break;
//            }
//            case SWT.TRANSPARENCY_ALPHA: {
//              int alpha = data.getAlpha(x, y);
//              if(alpha > 0) {
//                iRGB = iRGB & 0x00FFFFFF | alpha << 24;
//                bufferedImage.setRGB(x, y, iRGB);
//              }
//              break;
//            }
//            case SWT.TRANSPARENCY_MASK: {
//              rgb = transparencyMask.palette.getRGB(transparencyMask.getPixel(x, y));
//              int mask = rgb.red << 16 | rgb.green << 8 | rgb.blue;
//              if(mask != 0) {
//                int alpha = data.getAlpha(x, y);
//                if(alpha > 0) {
//                  iRGB = iRGB & 0x00FFFFFF | alpha << 24;
//                }
//                bufferedImage.setRGB(x, y, iRGB);
//              }
//              break;
//            }
//            default:
//              bufferedImage.setRGB(x, y, iRGB);
//              break;
//          }
//        }
//      }

//      colorModel = new DirectColorModel(data.depth, palette.redMask, palette.greenMask, palette.blueMask);
//      BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
//      WritableRaster raster = bufferedImage.getRaster();
//      int[] pixelArray = new int[3];
//      for (int y = 0; y < data.height; y++) {
//        for (int x = 0; x < data.width; x++) {
//          int pixel = data.getPixel(x, y);
//          RGB rgb = palette.getRGB(pixel);
//          pixelArray[0] = rgb.red;
//          pixelArray[1] = rgb.green;
//          pixelArray[2] = rgb.blue;
//          raster.setPixels(x, y, 1, 1, pixelArray);
//        }
//      }
      return bufferedImage;
    }
    RGB[] rgbs = palette.getRGBs();
    byte[] red = new byte[rgbs.length];
    byte[] green = new byte[rgbs.length];
    byte[] blue = new byte[rgbs.length];
    for (int i = 0; i < rgbs.length; i++) {
      RGB rgb = rgbs[i];
      red[i] = (byte)rgb.red;
      green[i] = (byte)rgb.green;
      blue[i] = (byte)rgb.blue;
    }
    if (data.transparentPixel != -1) {
      colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue, data.transparentPixel);
    } else {
      colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue);
    }
    BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
    WritableRaster raster = bufferedImage.getRaster();
    int[] pixelArray = new int[1];
    for (int y = 0; y < data.height; y++) {
      for (int x = 0; x < data.width; x++) {
        int pixel = data.getPixel(x, y);
        pixelArray[0] = pixel;
        raster.setPixel(x, y, pixelArray);
      }
    }
    return bufferedImage;
  }

}
