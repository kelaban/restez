package com.k317h.restez.route;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import com.k317h.restez.HttpMethod;

public class RouteSpec {
  private final String path;
  private final RegexPathMatcher regexPath;
  private final HttpMethod verb;
  
  private RouteSpec(String path, HttpMethod verb) {
    Objects.requireNonNull(path, "Path cannot be null");
    Objects.requireNonNull(verb, "verb cannot be null");
    
    this.path = path;
    this.regexPath = RegexPathMatcher.fromPath(path); 
    this.verb = verb;
  }
  
  public boolean matches(HttpServletRequest request) {
    HttpMethod requestMethod = HttpMethod.valueOf(request.getMethod().toLowerCase());
    String path = request.getPathInfo();

    boolean isRequestingHead = requestMethod.equals(HttpMethod.head);
    boolean isActuallyGet = verb.equals(HttpMethod.get);
    
    boolean verbMatches = requestMethod.equals(verb) || (isRequestingHead && isActuallyGet);

    return verbMatches && regexPath.matches(path);
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
