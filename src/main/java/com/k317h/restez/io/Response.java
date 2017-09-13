package com.k317h.restez.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.MimeTypes;

import com.k317h.restez.Serializers;

public class Response {
  private final HttpServletResponse httpServletResponse;
  private final Serializers serializers;
  int status;
  
  public Response(Response res) {
    this(res.httpServletResponse, res.serializers);
  }
  
  public Response(HttpServletResponse httpServletResponse, Serializers serializers) {
    this.httpServletResponse = httpServletResponse;
    this.serializers = serializers;
  }


  public Response send(String body) throws IOException {
    return send(serializers.serializeDefault(body));
  }

  public Response send(byte[] body) throws IOException {
    outputStream().write(body);
    return this;
  }
  
  public Response send(Object body) throws IOException {
    return send(serializers.serialize(body, contentType()));
  } 
  
  public Response json(Object body) throws IOException {
    contentType(MimeTypes.Type.APPLICATION_JSON.asString());
    return send(body);
  }

  public Response send(InputStream is) throws IOException {
    OutputStream os = outputStream();
    IOUtils.copy(is, os);
    return this;
  }
  

  public OutputStream outputStream() throws IOException {
    return httpServletResponse.getOutputStream();
  }


  public Response contentType(String contentType) { 
    httpServletResponse.setContentType(contentType);
    return this;
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
