package com.k317h.restez.middleware;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;

import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.k317h.restez.Handler;
import com.k317h.restez.Middleware;
import com.k317h.restez.io.Request;
import com.k317h.restez.io.Response;

public class GZIPMiddleware implements Middleware {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  @Override
  public void handle(Request req, Response res, Handler next) throws Exception {
    GZippedResponse gzresp = null;
    
    if(isGzipEncoded(req.contentEncoding())) {
      log.debug("using gzipped request");
      req = new Request(req, new GZippedRequest(req));
    }
    
    if(isGzipEncoded(req.acceptEncoding())) {
      log.debug("using gzipped response");
      
      gzresp = new GZippedResponse(res);
      res = new Response(res, gzresp);
      
      res.header(HttpHeader.CONTENT_ENCODING.asString(), "gzip");
    }
    
    next.handle(req, res);
    
    if(null != gzresp) {
      gzresp.finish();
    }
  }
  
  public static class GZippedRequest extends HttpServletRequestWrapper {
    private final GZIPServletInputStream gis;
    
    public GZippedRequest(Request req) throws IOException {
      super(req.rawRequest());
      gis = new GZIPServletInputStream(super.getInputStream());
    }
    
    @Override
    public ServletInputStream getInputStream() {
      return gis;
    }
    
  }
  
  private static class GZIPServletInputStream extends ServletInputStream {
    private final GZIPInputStream gis;
    
    public GZIPServletInputStream(InputStream source) throws IOException {
      this.gis = new GZIPInputStream(source);
    }
    
    @Override
    public int read() throws IOException {
      return gis.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      return gis.read(b, off, len);
    }
    
  }
  
  
  public static class GZippedResponse extends HttpServletResponseWrapper {
    final private GZIPServletOutputStream gzos;
    
    public GZippedResponse(Response in) throws IOException {
      super(in.rawResponse());
      gzos = new GZIPServletOutputStream(super.getOutputStream()); 
    }
    
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
      return gzos;
    }
    
    public void finish() throws IOException {
      gzos.finish();
    }
    
  }
  
  private static class GZIPServletOutputStream extends ServletOutputStream {
    private final GZIPOutputStream gzos;
    
    public GZIPServletOutputStream(OutputStream dest) throws IOException {
      this.gzos = new GZIPOutputStream(dest);
    }

    @Override
    public void write(int b) throws IOException {
      gzos.write(b);
    }
    
    public void finish() throws IOException {
      gzos.finish();
    }
    
  }
  
  private static boolean isGzipEncoded(String encoding) {
    return null != encoding && ("gzip".contains(encoding) || "deflate".contains(encoding));
  }

}
