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


# Serializers

Register a serializer for a mime type. When content type matches the serializers content type the serializer will be used.
When sending a response use `Response.send(Object)`, if `Response.send(String)` is used, the default serializer will be used to turn
the string into a UTF-8 encoded byte array. By default if there is no content type mapping for `Response.send(Object)` serialization
will fail with an `IllegalArgumentException`, this can be disable my setting `new Serializers(false)`. Disabling this behavior will
cause the serializer to fallback to the default serializer.

This example assumes gson is installed as a dependency.
`registerJsonSerializer(ser)` is syntactic sugar for `registerSerializer("application/json", ser)`
Note, when sending a response `response.json(obj)` is short hand for `response.contentType("application/json").send(obj)`.

```
Router router = new Router();

router.get("/users/:userId", (req, res) -> {
  res.json(Users.getById(req.params("userId")))
})

Gson gson = new GsonBuilder().create();

Serializers serializers = new Serializers()
  .registerJsonSerializer(obj -> gson.toJson(obj).getBytes());

Application app = new Application(router, serializers)

startServer(app)
```
