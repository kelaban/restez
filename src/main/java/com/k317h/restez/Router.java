package com.k317h.restez;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.k317h.restez.route.RouteMatch;
import com.k317h.restez.route.RouteSpec;

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
      
      //TODO clone routespec
      route(RouteSpec.builder()
          .path(PathUtils.concatPath(path, rm.getSpec().getPath()))
          .verb(rm.getSpec().getVerb())
          .build(), 
          rm.getHandler(), 
          rm.getMiddleware());
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
    return route(RouteSpec.builder().path(path).verb(verb).build(), handler, mw);
  }
  
  public Router route(RouteSpec spec, Handler handler, Middleware... mw) {
    return route(spec, handler, Arrays.asList(mw));
  }

  private Router route(RouteSpec spec, Handler handler, Collection<Middleware> mw) {
    routeMatches.add(new RouteMatch(spec, handler, concatMiddleware(middleware, mw)));
    return this;
  }
 
  public Optional<RouteMatch> match(HttpServletRequest httpReq) {
    return getRouteMatches()
      .stream()
      .filter(rm -> rm.matches(httpReq))
      .findFirst();
  }
  
  public Collection<RouteMatch> getRouteMatches() {
    return routeMatches;
  }
  
  public Collection<Middleware> getMiddleware() {
    return middleware;
  }

  private Collection<Middleware> concatMiddleware(Collection<Middleware> first, Collection<Middleware> second) {
    List<Middleware> mws = new ArrayList<>();
    mws.addAll(first);
    mws.addAll(second);
    return Collections.unmodifiableCollection(mws);
  }

}
