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
import java.util.Map;
import java.util.concurrent.Semaphore;

import chrriis.dj.nativeswing.NSSystemProperty;

/**
 * @author Christopher Deckers
 */
public class WebServer {

  public static class HTTPRequest implements Cloneable {
    HTTPRequest(String urlPath, Map<String, String> headerMap) {
      this.headerMap = headerMap == null? new HashMap<String, String>(): headerMap;
      setURLPath(urlPath);
    }
    private Map<String, String> headerMap;
    public Map<String, String> getHeaderMap() {
      return headerMap;
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
      urlPath = resourcePath + endQuery;
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

    private static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";

    public static String getDefaultMimeType(String extension) {
      String mimeType = MimeTypes.getMimeType(extension);
      return mimeType == null? MIME_APPLICATION_OCTET_STREAM: mimeType;
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
            for(int b; (b=read()) != '\r' && b != -1; baos.write(b)) {
            }
            break;
          case LF:
            for(int b; (b=read()) != '\n' && b != -1; baos.write(b)) {
            }
            break;
          case CRLF:
            for(int b; (b=read()) != '\r' && b != -1; baos.write(b)) {
            }
            read();
            break;
        }
        return new String(baos.toByteArray(), "UTF-8");
      }
      @Override
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
          Map<String, String> headerMap = new HashMap<String, String>();
          for(String header; (header = in.readAsciiLine()).length() > 0; ) {
            int index = header.indexOf(": ");
            if(index > 0) {
              headerMap.put(header.substring(0, index), header.substring(index + ": ".length()));
            }
          }
          HTTPRequest httpRequest = new HTTPRequest(resourcePath, headerMap);
          httpRequest.setPostMethod(isPostMethod);
          if(isPostMethod) {
            HTTPData[] httpDataArray;
            String contentType = headerMap.get("Content-Type");
            String contentLengthString = headerMap.get("Content-Length");
            int contentLength = contentLengthString == null? -1: Integer.parseInt(contentLengthString);
            if(contentType != null && contentType.startsWith("multipart/")) {
              byte[] dataBytes;
              if(contentLength > 0) {
                dataBytes = new byte[contentLength];
                in.read(dataBytes);
              } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] bytes = new byte[1024];
                for(int i; (i=in.read(bytes)) != -1; baos.write(bytes, 0, i)) {
                }
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
                Map<String, String> dataHeaderMap = httpData.getHeaderMap();
                for(String header; (header = din.readAsciiLine()).length() > 0; ) {
                  String key = header.substring(header.indexOf(": "));
                  String value = header.substring(key.length() + ": ".length());
                  dataHeaderMap.put(key, value);
                }
                ByteArrayOutputStream aos = new ByteArrayOutputStream();
                for(int n; (n=din.read()) != -1; aos.write(n)) {
                }
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
                for(int i; (i=reader.read(chars)) != -1; sb.append(chars, 0, i)) {
                }
                dataContent = sb.toString();
              }
              HTTPData httpData = new HTTPData();
              Map<String, String> dataHeaderMap = httpData.getHeaderMap();
              for(String content: dataContent.split("&")) {
                int eqIndex = content.indexOf('=');
                if(eqIndex > 0) {
                  String key = content.substring(0, eqIndex);
                  String value = Utils.decodeURL(content.substring(eqIndex + 1));
                  dataHeaderMap.put(key, value);
                } else {
                  dataHeaderMap.put(content, "");
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
          boolean isPrintRequestsDebug = Boolean.parseBoolean(NSSystemProperty.WEBSERVER_DEBUG_PRINTREQUESTS.get());
          String printDataProperty = NSSystemProperty.WEBSERVER_DEBUG_PRINTDATA.get();
          boolean isPrintDataDebug = false;
          long printDataCount = -1;
          if(printDataProperty != null) {
            try {
              printDataCount = Long.parseLong(printDataProperty);
              isPrintDataDebug = true;
            } catch(Exception e) {
              isPrintDataDebug = Boolean.parseBoolean(printDataProperty);
              printDataCount = Integer.MAX_VALUE;
            }
          }
          if(resourceStream_ == null) {
            if(isPrintRequestsDebug) {
              System.err.println("Web Server " + (isPostMethod? "POST": "GET") + ": " + resourcePath + " -> 404 (not found)");
            }
            writeHTTPError(out, 404, "File Not Found.");
            return;
          }
          if(isPrintRequestsDebug || isPrintDataDebug) {
            System.err.println("Web Server " + (isPostMethod? "POST": "GET") + ": " + resourcePath + " -> 200 (OK)");
          }
          BufferedInputStream resourceStream = new BufferedInputStream(resourceStream_);
          writeHTTPHeaders(out, 200, webServerContent.getContentType(), webServerContent.getContentLength(), webServerContent.getLastModified());
          byte[] bytes = new byte[4096];
          for(int i; (i=resourceStream.read(bytes)) != -1; out.write(bytes, 0, i)) {
            if(isPrintDataDebug && i > 0 && printDataCount > 0) {
              System.err.print(new String(bytes, 0, (int)Math.min(i, printDataCount), "UTF-8"));
              printDataCount -= i;
            }
          }
          if(isPrintDataDebug) {
            System.err.println();
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
    if(serverSocket != null) {
      ServerSocket serverSocket = this.serverSocket;
      this.serverSocket = null;
      try {
        serverSocket.close();
      } catch (IOException e) {
      }
    }
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void start() throws IOException {
    start(true);
  }

  private volatile ServerSocket serverSocket;
  private volatile int instanceID;

  public void start(boolean isDaemon) throws IOException {
    if(isRunning) {
      return;
    }
    isRunning = true;
    instanceID = ObjectRegistry.getInstance().add(this);
    serverSocket = new ServerSocket();
    serverSocket.bind(new InetSocketAddress(InetAddress.getByName(getHostAddress()), port));
    port = serverSocket.getLocalPort();
    if(Boolean.parseBoolean(NSSystemProperty.WEBSERVER_DEBUG_PRINTPORT.get())) {
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
          } catch(Exception e) {
            if(serverSocket != null) {
              e.printStackTrace();
            }
          }
        }
        serverSocket = null;
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
    if(hostAddress.indexOf(':') >= 0) {
      // IPv6
      return "http://[" + hostAddress + "]:" + port;
    }
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
      codeBase = new File(SystemProperty.USER_DIR.get()).getAbsolutePath();
    }
    if(Boolean.parseBoolean(NSSystemProperty.WEBSERVER_ACTIVATEOLDRESOURCEMETHOD.get())) {
      if(Utils.IS_WINDOWS) {
        // '\' is not allowed in URL, and it is causing a problem with certain URL handlers. Let's replace with '/'
        codeBase = codeBase.replace('\\', '/');
        resourcePath = resourcePath.replace('\\', '/');
      }
      return getURLPrefix() + "/resource/" + Utils.encodeURL(codeBase) + "/" + Utils.encodeURL(resourcePath);
    }
    return getURLPrefix() + "/location/" + Utils.encodeBase64(codeBase, true) + "/" + Utils.encodeURL(resourcePath);
  }

  public WebServerContent getURLContent(String resourceURL) {
    try {
      HTTPRequest httpRequest = new HTTPRequest(new URL(resourceURL).getPath(), null);
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

  /**
   * A content provider for global resources.
   * @author Christopher Deckers
   */
  public static interface WebServerContentProvider {
    /**
     * Get the web server content, or return null to ignore the request.
     * @param httpRequest the request.
     * @return the content, or null to ignore the request and potentially let another handler process it.
     */
    public WebServerContent getWebServerContent(HTTPRequest httpRequest);
  }

  private List<WebServerContentProvider> contentProviderList = new ArrayList<WebServerContentProvider>();

  /**
   * Add a content provider for content that is not natively supported by the web server.
   * @param webServerContentProvider the content provider to add.
   */
  public void addContentProvider(WebServerContentProvider webServerContentProvider) {
    contentProviderList.add(webServerContentProvider);
  }

  /**
   * Remove a content provider.
   * @param webServerContentProvider the content provider to remove.
   */
  public void removeContentProvider(WebServerContentProvider webServerContentProvider) {
    contentProviderList.remove(webServerContentProvider);
  }

  protected static WebServerContent getWebServerContent(HTTPRequest httpRequest) {
    String parameter = httpRequest.getResourcePath();
    if(parameter.startsWith("/")) {
      parameter = parameter.substring(1);
    }
    int index = parameter.indexOf('/');
    if(index != -1) {
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
        final String resourcePath = removeHTMLAnchor(parameter);
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
      if("location".equals(type)) {
        index = parameter.indexOf('/');
        String codeBase = Utils.decodeBase64(parameter.substring(0, index));
        parameter = Utils.decodeURL(removeHTMLAnchor(parameter.substring(index + 1)));
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
          // Exception when creating a URL, so it is malformed: it is likely to be a local file.
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
            try {
              return new URL(resourceURL_).openStream();
            } catch(Exception e) {
            }
            try {
              return new FileInputStream("/" + resourceURL_);
            } catch(Exception e) {
              e.printStackTrace();
            }
            return null;
          }
        };
      }
      if("resource".equals(type)) {
        index = parameter.indexOf('/');
        if(index > 0) {
          String subs = parameter.substring(index - 1);
          if(subs.startsWith("://")) {
            index = parameter.indexOf('/', index + 2);
          }
        }
        String codeBase = Utils.decodeURL(parameter.substring(0, index));
        parameter = Utils.decodeURL(parameter.substring(index + 1));
        String resourceURL;
        try {
          URL url = new URL(codeBase);
          int port = url.getPort();
          resourceURL = url.getProtocol() + "://" + url.getHost() + (port != -1? ":" + port: "");
          if(parameter.startsWith("/")) {
            resourceURL += removeHTMLAnchor(parameter);
          } else {
            String path = url.getPath();
            path = path.substring(0, path.lastIndexOf('/') + 1) + parameter;
            resourceURL += path.startsWith("/")? path: "/" + path;
          }
        } catch(Exception e) {
          File file = Utils.getLocalFile(new File(codeBase, removeHTMLAnchor(parameter)).getAbsolutePath());
          if(file != null) {
            resourceURL = new File(codeBase, removeHTMLAnchor(parameter)).toURI().toString();
          } else {
            resourceURL = codeBase + "/" + removeHTMLAnchor(parameter);
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
            try {
              return new URL(resourceURL_).openStream();
            } catch(Exception e) {
            }
            try {
              return new FileInputStream("/" + resourceURL_);
            } catch(Exception e) {
              e.printStackTrace();
            }
            return null;
          }
        };
      }
    }
    for(WebServerContentProvider contentProvider: webServer.contentProviderList) {
      WebServerContent webServerContent = contentProvider.getWebServerContent(httpRequest);
      if(webServerContent != null) {
        return webServerContent;
      }
    }
    return null;
  }

  private static String removeHTMLAnchor(String location) {
    int anchorIndex = location.indexOf('#');
    if(anchorIndex > 0) {
      location = location.substring(0, anchorIndex);
    }
    return location;
  }

  private static WebServer webServer;
  private static Object LOCK = new Object();
  private static String hostAddress;

  static {
    String hostAddress = Utils.getLocalHostAddress();
    if(hostAddress == null) {
      hostAddress = "127.0.0.1";
    }
    WebServer.hostAddress = hostAddress;
  }

  private static String getHostAddress() {
    return hostAddress;
  }

  public static void stopDefaultWebServer() {
    synchronized(LOCK) {
      if(webServer != null) {
        webServer.stop();
        webServer = null;
      }
    }
  }

  public static WebServer getDefaultWebServer() {
    synchronized(LOCK) {
      if(webServer != null) {
        return webServer;
      }
      webServer = new WebServer();
      try {
        boolean isApplet = "applet".equals(NSSystemProperty.DEPLOYMENT_TYPE.get());
        webServer.start(!isApplet);
      } catch(Exception e) {
        e.printStackTrace();
      }
      return webServer;
    }
  }

}
