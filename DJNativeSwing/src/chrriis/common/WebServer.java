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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Locale;

/**
 * @author Christopher Deckers
 */
public class WebServer {

  public static abstract class WebServerContent {
    
    public static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String MIME_TEXT = "text/plain";
    public static final String MIME_TEXT_HTML = "text/html";
    public static final String MIME_IMAGE_GIF = "image/gif";
    public static final String MIME_IMAGE_PNG = "image/png";
    public static final String MIME_IMAGE_JPEG = "image/jpeg";
    
    public static String getDefaultMimeType(String extension) {
      if(extension == null) {
        return MIME_APPLICATION_OCTET_STREAM;
      }
      if(extension.startsWith(".")) {
        extension = extension.substring(1);
      }
      extension = extension.toLowerCase(Locale.ENGLISH);
      if("txt".equals(extension)) {
        return MIME_TEXT;
      }
      if("html".equals(extension) || "htm".equals(extension)) {
        return MIME_TEXT_HTML;
      }
      if("gif".equals(extension)) {
        return MIME_IMAGE_GIF;
      }
      if("png".equals(extension)) {
        return MIME_IMAGE_PNG;
      }
      if("jpg".equals(extension) || "jpeg".equals(extension)) {
        return MIME_IMAGE_JPEG;
      }
      return MIME_APPLICATION_OCTET_STREAM;
    }
    
    public abstract InputStream getInputStream();
    public abstract String getContentType();
    public int getContentLength() {
      return -1;
    }
    public long getLastModified() {
      return System.currentTimeMillis();
    }
    
  }
  
  protected static class WebServerConnectionThread extends Thread {
    
    protected Socket socket;
    
    public WebServerConnectionThread(Socket socket) {
      super("WebServer Connection");
      this.socket = socket;
      setDaemon(true);
    }
    
    protected static final String LS = System.getProperty("line.separator");
    
    protected static void writeHTTPHeaders(BufferedOutputStream out, int code, String contentType, long contentLength, long lastModified) {
      StringBuilder sb = new StringBuilder();
      sb.append("HTTP/1.0 " + code + " OK" + LS);
      sb.append("Content-Type: " + contentType + LS);
      sb.append("Server: WebServer/1.0");
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

    protected static void writeHTTPError(BufferedOutputStream out, int code, String message) {
      writeHTTPHeaders(out, code, "text/html", message.length(), System.currentTimeMillis());
      try {
        out.write(message.getBytes("UTF-8"));
        out.flush();
        out.close();
      } catch(IOException e) {
//        e.printStackTrace();
      }
    }
    
    @Override
    public void run() {
      try {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        try {
          String request = in.readLine();
          if(request == null || !request.startsWith("GET ") || !(request.endsWith(" HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
            writeHTTPError(out, 500, "Invalid Method.");
            return;
          }            
          String path = request.substring("GET ".length(), request.length() - " HTTP/1.0".length());
          if(path.startsWith("/")) {
            path = path.substring(1);
          }
          int index = path.indexOf('/');
          if(index == 0) {
            writeHTTPError(out, 404, "File Not Found.");
            return;
          }
          String className = path.substring(0, index);
          String resourcePath = path.substring(index + 1);
          Class<?> clazz = Class.forName(className);
          Method getWebServerContentMethod = clazz.getDeclaredMethod("getWebServerContent", String.class);
          getWebServerContentMethod.setAccessible(true);
          WebServerContent webServerContent = (WebServerContent)getWebServerContentMethod.invoke(null, Utils.decodeURL(resourcePath));
          InputStream resourceStream_ = webServerContent.getInputStream();
          if(resourceStream_ == null) {
            writeHTTPError(out, 404, "File Not Found.");
            return;
          }
          BufferedInputStream resourceStream = new BufferedInputStream(resourceStream_);
          writeHTTPHeaders(out, 200, webServerContent.getContentType(), webServerContent.getContentLength(), webServerContent.getLastModified());
          byte[] bytes = new byte[4096];
          for(int i; (i=resourceStream.read(bytes)) != -1; ) {
            out.write(bytes, 0, i);
          }
          try {
            resourceStream.close();
          } catch(Exception e) {
            e.printStackTrace();
          }
        } finally {
          out.flush();
          out.close();
          in.close();
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    
  }
  
  protected int port;

  public WebServer() {
    this(0);
  }
  
  public WebServer(int port) {
    this.port = port;
  }
  
  protected volatile boolean isRunning;
  
  public void stop() {
    isRunning = false;
  }
  
  public void start() throws IOException {
    if(isRunning) {
      return;
    }
    isRunning = true;
    final ServerSocket serverSocket = new ServerSocket(port);
    port = serverSocket.getLocalPort();
    Thread listenerThread = new Thread("WebServer") {
      @Override
      public void run() {
        while(isRunning) {
          try {
            Socket socket = serverSocket.accept();
            socket.setSoTimeout(10000);
            WebServerConnectionThread webServerConnectionThread = new WebServerConnectionThread(socket);
            webServerConnectionThread.start();
          } catch(IOException e) {
            e.printStackTrace();
          }
        }
      }
    };
    listenerThread.setDaemon(true);
    listenerThread.start();
  }
  
  public int getPort() {
    return port;
  }
  
  public String getResourcePath(String className, String resourcePath) {
    String path = port + "/" + className + "/" + Utils.encodeURL(resourcePath);
    try {
      return "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + path;
    } catch(Exception e) {
      return "http://127.0.0.1:" + path;
    }
  }
  
  protected static WebServer webServer;
  protected static Object LOCK = new Object();
  
  public static WebServer getDefaultWebServer() {
    synchronized(LOCK) {
      if(webServer != null) {
        return webServer;
      }
      webServer = new WebServer() {
        @Override
        public void stop() {
          // Cannot be stopped
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
