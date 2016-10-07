package com.k317h.restez;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import util.PathUtils;

public final class Router {
  List<Middleware> middleware = new ArrayList<Middleware>();
  List<RouteMatch> routeMatches = new ArrayList<RouteMatch>();

  public Router use(Middleware mw, Middleware... mws) {
    middleware.add(mw);

    if (null != mws) {
      for (Middleware m : mws) {
        middleware.add(m);
      }
    }

    return this;
  }

  public Router use(String path, Router router) {
    for (RouteMatch rm : router.getRouteMatches()) {
      route(PathUtils.concatPath(path, rm.getPath()), rm.getMethod(), rm.getHandler(), rm.getMiddleware());
    }

    return this;
  }

  public Router get(String path, Handler handler, Middleware... mw) {
    return route(path, HttpMethod.get, handler, mw);
  }
  
  public Router post(String path, Handler handler, Middleware... mw) {
    return route(path, HttpMethod.post, handler, mw);
  }
  
  public Router put(String path, Handler handler, Middleware... mw) {
    return route(path, HttpMethod.put, handler, mw);
  }
  
  public Router patch(String path, Handler handler, Middleware... mw) {
    return route(path, HttpMethod.patch, handler, mw);
  }
  
  public Router delete(String path, Handler handler, Middleware... mw) {
    return route(path, HttpMethod.delete, handler, mw);
  }
  
  public Router head(String path, Handler handler, Middleware... mw) {
    return route(path, HttpMethod.head, handler, mw);
  }
  
  public Router options(String path, Handler handler, Middleware... mw) {
    return route(path, HttpMethod.options, handler, mw);
  }

  public Router route(String path, HttpMethod verb, Handler handler, Middleware... mw) {
    return route(path, verb, handler, Arrays.asList(mw));
  }

  public Router route(String path, HttpMethod verb, Handler handler, Collection<Middleware> mw) {
    routeMatches.add(new RouteMatch(path, verb, handler, concatMiddleware(middleware, mw)));
    return this;
  }

  public Collection<RouteMatch> getRouteMatches() {
    return routeMatches;
  }
  
  public Collection<Middleware> getMiddleware() {
    return middleware;
  }

  private Collection<Middleware> concatMiddleware(Collection<Middleware> first, Collection<Middleware> second) {
    ImmutableList.Builder<Middleware> mw = ImmutableList.builder();
    return mw.addAll(first).addAll(second).build();
  }

}
