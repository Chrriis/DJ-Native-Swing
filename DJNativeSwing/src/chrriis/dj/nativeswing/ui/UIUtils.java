/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.dj.nativeswing.ui;

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
public class UIUtils {

  protected static final int[][] KeyTable = {
    
    /* Keyboard and Mouse Masks */
    {java.awt.event.KeyEvent.VK_ALT,  SWT.ALT},
    {java.awt.event.KeyEvent.VK_SHIFT,  SWT.SHIFT},
    {java.awt.event.KeyEvent.VK_CONTROL,  SWT.CONTROL},
    {java.awt.event.KeyEvent.VK_WINDOWS,  SWT.COMMAND},

    /* NOT CURRENTLY USED */    
//    {OS.VK_LBUTTON, SWT.BUTTON1},
//    {OS.VK_MBUTTON, SWT.BUTTON3},
//    {OS.VK_RBUTTON, SWT.BUTTON2},
    
    /* Non-Numeric Keypad Keys */
    {java.awt.event.KeyEvent.VK_UP,   SWT.ARROW_UP},
    {java.awt.event.KeyEvent.VK_DOWN, SWT.ARROW_DOWN},
    {java.awt.event.KeyEvent.VK_LEFT, SWT.ARROW_LEFT},
    {java.awt.event.KeyEvent.VK_RIGHT,  SWT.ARROW_RIGHT},
    {java.awt.event.KeyEvent.VK_PAGE_UP,  SWT.PAGE_UP},
    {java.awt.event.KeyEvent.VK_PAGE_DOWN,  SWT.PAGE_DOWN},
    {java.awt.event.KeyEvent.VK_HOME, SWT.HOME},
    {java.awt.event.KeyEvent.VK_END,    SWT.END},
    {java.awt.event.KeyEvent.VK_INSERT, SWT.INSERT},

    /* Virtual and Ascii Keys */
    {java.awt.event.KeyEvent.VK_BACK_SPACE, SWT.BS},
    {java.awt.event.KeyEvent.VK_ENTER,  SWT.CR},
    {java.awt.event.KeyEvent.VK_DELETE, SWT.DEL},
    {java.awt.event.KeyEvent.VK_ESCAPE, SWT.ESC},
    {java.awt.event.KeyEvent.VK_ENTER,  SWT.LF},
    {java.awt.event.KeyEvent.VK_TAB,    SWT.TAB},
  
    /* Functions Keys */
    {java.awt.event.KeyEvent.VK_F1, SWT.F1},
    {java.awt.event.KeyEvent.VK_F2, SWT.F2},
    {java.awt.event.KeyEvent.VK_F3, SWT.F3},
    {java.awt.event.KeyEvent.VK_F4, SWT.F4},
    {java.awt.event.KeyEvent.VK_F5, SWT.F5},
    {java.awt.event.KeyEvent.VK_F6, SWT.F6},
    {java.awt.event.KeyEvent.VK_F7, SWT.F7},
    {java.awt.event.KeyEvent.VK_F8, SWT.F8},
    {java.awt.event.KeyEvent.VK_F9, SWT.F9},
    {java.awt.event.KeyEvent.VK_F10,  SWT.F10},
    {java.awt.event.KeyEvent.VK_F11,  SWT.F11},
    {java.awt.event.KeyEvent.VK_F12,  SWT.F12},
    {java.awt.event.KeyEvent.VK_F13,  SWT.F13},
    {java.awt.event.KeyEvent.VK_F14,  SWT.F14},
    {java.awt.event.KeyEvent.VK_F15,  SWT.F15},
    
    /* Numeric Keypad Keys */
    {java.awt.event.KeyEvent.VK_MULTIPLY, SWT.KEYPAD_MULTIPLY},
    {java.awt.event.KeyEvent.VK_ADD,      SWT.KEYPAD_ADD},
    {java.awt.event.KeyEvent.VK_ENTER,    SWT.KEYPAD_CR},
    {java.awt.event.KeyEvent.VK_SUBTRACT, SWT.KEYPAD_SUBTRACT},
    {java.awt.event.KeyEvent.VK_DECIMAL,    SWT.KEYPAD_DECIMAL},
    {java.awt.event.KeyEvent.VK_DIVIDE,   SWT.KEYPAD_DIVIDE},
    {java.awt.event.KeyEvent.VK_NUMPAD0,    SWT.KEYPAD_0},
    {java.awt.event.KeyEvent.VK_NUMPAD1,    SWT.KEYPAD_1},
    {java.awt.event.KeyEvent.VK_NUMPAD2,    SWT.KEYPAD_2},
    {java.awt.event.KeyEvent.VK_NUMPAD3,    SWT.KEYPAD_3},
    {java.awt.event.KeyEvent.VK_NUMPAD4,    SWT.KEYPAD_4},
    {java.awt.event.KeyEvent.VK_NUMPAD5,    SWT.KEYPAD_5},
    {java.awt.event.KeyEvent.VK_NUMPAD6,    SWT.KEYPAD_6},
    {java.awt.event.KeyEvent.VK_NUMPAD7,    SWT.KEYPAD_7},
    {java.awt.event.KeyEvent.VK_NUMPAD8,    SWT.KEYPAD_8},
    {java.awt.event.KeyEvent.VK_NUMPAD9,    SWT.KEYPAD_9},
//    {java.awt.event.KeyEvent.VK_????,   SWT.KEYPAD_EQUAL},

    /* Other keys */
    {java.awt.event.KeyEvent.VK_CAPS_LOCK,    SWT.CAPS_LOCK},
    {java.awt.event.KeyEvent.VK_NUM_LOCK,   SWT.NUM_LOCK},
    {java.awt.event.KeyEvent.VK_SCROLL_LOCK,    SWT.SCROLL_LOCK},
    {java.awt.event.KeyEvent.VK_PAUSE,    SWT.PAUSE},
    {java.awt.event.KeyEvent.VK_CANCEL,   SWT.BREAK},
    {java.awt.event.KeyEvent.VK_PRINTSCREEN,  SWT.PRINT_SCREEN},
    {java.awt.event.KeyEvent.VK_HELP,   SWT.HELP},
    
  };

  public static int translateMouseButton(int button) {
    switch(button) {
      case 1: return MouseEvent.BUTTON1;
      case 2: return MouseEvent.BUTTON2;
      case 3: return MouseEvent.BUTTON3;
    }
    return 0;
  }
  
  public static int translateKeyCode(int key) {
    for (int i=0; i<KeyTable.length; i++) {
      if (KeyTable[i][0] == key) return KeyTable [i] [1];
    }
    // TODO: return translateChar or something
    return 0;
  }
  
  public static int translateModifiers(int stateMask) {
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

  public static ImageData convertImage(Image image) {
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
  
  public static BufferedImage convertImage(ImageData data) {
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
