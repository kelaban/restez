RESTEZ
======

Develop: [![Build Status](https://travis-ci.org/kelaban/restez.svg?branch=develop)](https://travis-ci.org/kelaban/restez)

An easier REST framework for Java. Inspired by ExpressJS for NodeJS.

Example setup with Jetty

```java
Router router = new Router();

// Setup some default middleware
router.use(
new LoggingMiddleware(),  //predefined
(req, res, next) -> {    // a lambda
  try {
    next.handle(req, res);
  } catch (Throwable t) {
    log.error("", t);
    res.status(500);
  }
},
new BufferedSend(),  //predefined
new GZIPMiddleware() //predefined
);

// Wire up some routes
router.post("/p0$t/:param", (req, res) -> {
  res.send("Body: " + req.body() + " Param: " + req.params("param"));
});

router.get("/:name", (req, res) -> {
  res.send("OK! " + req.params("name"));
});

// Create a nested router
Router subrouter = new Router();

subrouter.get("/bar", (req, res) -> {
  Assert.assertEquals(req.path(), "/foo/bar");
});

router.use("/foo", subrouter);

// Wire up jetty

Server = new Server(port);

ServletHandler handler = new ServletHandler();
server.setHandler(handler);

ServletHolder s = new ServletHolder();
s.setServlet(new Application(router));

handler.addServletWithMapping(s, "/*");

server.start();
```
