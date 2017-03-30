package com.k317h.restez.io;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class Response {
  private HttpServletResponse httpServletResponse;
  int status;
  
  public Response(HttpServletResponse httpServletResponse) {
    this.httpServletResponse = httpServletResponse;
  }


  public Response send(String body) throws IOException {
    outputStream().write(body.getBytes());
    return this;
  }
  
  public Response json(String body) throws IOException {
    contentType("application/json");
    send(body);
    return this;
  }


  public Response send(InputStream is) throws IOException {
    OutputStream os = outputStream();
    IOUtils.copy(is, os);
    return this;
  }
  

  public OutputStream outputStream() throws IOException {
    return httpServletResponse.getOutputStream();
  }


  public void contentType(String contentType) { 
    httpServletResponse.setContentType(contentType);
  }


  public String contentType() {
    return httpServletResponse.getContentType();
  }


  public Response status(int status) {
    httpServletResponse.setStatus(status);
    return this;
  }


  public Response header(String key, String value) {
    httpServletResponse.addHeader(key, value);
    return this;
  }
  
  public HttpServletResponse rawResponse() {
    return httpServletResponse;
  }

}
