package com.k317h.restez.middleware;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpHeaders;
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
    boolean usingGzipResponse = false;
    
    if(isGzipEncoded(req.getContentEncoding())) {
      log.debug("using gzipped request");
      req = new GZippedRequest(req);
    }
    
    if(isGzipEncoded(req.getAcceptEncoding())) {
      log.debug("using gzipped response");
      
      res = new GZippedResponse(res);
      
      res.header(HttpHeaders.CONTENT_ENCODING, "gzip");
      usingGzipResponse = true;
    }
    
    next.handle(req, res);
    
    if(usingGzipResponse) {
      ((GZIPOutputStream) res.outputStream()).finish();
    }
  }
  
  public static class GZippedRequest extends Request {
    final GZIPInputStream gis;
    
    public GZippedRequest(Request in) throws IOException {
      super(in.rawRequest(), in.matchedParams());
      gis = new GZIPInputStream(in.getInputStream());
    }
    
    @Override
    public InputStream getInputStream() throws IOException {   
      return gis;    
    }

  }
  
  public static class GZippedResponse extends Response {
    final private GZIPOutputStream gzos;
    
    public GZippedResponse(Response in) throws IOException {
      super(in.rawResponse());
      gzos = new GZIPOutputStream(in.outputStream()); 
    }
    
    @Override
    public OutputStream outputStream() throws IOException {
      return gzos;
    }
    
  }
  
  private static boolean isGzipEncoded(String encoding) {
    return "gzip".equals(encoding) || "deflate".equals(encoding);
  }

}
