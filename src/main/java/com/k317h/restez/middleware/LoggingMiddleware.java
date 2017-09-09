package com.k317h.restez.middleware;

import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.k317h.restez.Handler;
import com.k317h.restez.Middleware;
import com.k317h.restez.io.Request;
import com.k317h.restez.io.Response;

/**
 * This is a Logging middleware, it should be the first middleware in your stack because it logs AFTER the request has been sent
 * Any middleware that calculates content-length, does gzip or does error handling should come after this middleware
 *
 */
public class LoggingMiddleware implements Middleware {
  public static String SHORT_FORMAT = ":remote-addr :remote-user :method :url :http-version :status :res[content-length] - :response-time ms";
  public static String COMMON_FORMAT = ":remote-addr - :remote-user [:date] \":method :url :http-version\" :status :res[content-length]";
  public static String COMBINED_FORMAT = ":remote-addr - :remote-user [:date] \":method :url :http-version\" :status :res[content-length] \":referrer\" \":user-agent\"";
  
  
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  private static Pattern FORMAT_PATTERN = Pattern.compile(":([-\\w]{2,})(?:\\[([^\\]]+)\\])?");
  
  private final String formatStr;
  
  //protected getFormatters for extensibility
  private Map<String, LogTokenFormatter> formatters = getFormatters();
  
  public LoggingMiddleware() {
    this(SHORT_FORMAT);
  }
  
  public LoggingMiddleware(String formatStr) {
    assertFormatStrIsLegit(formatStr);
    
    this.formatStr = formatStr;
  }
  
  
  private void assertFormatStrIsLegit(String test) {
    Matcher m = FORMAT_PATTERN.matcher(test);
    while(m.find()) {
      String fn = m.group(1);
      if(!formatters.containsKey(fn)) {
        throw new IllegalArgumentException(String.format("'%s' is not a supported log format", m.group(0)));
      }
    }
  }

  @FunctionalInterface
  protected interface LogTokenFormatter {
    public String format(Request req, Response res, String arg);
  }

  @Override
  public void handle(Request req, Response res, Handler next) throws Exception {
    long startTime = System.currentTimeMillis();
    req.rawRequest().setAttribute("startTime", startTime);
    
    //Forward request on
    next.handle(req, res);
    //
    
    Matcher m = FORMAT_PATTERN.matcher(formatStr);
    StringBuffer sb = new StringBuffer();
    
    while(m.find()) {
      String fn = m.group(1);
      String arg = m.groupCount() > 1 ? m.group(2) : null;
      
      String appendVal = formatters.get(fn).format(req, res, arg);
      m.appendReplacement(sb, appendVal == null ? "-" : Matcher.quoteReplacement(appendVal));
    }
    
    m.appendTail(sb);
    
    log.info(sb.toString());
  }
  
  protected Map<String, LogTokenFormatter> getFormatters() {
    Map<String, LogTokenFormatter> m = new HashMap<>(); 
    m.put("remote-addr", this::remoteAddress); 
    m.put("remote-user", this::remoteUser);
    m.put("method", this::method);
    m.put("url", this::url);
    m.put("http-version", this::httpVersion);
    m.put("scheme", this::scheme);
    m.put("status", this::status);
    m.put("res", this::respHeader);
    m.put("req", this::reqHeader);
    m.put("response-time", this::respTime);
    m.put("date", this::date);
    m.put("referrer", this::referrer);
    m.put("user-agent", this::userAgent);
    
    return Collections.unmodifiableMap(m);
  }
  
  protected String remoteAddress(Request req, Response res, String arg) {
    return req.rawRequest().getRemoteAddr();
  }
  
  protected String remoteUser(Request req, Response res, String arg) {
    return req.rawRequest().getRemoteUser();
  }
  
  protected String httpVersion(Request req, Response res, String arg) {
    return req.rawRequest().getProtocol();
  }
  
  protected String scheme(Request req, Response res, String arg) {
    return req.rawRequest().getScheme();
  }
  
  protected String url(Request req, Response res, String arg) {
    return req.path();
  }
  
  protected String status(Request req, Response res, String arg) {
    return Integer.toString(res.rawResponse().getStatus());
  }
  
  protected String method(Request req, Response res, String arg) {
    return req.method().toString().toUpperCase();
  }
  
  protected String respHeader(Request req, Response res, String arg) {
    return res.rawResponse().getHeader(arg);
  }
  
  protected String reqHeader(Request req, Response res, String arg) {
    return req.rawRequest().getHeader(arg);
  }
  
  protected String respTime(Request req, Response res, String arg) {
    return Long.toString(System.currentTimeMillis() - (long)req.rawRequest().getAttribute("startTime"));
  }
  
  //TODO more date formats
  protected String date(Request req, Response res, String arg) {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss+0000");
    df.setTimeZone(tz);
    return df.format(new Date());
  }
  
  protected String referrer(Request req, Response res, String arg) {
    String ref = req.rawRequest().getHeader("referer");
    return ref != null ? ref : req.rawRequest().getHeader("referrer");
  }
  
  protected String userAgent(Request req, Response res, String arg) {
    return req.rawRequest().getHeader("user-agent");
  }
}
