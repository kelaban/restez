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

public class Application extends HttpServlet {
  private Router router;

  public Application(Router router) {
    this.router = router;
  }
  
  @Override
  protected void service(HttpServletRequest httpReq, HttpServletResponse httpRes) throws ServletException, IOException {
    try {
      Optional<RouteMatch> route = router.getRouteMatches()
          .stream()
          .filter(rm -> rm.matches(HttpMethod.valueOf(httpReq.getMethod().toLowerCase()), httpReq.getRequestURI())).findFirst();
      
      if (route.isPresent()) {
        Request request = new Request(httpReq, route.get().parsePathParam(httpReq.getRequestURI()));
        Response response = new Response(httpRes);
        
        try {
          handleRouteMatch(request, response, route.get().getHandler(), route.get().getMiddleware().iterator());
        } catch (Exception e) {
          httpRes.setStatus(500);
        }
      } else {
        httpRes.setStatus(404);
        handleTopLevelMiddlewaresOnly(new Request(httpReq, null), new Response(httpRes), router.getMiddleware().iterator());
      }
    } catch(Exception e) {
      httpRes.setStatus(500);
    }
  }
  
  private void handleTopLevelMiddlewaresOnly(Request request, Response response, Iterator<Middleware> middlewares) throws Exception {
    if (middlewares.hasNext()) {
      middlewares.next().handle(request, response, (req, res) -> {
        handleTopLevelMiddlewaresOnly(req, res, middlewares);
      });
    }
  }
  
  private void handleRouteMatch(Request request, Response response, Handler h, Iterator<Middleware> middlewares) throws Exception {
    if (!middlewares.hasNext()) {
      h.handle(request, response);
    } else {
      middlewares.next().handle(request, response, (req, res) -> {
        handleRouteMatch(req, res, h, middlewares);
      });
    }
  }

  
}
