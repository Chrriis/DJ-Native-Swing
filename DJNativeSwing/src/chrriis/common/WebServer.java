/*
 * Christopher Deckers (chrriis@nextencia.net)
 * http://www.nextencia.net
 * 
 * See the file "readme.txt" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package chrriis.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @author Christopher Deckers
 */
public class WebServer {

  public static class HTTPRequest implements Cloneable {
    HTTPRequest(String urlPath) {
      setURLPath(urlPath);
    }
    private String endQuery = "";
    private String urlPath;
    void setURLPath(String urlPath) {
      this.urlPath = urlPath;
      resourcePath = urlPath;
      int index = resourcePath.indexOf('?');
      if(index != -1) {
        String queryString = resourcePath.substring(index + 1);
        endQuery = '?' + queryString;
        resourcePath = resourcePath.substring(0, index);
        for(String content: queryString.split("&")) {
          int eqIndex = content.indexOf('=');
          if(eqIndex > 0) {
            String key = content.substring(0, eqIndex);
            String value = Utils.decodeURL(content.substring(eqIndex + 1));
            queryParameterMap.put(key, value);
          } else {
            queryParameterMap.put(content, "");
          }
        }
      }
      index = resourcePath.indexOf('#');
      if(index != -1) {
        anchor = resourcePath.substring(index + 1);
        endQuery = '#' + anchor + endQuery;
        resourcePath = resourcePath.substring(0, index);
      }
    }
    public String getURLPath() {
      return urlPath;
    }
    private String resourcePath;
    void setResourcePath(String resourcePath) {
      this.resourcePath = resourcePath;
      this.urlPath = resourcePath + endQuery;
    }
    public String getResourcePath() {
      return resourcePath;
    }
    private String anchor;
    public String getAnchor() {
      return anchor;
    }
    private Map<String, String> queryParameterMap = new HashMap<String, String>();
    public Map<String, String> getQueryParameterMap() {
      return queryParameterMap;
    }
    private boolean isPostMethod;
    void setPostMethod(boolean isPostMethod) {
      this.isPostMethod = isPostMethod;
    }
    public boolean isPostMethod() {
      return isPostMethod;
    }
    private HTTPData[] httpPostDataArray;
    void setHTTPPostDataArray(HTTPData[] httpPostDataArray) {
      this.httpPostDataArray = httpPostDataArray;
    }
    public HTTPData[] getHTTPPostDataArray() {
      return httpPostDataArray;
    }
    @Override
    protected HTTPRequest clone() {
      try {
        HTTPRequest httpRequest = (HTTPRequest)super.clone();
        httpRequest.queryParameterMap = new HashMap<String, String>(queryParameterMap);
        return httpRequest;
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  public static class HTTPData {
    private Map<String, String> headerMap = new HashMap<String, String>();
    HTTPData() {
    }
    public Map<String, String> getHeaderMap() {
      return headerMap;
    }
    private byte[] bytes;
    public byte[] getBytes() {
      return bytes;
    }
    void setBytes(byte[] bytes) {
      this.bytes = bytes;
    }
  }
  
  public static abstract class WebServerContent {
    
    public static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";
    
    private static Map<String, String> extensionToMimeTypeMap = new HashMap<String, String>();
    static {
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
      extensionToMimeTypeMap.put("dot", "application/msword");
      extensionToMimeTypeMap.put("dvi", "application/x-dvi");
      extensionToMimeTypeMap.put("dxr", "application/x-director");
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
      extensionToMimeTypeMap.put("mpa", "video/mpeg");
      extensionToMimeTypeMap.put("mpe", "video/mpeg");
      extensionToMimeTypeMap.put("mpeg", "video/mpeg");
      extensionToMimeTypeMap.put("mpg", "video/mpeg");
      extensionToMimeTypeMap.put("mpp", "application/vnd.ms-project");
      extensionToMimeTypeMap.put("mpv2", "video/mpeg");
      extensionToMimeTypeMap.put("ms", "application/x-troff-ms");
      extensionToMimeTypeMap.put("mvb", "application/x-msmediaview");
      extensionToMimeTypeMap.put("nws", "message/rfc822");
      extensionToMimeTypeMap.put("oda", "application/oda");
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
      extensionToMimeTypeMap.put("pnm", "image/x-portable-anymap");
      extensionToMimeTypeMap.put("pot,", "application/vnd.ms-powerpoint");
      extensionToMimeTypeMap.put("ppm", "image/x-portable-pixmap");
      extensionToMimeTypeMap.put("pps", "application/vnd.ms-powerpoint");
      extensionToMimeTypeMap.put("ppt", "application/vnd.ms-powerpoint");
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
      extensionToMimeTypeMap.put("xlc", "application/vnd.ms-excel");
      extensionToMimeTypeMap.put("xlm", "application/vnd.ms-excel");
      extensionToMimeTypeMap.put("xls", "application/vnd.ms-excel");
      extensionToMimeTypeMap.put("xlt", "application/vnd.ms-excel");
      extensionToMimeTypeMap.put("xlw", "application/vnd.ms-excel");
      extensionToMimeTypeMap.put("xml", "application/xml");
      extensionToMimeTypeMap.put("xof", "x-world/x-vrml");
      extensionToMimeTypeMap.put("xpm", "image/x-xpixmap");
      extensionToMimeTypeMap.put("xwd", "image/x-xwindowdump");
      extensionToMimeTypeMap.put("z", "application/x-compress");
      extensionToMimeTypeMap.put("zip", "application/zip");
    }
    
    public static String getDefaultMimeType(String extension) {
      if(extension == null) {
        return MIME_APPLICATION_OCTET_STREAM;
      }
      if(extension.startsWith(".")) {
        extension = extension.substring(1);
      }
      String mimeType = extensionToMimeTypeMap.get(extension.toLowerCase(Locale.ENGLISH));
      return mimeType != null? mimeType: MIME_APPLICATION_OCTET_STREAM;
    }
    
    public abstract InputStream getInputStream();
    public static InputStream getInputStream(String content) {
      if(content == null) {
        return null;
      }
      try {
        return new ByteArrayInputStream(content.getBytes("UTF-8"));
      } catch(Exception e) {
        e.printStackTrace();
        return null;
      }
    }
    public String getContentType() {
      return getDefaultMimeType(".html");
    }
    public long getContentLength() {
      return -1;
    }
    public long getLastModified() {
      return System.currentTimeMillis();
    }
    
  }
  
  private static class WebServerConnectionThread extends Thread {
    
    private static int threadInitNumber;
    private static Semaphore semaphore = new Semaphore(10);
    
    private static synchronized int nextThreadNumber() {
      return threadInitNumber++;
    }

    private Socket socket;
    
    public WebServerConnectionThread(Socket socket) {
      super("WebServer Connection-" + nextThreadNumber());
      this.socket = socket;
      setDaemon(true);
    }
    
    private static final String LS = Utils.LINE_SEPARATOR;
    
    static void writeHTTPHeaders(BufferedOutputStream out, int code, String contentType, long contentLength, long lastModified) {
      StringBuilder sb = new StringBuilder();
      sb.append("HTTP/1.0 " + code + " OK" + LS);
      sb.append("Content-Type: " + contentType + LS);
      sb.append("Server: WebServer/1.0" + LS);
      sb.append("Date: " + new Date() + LS);
//      sb.append("Expires: " + new Date() + LS);
//      sb.append("Last-modified: " + new Date(lastModified) + LS);
      if(contentLength != -1) {
        sb.append("Content-Length: " + contentLength + LS);
      }
      sb.append(LS);
      try {
        out.write(sb.toString().getBytes("UTF-8"));
      } catch(IOException e) {
        e.printStackTrace();
      }
    }

    static void writeHTTPError(BufferedOutputStream out, int code, String message) {
      writeHTTPHeaders(out, code, "text/html", message.length(), System.currentTimeMillis());
      try {
        out.write(message.getBytes("UTF-8"));
        out.flush();
        out.close();
      } catch(IOException e) {
//        e.printStackTrace();
      }
    }
    
    private static class HTTPInputStream extends InputStream {
      static enum LineSeparator {
        CR,
        LF,
        CRLF,
      }
      private InputStream inputStream;
      public HTTPInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
      }
      public String getLineSeparator() {
        switch(lineSeparator) {
          case CR: return "\r";
          case LF: return "\n";
          case CRLF: return "\r\n";
        }
        return null;
      }
      private LineSeparator lineSeparator;
      private int lastByte = -1;
      public String readAsciiLine() throws IOException {
        if(lineSeparator == null) {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          while(true) {
            int b = read();
            if(b == -1) {
              return null;
            }
            if(b == '\n') {
              lineSeparator = LineSeparator.LF;
              return new String(baos.toByteArray(), "UTF-8");
            }
            if(b == '\r') {
              int b2 = read();
              if(b2 == '\n') {
                lineSeparator = LineSeparator.CRLF;
              } else {
                lineSeparator = LineSeparator.CR;
                if(b2 != -1) {
                  lastByte = b2;
                }
              }
              return new String(baos.toByteArray(), "UTF-8");
            }
            baos.write(b);
          }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if(lastByte != -1) {
          baos.write(lastByte);
          lastByte = -1;
        }
        switch(lineSeparator) {
          case CR:
            for(int b; (b=read()) != '\r' && b != -1; baos.write(b));
            break;
          case LF:
            for(int b; (b=read()) != '\n' && b != -1; baos.write(b));
            break;
          case CRLF:
            for(int b; (b=read()) != '\r' && b != -1; baos.write(b));
            read();
            break;
        }
        return new String(baos.toByteArray(), "UTF-8");
      }
      public void close() throws IOException {
        inputStream.close();
      }
      @Override
      public int read(byte[] b) throws IOException {
        return inputStream.read(b);
      }
      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
      }
      @Override
      public int read() throws IOException {
        int n = inputStream.read();
        return n;
      }
    }
    
    private static final boolean DEBUG_PRINT_REQUESTS = Boolean.parseBoolean(System.getProperty("nativeswing.webserver.debug.printrequests"));
    
    @Override
    public void run() {
      try {
        HTTPInputStream in = new HTTPInputStream(new BufferedInputStream(socket.getInputStream()));
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        try {
          String request = in.readAsciiLine();
          if(request == null || !(request.endsWith(" HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
            writeHTTPError(out, 500, "Invalid Method.");
            return;
          }
          boolean isPostMethod = false;
          if(request.startsWith("POST ")) {
            isPostMethod = true;
          } else if(!request.startsWith("GET ")) {
            writeHTTPError(out, 500, "Invalid Method.");
            return;
          }
          String resourcePath = request.substring((isPostMethod? "POST ": "GET ").length(), request.length() - " HTTP/1.0".length());
          HTTPRequest httpRequest = new HTTPRequest(resourcePath);
          httpRequest.setPostMethod(isPostMethod);
          if(isPostMethod) {
            HTTPData[] httpDataArray;
            String contentType = null;
            int contentLength = -1;
            for(String header; (header = in.readAsciiLine()).length() > 0; ) {
              if(header.startsWith("Content-Length: ")) {
                contentLength = Integer.parseInt(header.substring("Content-Length: ".length()));
              } else if(header.startsWith("Content-Type: ")) {
                contentType = header.substring("Content-Type: ".length());
              }
            }
            if(contentType != null && contentType.startsWith("multipart/")) {
              byte[] dataBytes;
              if(contentLength > 0) {
                dataBytes = new byte[contentLength];
                in.read(dataBytes);
              } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] bytes = new byte[1024];
                for(int i; (i=in.read(bytes)) != -1; baos.write(bytes, 0, i));
                dataBytes = baos.toByteArray();
              }
              String boundary = "--" + contentType.substring(contentType.indexOf("boundary=") + "boundary=".length());
              byte[] boundaryBytes = boundary.getBytes("UTF-8");
              List<Integer> indexList = new ArrayList<Integer>();
              for(int i=0; i<dataBytes.length - boundaryBytes.length; i++) {
                boolean isFound = true;
                for(int j=0; j<boundaryBytes.length; j++) {
                  if(dataBytes[i + j] != boundaryBytes[j]) {
                    isFound = false;
                    break;
                  }
                }
                if(isFound) {
                  indexList.add(i);
                  i += boundaryBytes.length;
                }
              }
              httpDataArray = new HTTPData[indexList.size() - 1];
              for(int i=0; i<httpDataArray.length; i++) {
                HTTPData httpData = new HTTPData();
                httpDataArray[i] = httpData;
                int start = indexList.get(i);
                ByteArrayInputStream bais = new ByteArrayInputStream(dataBytes, start, indexList.get(i + 1) - start - in.getLineSeparator().length());
                HTTPInputStream din = new HTTPInputStream(bais);
                din.readAsciiLine();
                Map<String, String> headerMap = httpData.getHeaderMap();
                for(String header; (header = din.readAsciiLine()).length() > 0; ) {
                  String key = header.substring(header.indexOf(": "));
                  String value = header.substring(key.length() + ": ".length());
                  headerMap.put(key, value);
                }
                ByteArrayOutputStream aos = new ByteArrayOutputStream();
                for(int n; (n=din.read()) != -1; aos.write(n));
                httpData.setBytes(aos.toByteArray());
              }
            } else {
              InputStreamReader reader = new InputStreamReader(in, "UTF-8");
              String dataContent;
              if(contentLength > 0) {
                char[] chars = new char[contentLength];
                int offset = 0;
                while(chars.length > offset) {
                  int n = reader.read(chars, offset, chars.length - offset);
                  offset = n == -1? chars.length: offset + n;
                }
                dataContent = new String(chars);
              } else {
                StringBuilder sb = new StringBuilder();
                char[] chars = new char[1024];
                for(int i; (i=reader.read(chars)) != -1; sb.append(chars, 0, i));
                dataContent = sb.toString();
              }
              HTTPData httpData = new HTTPData();
              Map<String, String> headerMap = httpData.getHeaderMap();
              for(String content: dataContent.split("&")) {
                int eqIndex = content.indexOf('=');
                if(eqIndex > 0) {
                  String key = content.substring(0, eqIndex);
                  String value = Utils.decodeURL(content.substring(eqIndex + 1));
                  headerMap.put(key, value);
                } else {
                  headerMap.put(content, "");
                }
              }
              httpDataArray = new HTTPData[] {httpData};
            }
            httpRequest.setHTTPPostDataArray(httpDataArray);
          }
          WebServerContent webServerContent = getWebServerContent(httpRequest);
          InputStream resourceStream_ = null;
          if(webServerContent != null) {
            try {
              resourceStream_ = webServerContent.getInputStream();
            } catch(Exception e) {
              e.printStackTrace();
            }
          }
          if(resourceStream_ == null) {
            if(DEBUG_PRINT_REQUESTS) {
              System.err.println("Web Server " + (isPostMethod? "POST": "GET") + ": " + resourcePath + " -> 404 (not found)");
            }
            writeHTTPError(out, 404, "File Not Found.");
            return;
          }
          if(DEBUG_PRINT_REQUESTS) {
            System.err.println("Web Server " + (isPostMethod? "POST": "GET") + ": " + resourcePath + " -> 200 (OK)");
          }
          BufferedInputStream resourceStream = new BufferedInputStream(resourceStream_);
          writeHTTPHeaders(out, 200, webServerContent.getContentType(), webServerContent.getContentLength(), webServerContent.getLastModified());
          byte[] bytes = new byte[4096];
          for(int i; (i=resourceStream.read(bytes)) != -1; out.write(bytes, 0, i));
          try {
            resourceStream.close();
          } catch(Exception e) {
            e.printStackTrace();
          }
        } finally {
          out.flush();
          out.close();
          in.close();
          socket.close();
        }
      } catch(Exception e) {
//        e.printStackTrace();
      } finally {
        semaphore.release();
      }
    }
    
  }
  
  private int port;

  public WebServer() {
    this(0);
  }
  
  public WebServer(int port) {
    this.port = port;
  }
  
  private volatile boolean isRunning;
  
  public void stop() {
    isRunning = false;
  }
  
  public boolean isRunning() {
    return isRunning;
  }
  
  public void start() throws IOException {
    start(true);
  }
  
  private int instanceID;
  
  public void start(boolean isDaemon) throws IOException {
    if(isRunning) {
      return;
    }
    isRunning = true;
    instanceID = ObjectRegistry.getInstance().add(this);
    final ServerSocket serverSocket = new ServerSocket();
    serverSocket.bind(new InetSocketAddress(InetAddress.getByName(WebServer.getHostAddress()), port));
    port = serverSocket.getLocalPort();
    if(Boolean.parseBoolean(System.getProperty("nativeswing.webserver.debug.printport"))) {
      System.err.println("Web Server port: " + port);
    }
    Thread listenerThread = new Thread("WebServer") {
      @Override
      public void run() {
        while(isRunning) {
          try {
            Socket socket = serverSocket.accept();
            // Too risky if there are multiple network interfaces. Need to find a better way...
//            String hostAddress = socket.getInetAddress().getHostAddress();
//            if(!HOST_ADDRESS.equals(hostAddress) && !"127.0.0.1".equals(hostAddress)) {
//              throw new IllegalStateException("Illegal connection from host " + hostAddress);
//            }
            socket.setSoTimeout(10000);
            try {
              WebServerConnectionThread.semaphore.acquire();
            } catch(InterruptedException e) {
            }
            WebServerConnectionThread webServerConnectionThread = new WebServerConnectionThread(socket);
            webServerConnectionThread.start();
          } catch(IOException e) {
            e.printStackTrace();
          }
        }
        ObjectRegistry.getInstance().remove(instanceID);
      }
    };
    listenerThread.setDaemon(isDaemon);
    listenerThread.start();
  }
  
  public int getPort() {
    return port;
  }
  
  public String getURLPrefix() {
    return "http://" + hostAddress + ":" + port;
  }
  
  /**
   * @return A URL that when accessed will invoke the method <code>static WebServerContent getWebServerContent(HTTPRequest)</code> of the parameter class (the method visibility does not matter).
   */
  public String getDynamicContentURL(String className, String parameter) {
    return getURLPrefix() + "/class/" + instanceID + "/" + className + "/" + Utils.encodeURL(parameter);
  }
  
  /**
   * @return A URL that when accessed will invoke the method <code>static WebServerContent getWebServerContent(HTTPRequest)</code> of the parameter class (the method visibility does not matter).
   */
  public String getDynamicContentURL(String className, String codebase, String parameter) {
    return getURLPrefix() + "/class/" + instanceID + "/" + className + "/" + codebase + "/" + Utils.encodeURL(parameter);
  }
  
  public String getClassPathResourceURL(String className, String resourcePath) {
    if(!resourcePath.startsWith("/")) {
      String classPath = className.replace('.', '/');
      classPath = classPath.substring(0, classPath.lastIndexOf('/') + 1);
      resourcePath = "/" + classPath + resourcePath;
    }
    return getURLPrefix() + "/classpath/" + instanceID + Utils.simplifyPath(resourcePath);
  }
  
  public String getResourcePathURL(String codeBase, String resourcePath) {
    if(codeBase == null) {
      codeBase = ".";
    }
    return getURLPrefix() + "/resource/" + Utils.encodeURL(codeBase) + "/" + Utils.encodeURL(resourcePath);
  }
  
  public WebServerContent getURLContent(String resourceURL) {
    try {
      HTTPRequest httpRequest = new HTTPRequest(new URL(resourceURL).getPath());
      return getWebServerContent(httpRequest);
    } catch(Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  private List<ClassLoader> referenceClassLoaderList = new ArrayList<ClassLoader>(1);
  
  public void addReferenceClassLoader(ClassLoader referenceClassLoader) {
    if(referenceClassLoader == null || referenceClassLoader == getClass().getClassLoader()) {
      return;
    }
    referenceClassLoaderList.add(0, referenceClassLoader);
  }
  
  public void removeReferenceClassLoader(ClassLoader referenceClassLoader) {
    if(referenceClassLoader == null || referenceClassLoader == getClass().getClassLoader()) {
      return;
    }
    referenceClassLoaderList.remove(referenceClassLoader);
  }
  
  protected static WebServerContent getWebServerContent(HTTPRequest httpRequest) {
    String parameter = httpRequest.getResourcePath();
    if(parameter.startsWith("/")) {
      parameter = parameter.substring(1);
    }
    int index = parameter.indexOf('/');
    if(index == -1) {
      return null;
    }
    String type = parameter.substring(0, index);
    parameter = parameter.substring(index + 1);
    if("class".equals(type)) {
      index = parameter.indexOf('/');
      WebServer webServer = (WebServer)ObjectRegistry.getInstance().get(Integer.parseInt(parameter.substring(0, index)));
      if(webServer == null) {
        return null;
      }
      parameter = parameter.substring(index + 1);
      index = parameter.indexOf('/');
      String className = parameter.substring(0, index);
      parameter = Utils.decodeURL(parameter.substring(index + 1));
      httpRequest = httpRequest.clone();
      try {
        Class<?> clazz = null;
        for(ClassLoader referenceClassLoader: webServer.referenceClassLoaderList) {
          try {
            clazz = Class.forName(className, true, referenceClassLoader);
            break;
          } catch(Exception e) {
          }
        }
        if(clazz == null) {
          clazz = Class.forName(className);
        }
        Method getWebServerContentMethod = clazz.getDeclaredMethod("getWebServerContent", HTTPRequest.class);
        getWebServerContentMethod.setAccessible(true);
        httpRequest.setResourcePath(parameter);
        return (WebServerContent)getWebServerContentMethod.invoke(null, httpRequest);
      } catch(Exception e) {
        e.printStackTrace();
        return null;
      }
    }
    if("classpath".equals(type)) {
      index = parameter.indexOf('/');
      final WebServer webServer = (WebServer)ObjectRegistry.getInstance().get(Integer.parseInt(parameter.substring(0, index)));
      if(webServer == null) {
        return null;
      }
      parameter = parameter.substring(index + 1);
      final String resourcePath = parameter;
      return new WebServerContent() {
        @Override
        public String getContentType() {
          int index = resourcePath.lastIndexOf('.');
          return getDefaultMimeType(index == -1? null: resourcePath.substring(index));
        }
        @Override
        public InputStream getInputStream() {
          try {
            for(ClassLoader referenceClassLoader: webServer.referenceClassLoaderList) {
              InputStream in = referenceClassLoader.getResourceAsStream(resourcePath);
              if(in != null) {
                return in;
              }
            }
            return WebServer.class.getResourceAsStream('/' + resourcePath);
          } catch(Exception e) {
            e.printStackTrace();
            return null;
          }
        }
      };
    }
    if("resource".equals(type)) {
      parameter = Utils.decodeURL(parameter);
      index = parameter.indexOf('/');
      String codeBase = Utils.decodeURL(parameter.substring(0, index));
      parameter = parameter.substring(index + 1);
      String resourceURL;
      try {
        URL url = new URL(codeBase);
        int port = url.getPort();
        resourceURL = url.getProtocol() + "://" + url.getHost() + (port != -1? ":" + port: "");
        if(parameter.startsWith("/")) {
          resourceURL += parameter;
        } else {
          String path = url.getPath();
          path = path.substring(0, path.lastIndexOf('/') + 1) + parameter;
          resourceURL += path.startsWith("/")? path: "/" + path;
        }
      } catch(Exception e) {
        File file = Utils.getLocalFile(new File(codeBase, parameter).getAbsolutePath());
        if(file != null) {
          resourceURL = new File(codeBase, parameter).toURI().toString();
        } else {
          resourceURL = codeBase + "/" + parameter;
        }
      }
      final String resourceURL_ = resourceURL;
      return new WebServerContent() {
        @Override
        public long getContentLength() {
          File file = Utils.getLocalFile(resourceURL_);
          if(file != null) {
            return file.length();
          }
          return super.getContentLength();
        }
        @Override
        public String getContentType() {
          int index = resourceURL_.lastIndexOf('.');
          return getDefaultMimeType(index == -1? null: resourceURL_.substring(index));
        }
        @Override
        public InputStream getInputStream() {
          String url = resourceURL_;
          try {
            return new URL(url).openStream();
          } catch(Exception e) {
          }
          try {
            return new FileInputStream("/" + url);
          } catch(Exception e) {
            e.printStackTrace();
          }
          return null;
        }
      };
    }
    return null;
  }

  private static WebServer webServer;
  private static Object LOCK = new Object();
  private static String hostAddress;
  
  static {
    String hostAddress = System.getProperty("nativeswing.webserver.hostaddress");
    if("<localhost>".equals(hostAddress)) {
      try {
        hostAddress = InetAddress.getLocalHost().getHostAddress();
      } catch(Exception e) {
      }
    }
    if(hostAddress == null) {
      hostAddress = "127.0.0.1";
    }
    setHostAddress(hostAddress);
  }
  
  /**
   * Set the host address which is used when the URL prefix is requested, which by default is "127.0.0.1".
   * @param hostAddress The new host address.
   */
  public static void setHostAddress(String hostAddress) {
    WebServer.hostAddress = hostAddress;
    if(Boolean.parseBoolean(System.getProperty("nativeswing.webserver.debug.printhostaddress"))) {
      System.err.println("Web Server host address: " + hostAddress);
    }
  }
  
  public static String getHostAddress() {
    return hostAddress;
  }
  
  public static WebServer getDefaultWebServer() {
    synchronized(LOCK) {
      if(webServer != null) {
        return webServer;
      }
      webServer = new WebServer() {
        @Override
        public void stop() {
          throw new IllegalStateException("The default web server may be shared and thus cannot be stopped!");
        }
      };
      try {
        webServer.start();
      } catch(Exception e) {
        e.printStackTrace();
      }
      return webServer;
    }
  }
  
}
