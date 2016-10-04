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


  public void send(String body) throws IOException {
    outputStream().write(body.getBytes());
  }
  
  public void json(String body) throws IOException {
    contentType("application/json");
    send(body);
  }


  public void send(InputStream is) throws IOException {
    OutputStream os = outputStream();
    IOUtils.copy(is, os);
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


  public void status(int status) {
    httpServletResponse.setStatus(status);
  }


  public void header(String key, String value) {
    httpServletResponse.addHeader(key, value);
  }
  
  public HttpServletResponse rawResponse() {
    return httpServletResponse;
  }

}
