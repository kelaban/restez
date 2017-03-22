package com.k317h.restez.route;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Preconditions;
import com.k317h.restez.HttpMethod;

public class RouteSpec {
  private final String path;
  private final RegexPathMatcher regexPath;
  private final HttpMethod verb;
  
  private RouteSpec(String path, HttpMethod verb) {
    Preconditions.checkNotNull(path, "Path cannot be null");
    Preconditions.checkNotNull(verb, "verb cannot be null");
    
    this.path = path;
    this.regexPath = RegexPathMatcher.fromPath(path); 
    this.verb = verb;
  }
  
  public boolean matches(HttpServletRequest request) {
    HttpMethod requestMethod = HttpMethod.valueOf(request.getMethod().toLowerCase());
    String path = request.getRequestURI();
    return requestMethod.equals(verb) && regexPath.matches(path);
  }
  
  public String getPath() {
    return path;
  }

  public RegexPathMatcher getRegexPath() {
    return regexPath;
  }

  public HttpMethod getVerb() {
    return verb;
  }

  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private String path;
    private HttpMethod verb;
    
    public Builder path(String path) {
      this.path = path;
      return this;
    }
    
    public Builder verb(HttpMethod verb) {
      this.verb = verb;
      return this;
    }
    
    public RouteSpec build() {      
      return new RouteSpec(path, verb);
    }
    
  }

}
