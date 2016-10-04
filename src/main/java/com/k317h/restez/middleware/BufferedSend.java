package com.k317h.restez.middleware;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.k317h.restez.Handler;
import com.k317h.restez.Middleware;
import com.k317h.restez.io.Request;
import com.k317h.restez.io.Response;

public class BufferedSend implements Middleware {

  @Override
  public void handle(Request req, Response res, Handler next) throws Exception {
    BufferedResponse bufferedRes = new BufferedResponse(res);
    
    next.handle(req, bufferedRes);
    
    bufferedRes.rawResponse().setContentLength((int) bufferedRes.bufferSize());
    bufferedRes.finish();
  }
  
  public static class BufferedResponse extends Response {
    ByteArrayOutputStream buff = new ByteArrayOutputStream();

    public BufferedResponse(Response res) {
      super(res.rawResponse());
    }
    
    @Override
    public OutputStream outputStream() {
      return buff;
    }
    
    public void finish() throws IOException {
      buff.writeTo(super.outputStream());
    }
    
    public long bufferSize() {
      return buff.size();
    }
    
  }
}
