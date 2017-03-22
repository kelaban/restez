package com.k317h.restez.route;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.k317h.restez.Handler;
import com.k317h.restez.Middleware;
import com.k317h.restez.route.RegexPathMatcher.PathParams;

public class RouteMatch {
 
  private RouteSpec spec;
  private Collection<Middleware> middleware;
  private Handler handler;

  public RouteMatch(RouteSpec spec, Handler handler, Collection<Middleware> middleware) {
    this.spec = spec;
    this.middleware = middleware;
    this.handler = handler;
  }

  public boolean matches(HttpServletRequest request) {
    return spec.matches(request);
  }

  public PathParams parsePathParam(String path) {
    return getSpec().getRegexPath().parsePathParams(path);
  }

  public RouteSpec getSpec() {
    return spec;
  }

  public Collection<Middleware> getMiddleware() {
    return middleware;
  }

  public Handler getHandler() {
    return handler;
  }
}
