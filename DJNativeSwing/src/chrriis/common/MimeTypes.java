/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A utility class to get mime type mappings.
 * @author Christopher Deckers
 */
public class MimeTypes {

  private MimeTypes() {}
  
  private static Map<String, String> extensionToMimeTypeMap = new HashMap<String, String>();
  
  static {
    synchronized(extensionToMimeTypeMap) {
      extensionToMimeTypeMap.put("323", "text/h323");
      extensionToMimeTypeMap.put("acx", "application/internet-property-stream");
      extensionToMimeTypeMap.put("ai", "application/postscript");
      extensionToMimeTypeMap.put("aif", "audio/x-aiff");
      extensionToMimeTypeMap.put("aifc", "audio/x-aiff");
      extensionToMimeTypeMap.put("aiff", "audio/x-aiff");
      extensionToMimeTypeMap.put("asf", "video/x-ms-asf");
      extensionToMimeTypeMap.put("asr", "video/x-ms-asf");
      extensionToMimeTypeMap.put("asx", "video/x-ms-asf");
      extensionToMimeTypeMap.put("au", "audio/basic");
      extensionToMimeTypeMap.put("avi", "video/x-msvideo");
      extensionToMimeTypeMap.put("axs", "application/olescript");
      extensionToMimeTypeMap.put("bas", "text/plain");
      extensionToMimeTypeMap.put("bcpio", "application/x-bcpio");
      extensionToMimeTypeMap.put("bin", "application/octet-stream");
      extensionToMimeTypeMap.put("bmp", "image/bmp");
      extensionToMimeTypeMap.put("c", "text/plain");
      extensionToMimeTypeMap.put("cat", "application/vnd.ms-pkiseccat");
      extensionToMimeTypeMap.put("cdf", "application/x-cdf");
      extensionToMimeTypeMap.put("cer", "application/x-x509-ca-cert");
      extensionToMimeTypeMap.put("class", "application/octet-stream");
      extensionToMimeTypeMap.put("clp", "application/x-msclip");
      extensionToMimeTypeMap.put("cmx", "image/x-cmx");
      extensionToMimeTypeMap.put("cod", "image/cis-cod");
      extensionToMimeTypeMap.put("cpio", "application/x-cpio");
      extensionToMimeTypeMap.put("crd", "application/x-mscardfile");
      extensionToMimeTypeMap.put("crl", "application/pkix-crl");
      extensionToMimeTypeMap.put("crt", "application/x-x509-ca-cert");
      extensionToMimeTypeMap.put("csh", "application/x-csh");
      extensionToMimeTypeMap.put("css", "text/css");
      extensionToMimeTypeMap.put("dcr", "application/x-director");
      extensionToMimeTypeMap.put("der", "application/x-x509-ca-cert");
      extensionToMimeTypeMap.put("dir", "application/x-director");
      extensionToMimeTypeMap.put("dll", "application/x-msdownload");
      extensionToMimeTypeMap.put("dms", "application/octet-stream");
      extensionToMimeTypeMap.put("doc", "application/msword");
      extensionToMimeTypeMap.put("docm", "application/vnd.ms-word.document.macroEnabled.12");
      extensionToMimeTypeMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
      extensionToMimeTypeMap.put("dot", "application/msword");
      extensionToMimeTypeMap.put("dotm", "application/vnd.ms-word.template.macroEnabled.12");
      extensionToMimeTypeMap.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
      extensionToMimeTypeMap.put("dvi", "application/x-dvi");
      extensionToMimeTypeMap.put("dxr", "application/x-director");
      extensionToMimeTypeMap.put("eml", "message/rfc822");
      extensionToMimeTypeMap.put("eps", "application/postscript");
      extensionToMimeTypeMap.put("etx", "text/x-setext");
      extensionToMimeTypeMap.put("evy", "application/envoy");
      extensionToMimeTypeMap.put("exe", "application/octet-stream");
      extensionToMimeTypeMap.put("fif", "application/fractals");
      extensionToMimeTypeMap.put("flr", "x-world/x-vrml");
      extensionToMimeTypeMap.put("gif", "image/gif");
      extensionToMimeTypeMap.put("gtar", "application/x-gtar");
      extensionToMimeTypeMap.put("gz", "application/x-gzip");
      extensionToMimeTypeMap.put("h", "text/plain");
      extensionToMimeTypeMap.put("hdf", "application/x-hdf");
      extensionToMimeTypeMap.put("hlp", "application/winhlp");
      extensionToMimeTypeMap.put("hqx", "application/mac-binhex40");
      extensionToMimeTypeMap.put("hta", "application/hta");
      extensionToMimeTypeMap.put("htc", "text/x-component");
      extensionToMimeTypeMap.put("htm", "text/html");
      extensionToMimeTypeMap.put("html", "text/html");
      extensionToMimeTypeMap.put("htt", "text/webviewhtml");
      extensionToMimeTypeMap.put("ico", "image/x-icon");
      extensionToMimeTypeMap.put("ief", "image/ief");
      extensionToMimeTypeMap.put("iii", "application/x-iphone");
      extensionToMimeTypeMap.put("ins", "application/x-internet-signup");
      extensionToMimeTypeMap.put("isp", "application/x-internet-signup");
      extensionToMimeTypeMap.put("jfif", "image/pipeg");
      extensionToMimeTypeMap.put("jnlp", "application/x-java-jnlp-file");
      extensionToMimeTypeMap.put("jpe", "image/jpeg");
      extensionToMimeTypeMap.put("jpeg", "image/jpeg");
      extensionToMimeTypeMap.put("jpg", "image/jpeg");
      extensionToMimeTypeMap.put("js", "application/x-javascript");
      extensionToMimeTypeMap.put("latex", "application/x-latex");
      extensionToMimeTypeMap.put("lha", "application/octet-stream");
      extensionToMimeTypeMap.put("lsf", "video/x-la-asf");
      extensionToMimeTypeMap.put("lsx", "video/x-la-asf");
      extensionToMimeTypeMap.put("lzh", "application/octet-stream");
      extensionToMimeTypeMap.put("m13", "application/x-msmediaview");
      extensionToMimeTypeMap.put("m14", "application/x-msmediaview");
      extensionToMimeTypeMap.put("m3u", "audio/x-mpegurl");
      extensionToMimeTypeMap.put("man", "application/x-troff-man");
      extensionToMimeTypeMap.put("mdb", "application/x-msaccess");
      extensionToMimeTypeMap.put("me", "application/x-troff-me");
      extensionToMimeTypeMap.put("mht", "message/rfc822");
      extensionToMimeTypeMap.put("mhtml", "message/rfc822");
      extensionToMimeTypeMap.put("mid", "audio/mid");
      extensionToMimeTypeMap.put("mny", "application/x-msmoney");
      extensionToMimeTypeMap.put("mov", "video/quicktime");
      extensionToMimeTypeMap.put("movie", "video/x-sgi-movie");
      extensionToMimeTypeMap.put("mp2", "video/mpeg");
      extensionToMimeTypeMap.put("mp3", "audio/mpeg");
      extensionToMimeTypeMap.put("mp4", "video/mp4");
      extensionToMimeTypeMap.put("mpa", "video/mpeg");
      extensionToMimeTypeMap.put("mpe", "video/mpeg");
      extensionToMimeTypeMap.put("mpeg", "video/mpeg");
      extensionToMimeTypeMap.put("mpg", "video/mpeg");
      extensionToMimeTypeMap.put("mpp", "application/vnd.ms-project");
      extensionToMimeTypeMap.put("mpv2", "video/mpeg");
      extensionToMimeTypeMap.put("ms", "application/x-troff-ms");
      extensionToMimeTypeMap.put("msg", "application/vnd.ms-outlook");
      extensionToMimeTypeMap.put("mvb", "application/x-msmediaview");
      extensionToMimeTypeMap.put("nws", "message/rfc822");
      extensionToMimeTypeMap.put("oda", "application/oda");
      extensionToMimeTypeMap.put("odp", "application/vnd.oasis.opendocument.presentation");
      extensionToMimeTypeMap.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
      extensionToMimeTypeMap.put("odt", "application/vnd.oasis.opendocument.text");
      extensionToMimeTypeMap.put("oga", "audio/ogg");
      extensionToMimeTypeMap.put("ogg", "audio/ogg");
      extensionToMimeTypeMap.put("ogv", "video/ogg");
      extensionToMimeTypeMap.put("p10", "application/pkcs10");
      extensionToMimeTypeMap.put("p12", "application/x-pkcs12");
      extensionToMimeTypeMap.put("p7b", "application/x-pkcs7-certificates");
      extensionToMimeTypeMap.put("p7c", "application/x-pkcs7-mime");
      extensionToMimeTypeMap.put("p7m", "application/x-pkcs7-mime");
      extensionToMimeTypeMap.put("p7r", "application/x-pkcs7-certreqresp");
      extensionToMimeTypeMap.put("p7s", "application/x-pkcs7-signature");
      extensionToMimeTypeMap.put("pbm", "image/x-portable-bitmap");
      extensionToMimeTypeMap.put("pdf", "application/pdf");
      extensionToMimeTypeMap.put("pfx", "application/x-pkcs12");
      extensionToMimeTypeMap.put("pgm", "image/x-portable-graymap");
      extensionToMimeTypeMap.put("pko", "application/ynd.ms-pkipko");
      extensionToMimeTypeMap.put("pma", "application/x-perfmon");
      extensionToMimeTypeMap.put("pmc", "application/x-perfmon");
      extensionToMimeTypeMap.put("pml", "application/x-perfmon");
      extensionToMimeTypeMap.put("pmr", "application/x-perfmon");
      extensionToMimeTypeMap.put("pmw", "application/x-perfmon");
      extensionToMimeTypeMap.put("png", "image/png");
      extensionToMimeTypeMap.put("pnm", "image/x-portable-anymap");
      extensionToMimeTypeMap.put("pot,", "application/vnd.ms-powerpoint");
      extensionToMimeTypeMap.put("potm,", "application/vnd.ms-powerpoint.template.macroEnabled.12");
      extensionToMimeTypeMap.put("potx,", "application/vnd.openxmlformats-officedocument.presentationml.template");
      extensionToMimeTypeMap.put("ppa", "application/vnd.ms-powerpoint");
      extensionToMimeTypeMap.put("ppam", "application/vnd.ms-powerpoint.addin.macroEnabled.12");
      extensionToMimeTypeMap.put("ppm", "image/x-portable-pixmap");
      extensionToMimeTypeMap.put("pps", "application/vnd.ms-powerpoint");
      extensionToMimeTypeMap.put("ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12");
      extensionToMimeTypeMap.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
      extensionToMimeTypeMap.put("ppt", "application/vnd.ms-powerpoint");
      extensionToMimeTypeMap.put("pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12");
      extensionToMimeTypeMap.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
      extensionToMimeTypeMap.put("prf", "application/pics-rules");
      extensionToMimeTypeMap.put("ps", "application/postscript");
      extensionToMimeTypeMap.put("pub", "application/x-mspublisher");
      extensionToMimeTypeMap.put("qt", "video/quicktime");
      extensionToMimeTypeMap.put("ra", "audio/x-pn-realaudio");
      extensionToMimeTypeMap.put("ram", "audio/x-pn-realaudio");
      extensionToMimeTypeMap.put("ras", "image/x-cmu-raster");
      extensionToMimeTypeMap.put("rgb", "image/x-rgb");
      extensionToMimeTypeMap.put("rmi", "audio/mid");
      extensionToMimeTypeMap.put("roff", "application/x-troff");
      extensionToMimeTypeMap.put("rtf", "application/rtf");
      extensionToMimeTypeMap.put("rtx", "text/richtext");
      extensionToMimeTypeMap.put("scd", "application/x-msschedule");
      extensionToMimeTypeMap.put("sct", "text/scriptlet");
      extensionToMimeTypeMap.put("setpay", "application/set-payment-initiation");
      extensionToMimeTypeMap.put("setreg", "application/set-registration-initiation");
      extensionToMimeTypeMap.put("sh", "application/x-sh");
      extensionToMimeTypeMap.put("shar", "application/x-shar");
      extensionToMimeTypeMap.put("sit", "application/x-stuffit");
      extensionToMimeTypeMap.put("snd", "audio/basic");
      extensionToMimeTypeMap.put("spc", "application/x-pkcs7-certificates");
      extensionToMimeTypeMap.put("spl", "application/futuresplash");
      extensionToMimeTypeMap.put("src", "application/x-wais-source");
      extensionToMimeTypeMap.put("sst", "application/vnd.ms-pkicertstore");
      extensionToMimeTypeMap.put("stl", "application/vnd.ms-pkistl");
      extensionToMimeTypeMap.put("stm", "text/html");
      extensionToMimeTypeMap.put("svg", "image/svg+xml");
      extensionToMimeTypeMap.put("sv4cpio", "application/x-sv4cpio");
      extensionToMimeTypeMap.put("sv4crc", "application/x-sv4crc");
      extensionToMimeTypeMap.put("swf", "application/x-shockwave-flash");
      extensionToMimeTypeMap.put("t", "application/x-troff");
      extensionToMimeTypeMap.put("tar", "application/x-tar");
      extensionToMimeTypeMap.put("tcl", "application/x-tcl");
      extensionToMimeTypeMap.put("tex", "application/x-tex");
      extensionToMimeTypeMap.put("texi", "application/x-texinfo");
      extensionToMimeTypeMap.put("texinfo", "application/x-texinfo");
      extensionToMimeTypeMap.put("tgz", "application/x-compressed");
      extensionToMimeTypeMap.put("tif", "image/tiff");
      extensionToMimeTypeMap.put("tiff", "image/tiff");
      extensionToMimeTypeMap.put("tr", "application/x-troff");
      extensionToMimeTypeMap.put("trm", "application/x-msterminal");
      extensionToMimeTypeMap.put("tsv", "text/tab-separated-values");
      extensionToMimeTypeMap.put("txt", "text/plain");
      extensionToMimeTypeMap.put("uls", "text/iuls");
      extensionToMimeTypeMap.put("ustar", "application/x-ustar");
      extensionToMimeTypeMap.put("vcf", "text/x-vcard");
      extensionToMimeTypeMap.put("vrml", "x-world/x-vrml");
      extensionToMimeTypeMap.put("wav", "audio/x-wav");
      extensionToMimeTypeMap.put("wax", "audio/x-ms-wax");
      extensionToMimeTypeMap.put("wcm", "application/vnd.ms-works");
      extensionToMimeTypeMap.put("wdb", "application/vnd.ms-works");
      extensionToMimeTypeMap.put("webm", "video/webm");
      extensionToMimeTypeMap.put("wks", "application/vnd.ms-works");
      extensionToMimeTypeMap.put("wm", "video/x-ms-wm");
      extensionToMimeTypeMap.put("wma", "audio/x-ms-wma");
      extensionToMimeTypeMap.put("wmd", "application/x-ms-wmd");
      extensionToMimeTypeMap.put("wmf", "application/x-msmetafile");
      extensionToMimeTypeMap.put("wmv", "audio/x-ms-wmv");
      extensionToMimeTypeMap.put("wmx", "video/x-ms-wmx");
      extensionToMimeTypeMap.put("wmz", "application/x-ms-wmz");
      extensionToMimeTypeMap.put("wps", "application/vnd.ms-works");
      extensionToMimeTypeMap.put("wri", "application/x-mswrite");
      extensionToMimeTypeMap.put("wrl", "x-world/x-vrml");
      extensionToMimeTypeMap.put("wrz", "x-world/x-vrml");
      extensionToMimeTypeMap.put("wvx", "video/x-ms-wvx");
      extensionToMimeTypeMap.put("xaf", "x-world/x-vrml");
      extensionToMimeTypeMap.put("xbm", "image/x-xbitmap");
      extensionToMimeTypeMap.put("xla", "application/vnd.ms-excel");
      extensionToMimeTypeMap.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
      extensionToMimeTypeMap.put("xlc", "application/vnd.ms-excel");
      extensionToMimeTypeMap.put("xlm", "application/vnd.ms-excel");
      extensionToMimeTypeMap.put("xls", "application/vnd.ms-excel");
      extensionToMimeTypeMap.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
      extensionToMimeTypeMap.put("xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12");
      extensionToMimeTypeMap.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      extensionToMimeTypeMap.put("xlt", "application/vnd.ms-excel");
      extensionToMimeTypeMap.put("xltm", "application/vnd.ms-excel.template.macroEnabled.12");
      extensionToMimeTypeMap.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
      extensionToMimeTypeMap.put("xlw", "application/vnd.ms-excel");
      extensionToMimeTypeMap.put("xml", "application/xml");
      extensionToMimeTypeMap.put("xof", "x-world/x-vrml");
      extensionToMimeTypeMap.put("xpm", "image/x-xpixmap");
      extensionToMimeTypeMap.put("xwd", "image/x-xwindowdump");
      extensionToMimeTypeMap.put("z", "application/x-compress");
      extensionToMimeTypeMap.put("zip", "application/zip");
    }
  }

  /**
   * Get the mime type associated to a particular extension.
   * @return null if no mapping exists.
   */
  public static String getMimeType(String extension) {
    if(extension == null) {
      return null;
    }
    if(extension.startsWith(".")) {
      extension = extension.substring(1);
    }
    extension = extension.toLowerCase(Locale.ENGLISH);
    synchronized(extensionToMimeTypeMap) {
      return extensionToMimeTypeMap.get(extension);
    }
  }
  
  /**
   * Add (or replace) a mime type for a given extension.
   * This method is useful when a new mime type needs to be supported and the library does not yet contain it.
   */
  public static void addMissingMimeType(String extension, String mimeType) {
    if(extension == null) {
      return;
    }
    if(extension.startsWith(".")) {
      extension = extension.substring(1);
    }
    extension = extension.toLowerCase(Locale.ENGLISH);
    synchronized(extensionToMimeTypeMap) {
      extensionToMimeTypeMap.put(extension, mimeType);
    }
  }

}
