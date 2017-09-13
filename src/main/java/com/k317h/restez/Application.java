package com.k317h.restez;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.k317h.restez.io.Request;
import com.k317h.restez.io.Response;
import com.k317h.restez.route.RouteMatch;

public class Application extends HttpServlet {
  private final Router router;
  private final Serializers serializers;

  public Application(Router router) {
    this(router, new Serializers(true));
  }
  
  public Application(Router router, Serializers serializers) {
    this.router = router;
    this.serializers = serializers;
  }
  
  @Override
  protected void service(HttpServletRequest httpReq, HttpServletResponse httpRes) throws ServletException, IOException {
    try {
      Optional<RouteMatch> route = router.match(httpReq);
      
      if (route.isPresent()) {
        Request request = new Request(httpReq, route.get().parsePathParam(httpReq.getRequestURI()));
        Response response = new Response(httpRes, serializers);
        
        try {
          handleRouteMatch(request, response, route.get().getHandler(), route.get().getMiddleware().iterator());
        } catch (Exception e) {
          httpRes.setStatus(500);
        }
      } else {
        httpRes.setStatus(404);
        handleRouteMatch(new Request(httpReq, null), new Response(httpRes, serializers), null, router.getMiddleware().iterator());
      }
    } catch(Exception e) {
      httpRes.setStatus(500);
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
