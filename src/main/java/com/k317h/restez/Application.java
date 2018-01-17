package com.k317h.restez;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.k317h.restez.errors.RestezApiException;
import com.k317h.restez.io.Request;
import com.k317h.restez.io.Response;
import com.k317h.restez.route.RouteMatch;
import com.k317h.restez.serialization.Deserializers;
import com.k317h.restez.serialization.Serializers;

public class Application extends HttpServlet {

  private static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final Router router;
  private final Serializers serializers;
  private final Deserializers deserializers;

  public Application(Router router) {
    this(router, null, null);
  }
  
  private Application(Router router, Serializers serializers, Deserializers deserializers) {
    if(null == serializers) {
      serializers = new Serializers(true);
    }
    
    if(null == deserializers){
      deserializers =  new Deserializers();
    }
    
    this.router = router;
    this.serializers = serializers;
    this.deserializers = deserializers;
  }
  
  public static Builder create(Router router) {
    return new Builder(router);
  }
  
  public static class Builder {
    private final Router router;
    private Serializers serializers;
    private Deserializers deserializers;
    
    private Builder (Router router) {
      this.router = router;
    }
    
    public Builder withSerializers(Serializers s) {
      this.serializers = s;
      return this;
    }
    
    public Builder withDeserializers(Deserializers s) {
      this.deserializers = s;
      return this;
    }
    
    public Application build() {
      return new Application(router, serializers, deserializers);
    }
    
  }
  
  @Override
  protected void service(HttpServletRequest httpReq, HttpServletResponse httpRes) throws ServletException, IOException {
    try {
      Optional<RouteMatch> route = router.match(httpReq);
      
      if (route.isPresent()) {
        Request request = new Request(httpReq, route.get().parsePathParam(httpReq.getPathInfo()), deserializers);
        Response response = new Response(httpRes, serializers);
        
        try {
          handleRouteMatch(request, response, route.get().getHandler(), route.get().getMiddleware().iterator());
        } catch(RestezApiException e) {
          log.warn("Uncaught API Exception, it is recommended to setup a custom middleware to catch these", e);
          httpRes.setStatus(e.getCode());
        } catch (Exception e) {
          log.warn("Uncaught Exception, it is recommended to setup a custom middleware to catch these", e);
          httpRes.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
      } else {
        httpRes.setStatus(HttpServletResponse.SC_NOT_FOUND);
        handleRouteMatch(new Request(httpReq, null, null), new Response(httpRes, serializers), null, router.getMiddleware().iterator());
      }
    } catch(Exception e) {
      log.error("Unexpected top level exception", e);
      httpRes.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
 
  }
  
  
  private void handleRouteMatch(Request request, Response response, Handler h, Iterator<Middleware> middlewares) throws Exception {
    if(middlewares.hasNext()) {
      middlewares.next().handle(request, response, (req, res) -> {
        handleRouteMatch(req, res, h, middlewares);
      });
    } else if(null != h) {
      h.handle(request, response);
    }
  }

  
}
