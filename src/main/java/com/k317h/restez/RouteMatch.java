package com.k317h.restez;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import util.PathUtils;

public class RouteMatch {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  private String path;
  private HttpMethod verb;
  private Collection<Middleware> middleware;
  private Handler handler;
  private RegexPathMatcher pathMatcher;

  public RouteMatch(String path, HttpMethod verb, Handler handler, Collection<Middleware> middleware) {
    this.path = path;
    this.verb = verb;
    this.middleware = middleware;
    this.handler = handler;
    this.pathMatcher = parsePathToRegex(this.path);
  }
  
  public class PathParams {
    public List<String> splatParams = null;
    public String rawSplat = null;
    public Map<String, String> namedParams = null;
  }
  
  private class RegexPathMatcher {
    private Pattern pattern;
    private Collection<String> matchGroups;
    private boolean hasSplat;

    public RegexPathMatcher(Pattern patt, Collection<String> matchGroups, boolean hasSplat) {
      this.pattern = patt;
      this.matchGroups = matchGroups;
      this.hasSplat = hasSplat;
    }
    
    public boolean matches(String test) {
      boolean m = pattern.matcher(test).matches();
      log.info("testing {} against {} : matches[{}]", test, pattern, m);
      return m;
    }
    
    public PathParams parsePathParams(String test) {
      PathParams pp = new PathParams();
      Matcher m = pattern.matcher(test);
      
      m.find();

      if (hasSplat) {
        String splat = m.group("splat");

        if (null != splat) {
          pp.splatParams = Arrays.asList(splat.split("/"));
          pp.rawSplat = splat;
        }
      }

      ImmutableMap.Builder<String, String> namedParams = ImmutableMap.builder();
      for (String mg : matchGroups) {
        namedParams.put(mg, m.group(mg));
      }

      pp.namedParams = namedParams.build();

      return pp;
    }
  }
  
  private RegexPathMatcher parsePathToRegex(String path) {
    String[] pathParts = PathUtils.trimSlashes(path).split("/");
    
    Pattern patt;
    boolean hasSplat = false;
    Set<String> matchGroups = new HashSet<String>();

    if (pathParts.length == 0) {
      patt = Pattern.compile("^/?$");
    } else {
      StringBuilder regexp = new StringBuilder("^");
  
      for (String p : pathParts) {
        if(hasSplat) {
          throw new IllegalArgumentException("A nothing allowed after splat: " + path);
        }
        
        regexp.append("/");
  
        if (p.startsWith(":")) {
          String paramName = p.substring(1);
          
          if(!matchGroups.add(paramName)) {
            throw new IllegalArgumentException(path + " contains two params named '" + paramName + "'");
          }

          regexp.append("(?<").append(Pattern.quote(paramName)).append(">").append("[^/]+)");
        } else if (p.equals("*")) {
          hasSplat = true;
          regexp.append("?(?<splat>.*)");
        } else {
          regexp.append(Pattern.quote(p));
        }
      }
  
      regexp.append("/?$");
  
      patt = Pattern.compile(regexp.toString());
    }
    
    return new RegexPathMatcher(patt, matchGroups, hasSplat);
  }

  public boolean matches(HttpMethod method, String path) {
    return method.equals(verb) && pathMatcher.matches(path);
  }

  public PathParams parsePathParam(String path) {
    return pathMatcher.parsePathParams(path);
  }

  public String getPath() {
    return path;
  }

  public HttpMethod getMethod() {
    return verb;
  }

  public Collection<Middleware> getMiddleware() {
    return middleware;
  }

  public Handler getHandler() {
    return handler;
  }
}
