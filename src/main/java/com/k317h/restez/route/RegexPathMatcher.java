package com.k317h.restez.route;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.PathUtils;

public class RegexPathMatcher {
  private static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Pattern pattern;
  private Collection<String> matchGroups;
  private boolean hasSplat;

  public class PathParams {
    public List<String> splatParams = null;
    public String rawSplat = null;
    public Map<String, String> namedParams = null;
  }

  public RegexPathMatcher(Pattern patt, Collection<String> matchGroups, boolean hasSplat) {
    this.pattern = patt;
    this.matchGroups = matchGroups;
    this.hasSplat = hasSplat;
  }

  public boolean matches(String test) {
    boolean m = pattern.matcher(test).matches();
    log.debug("testing {} against {} : matches[{}]", test, pattern, m);
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

    Map<String, String> namedParams = new HashMap<>();
    for (String mg : matchGroups) {
      namedParams.put(mg, m.group(mg));
    }

    pp.namedParams = Collections.unmodifiableMap(namedParams);

    return pp;
  }

  public static RegexPathMatcher fromPath(String path) {
    String[] pathParts = PathUtils.trimSlashes(path).split("/");

    Pattern patt;
    boolean hasSplat = false;
    Set<String> matchGroups = new HashSet<String>();

    if (pathParts.length == 0) {
      patt = Pattern.compile("^/?$");
    } else {
      StringBuilder regexp = new StringBuilder("^");

      for (String p : pathParts) {
        if (hasSplat) {
          throw new IllegalArgumentException("A nothing allowed after splat: " + path);
        }

        regexp.append("/");

        if (p.startsWith(":")) {
          String paramName = p.substring(1);

          if (!matchGroups.add(paramName)) {
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
}
