package com.k317h.restez.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;

import com.k317h.restez.HttpMethod;
import com.k317h.restez.route.RegexPathMatcher.PathParams;

import util.AtomicSingleton;

public class Request {
  private final HttpServletRequest httpServletRequest;
  private final PathParams matchedParams;

  public Request(HttpServletRequest httpServletRequest, PathParams matchedParams) {
    this.httpServletRequest = httpServletRequest;
    this.matchedParams = matchedParams;
  }

  @SuppressWarnings("unchecked")
  public Map<String, String[]> query() {
    return this.httpServletRequest.getParameterMap();
  }
  
  public Optional<String[]> query(String param) {
    return Optional.ofNullable(query().get(param));
  }


  public Map<String, String> params() {
    return matchedParams.namedParams;
  }
  
  public String params(String param) {
    return params().get(param);
  }
  
  public String params(String param, String defaultValue) {
    return params().getOrDefault(param, defaultValue);
  }


  public List<String> splat() {
    return matchedParams.splatParams;
  }
  
  public String splat(int param) {
    return splat().get(param);
  }
  

  public String rawSplat() {
    return matchedParams.rawSplat;
  }


  public InputStream inputStream() throws IOException {
    return httpServletRequest.getInputStream();
  }
  
  AtomicSingleton<String> body = new AtomicSingleton<String>();


  public Object body() {
    return body.getOrSet(() -> {
      StringWriter sw = new StringWriter();
      try {
        IOUtils.copy(inputStream(), sw, Charset.forName("UTF-8"));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return sw.toString();
    });
  }


  public String path() {
    return httpServletRequest.getRequestURI();
  }


  public HttpMethod method() {
    return HttpMethod.valueOf(httpServletRequest.getMethod().toLowerCase());
  }


  public HttpServletRequest rawRequest() {
    return this.httpServletRequest;
  }
  
  public PathParams matchedParams() {
    return matchedParams;
  }
  
  public String getContentEncoding() {
    String contentEncoding = rawRequest().getHeader(HttpHeaders.CONTENT_ENCODING);
    if (contentEncoding == null) {
        return null;
    }
    contentEncoding.trim();
    return contentEncoding;
  }
  
  public String getAcceptEncoding() {
    String contentEncoding = rawRequest().getHeader(HttpHeaders.ACCEPT_ENCODING);
    if (contentEncoding == null) {
        return null;
    }
    contentEncoding.trim();
    return contentEncoding;
  }
}
