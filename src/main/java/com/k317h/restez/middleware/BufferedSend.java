package com.k317h.restez.middleware;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.k317h.restez.Handler;
import com.k317h.restez.Middleware;
import com.k317h.restez.io.Request;
import com.k317h.restez.io.Response;

/**
 * 
 * Buffers output to set content length
 *
 */
public class BufferedSend implements Middleware {

  @Override
  public void handle(Request req, Response res, Handler next) throws Exception {
    BufferedResponse bufferedRes = new BufferedResponse(res);
    
    next.handle(req, new Response(res, bufferedRes));
    
    bufferedRes.finish();
  }
  
  public static class BufferedResponse extends HttpServletResponseWrapper {
    BufferedServletOutputStream buff = new BufferedServletOutputStream();
    Response res;

    public BufferedResponse(Response res) {
      super(res.rawResponse());
    }
    
    @Override
    public ServletOutputStream getOutputStream() {
      return buff;
    }
    
    
    public void finish() throws IOException {
      super.setContentLength(buff.size());
      buff.writeTo(super.getOutputStream());
    }
  }
  
  private static class BufferedServletOutputStream extends ServletOutputStream{
    ByteArrayOutputStream buff = new ByteArrayOutputStream();

    @Override
    public void write(int b) throws IOException {
      buff.write(b);
    }
    
    public int size() {
      return buff.size();
    }
    
    public void writeTo(OutputStream os) throws IOException {
      buff.writeTo(os);
    }
    
  }

}
