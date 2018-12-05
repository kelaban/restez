package com.k317h.restez.io;

import com.k317h.restez.HttpMethod;
import com.k317h.restez.http.HttpHeader;
import com.k317h.restez.route.RegexPathMatcher.PathParams;
import com.k317h.restez.serialization.Deserializers;
import com.k317h.restez.util.AtomicSingleton;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Request {
  private final HttpServletRequest httpServletRequest;
  private final PathParams matchedParams;
  private final Deserializers deserializers;

  public Request(Request in, HttpServletRequest httpServletRequest) {
    this(httpServletRequest, in.matchedParams, in.deserializers);
  }

  public Request(HttpServletRequest httpServletRequest, PathParams matchedParams, Deserializers deserializers) {
    this.httpServletRequest = httpServletRequest;
    this.matchedParams = matchedParams;
    this.deserializers = deserializers;
  }

  public Map<String, QueryParam> query() {
    return this.httpServletRequest
        .getParameterMap()
        .entrySet()
        .stream()
        .collect(Collectors.toMap(e -> e.getKey(), e -> this.query(e.getKey())));
  }

  public QueryParam query(String param) {
    return new QueryParam(
        param,
        Optional.ofNullable(
            this.httpServletRequest.getParameterMap().get(param)
        ));
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

  public String body() throws IOException {
    return body.getOrSet(() -> {
      StringWriter sw = new StringWriter();
      IOUtils.copy(inputStream(), sw, Charset.forName("UTF-8"));
      return sw.toString();
    });
  }

  public <T> T body(Class<T> clazz) throws Exception {
    return deserializers.deserialize(body().getBytes(), contentType(), clazz);
  }

  public String path() {
    return httpServletRequest.getPathInfo();
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

  public String contentType() {
    String contentType = rawRequest().getHeader(HttpHeader.CONTENT_TYPE.asString());
    if (contentType == null) {
      return null;
    }

    if (!contentType.contains(";")) {
      return contentType.trim();
    }

    String[] parts = contentType.split(";");
    if (parts.length < 1) {
      return contentType.trim();
    } else {
      return parts[0].trim();
    }

  }

  public String contentEncoding() {
    String contentEncoding = rawRequest().getHeader(HttpHeader.CONTENT_ENCODING.asString());
    if (contentEncoding == null) {
      return null;
    }
    return contentEncoding.trim();
  }

  public String acceptEncoding() {
    String acceptEncoding = rawRequest().getHeader(HttpHeader.ACCEPT_ENCODING.asString());
    if (acceptEncoding == null) {
      return null;
    }
    return acceptEncoding.trim();
  }
}
