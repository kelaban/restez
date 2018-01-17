package com.k317h.restez;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.k317h.restez.errors.SerializationException;
import com.k317h.restez.io.Request;
import com.k317h.restez.io.Response;
import com.k317h.restez.middleware.BufferedSend;
import com.k317h.restez.middleware.GZIPMiddleware;
import com.k317h.restez.middleware.LoggingMiddleware;
import com.k317h.restez.serialization.Deserializers;
import com.k317h.restez.serialization.Deserializers.Deserializer;
import com.k317h.restez.serialization.Serializers;

import static org.junit.Assert.assertEquals;


public class AppTest {

  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  private int port = 8080;
  private Server server = null;
  private CloseableHttpClient httpclient;
  private Router app;
  private Serializers serializers;
  private Deserializers deserializers;

  private void initServer() throws Exception {
    server = new Server(port);

    ServletHandler handler = new ServletHandler();
    server.setHandler(handler);

    ServletHolder s = new ServletHolder();
    s.setServlet(Application.create(app).withSerializers(serializers).withDeserializers(deserializers).build());

    handler.addServletWithMapping(s, "/*");

    server.start();
  }
  
  @BeforeClass
  public static void beforeAllYall() {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
  }
  
  @Before
  public void before() throws Exception {
    httpclient = HttpClients.createDefault();
    serializers = new Serializers(true);
    deserializers = new Deserializers();
    
    app = new Router();
    app.use(new LoggingMiddleware(), new BufferedSend());
  }
  
  @After
  public void after() throws Exception {
    if(null != server && server.isStarted()) {
      server.stop();
    }
    
    httpclient.close();
  }

  private CloseableHttpResponse request(HttpRequestBase base, String path) throws Exception {
    base.setURI(new URIBuilder()
        .setPath(path)
        .setScheme("http")
        .setPort(port)
        .setHost("localhost")
        .build());

    log.info("Request URL: {}", base.getURI().toString());
    CloseableHttpResponse res = httpclient.execute(base);
    log.info("Received response: {}", res.toString());

    return res;
  }
  
  private CloseableHttpResponse get(String path) throws Exception {
    return request(new HttpGet(), path);
  }
  
  private CloseableHttpResponse head(String path) throws Exception {
    return request(new HttpHead(), path);
  }
  
  private CloseableHttpResponse post(String path, String body) throws Exception {
    HttpPost post = new HttpPost();
    post.setHeader("Content-Type", "application/json");
    post.setEntity(new StringEntity(body));
    return request(post, path);
  }
  
  private void assertGet(String path) throws Exception{
    assertResponse(get(path), path, path.length());
  }
  
  private void assertHead(String path) throws Exception{
    assertResponse(head(path), null, path.length());
  }
  
  private void assertResponse(CloseableHttpResponse resp, String body, int contentLength) throws Exception {
    assertResponse(resp, body, contentLength, HttpStatus.SC_OK);
  }
  
  private void assertResponse(CloseableHttpResponse resp, String body, int contentLength, int status) throws Exception {
    assertEquals(status, resp.getStatusLine().getStatusCode());
    assertEquals(""+contentLength, resp.getFirstHeader("content-length").getValue());
    
    HttpEntity entity = resp.getEntity(); 
    String actualBody = null != entity ? IOUtils.toString(entity.getContent(), Charset.forName("UTF-8")) : null;   
    assertEquals(body, actualBody);
  }
  
  private void echoPath(Request req, Response res) throws IOException {
    res.contentType("text/plain");
    res.send(req.path());
  }
  
  @Test
  public void testPathParams() throws Exception {
    app.get("/a/b/c", this::echoPath);

    app.get("/:a/:b/:c", this::echoPath);
    
    Router r = new Router();
    r.get("/:d", this::echoPath);
    
    app.use("/a/b/c", r);
    
    initServer();
    
    assertGet("/a/b/c");
    assertGet("/a/b/cc");
    assertGet("/a/b/c/param");
    
    assertHead("/a/b/c");
  }
  
  @Test
  public void testPathSplat() throws Exception {    
    app.get("/a/b/c.mp3", this::echoPath);
    app.get("/:a/*", this::echoPath);
    
    initServer();
    
    assertGet("/a/b/c.mp3");
    assertGet("/a/b/cc");
    assertGet("/a");
  }
  
  @Test
  public void testPost() throws Exception {
    String expectedResponse = "OK!";
    
    app.post("/p0$t/:param", (req, res) -> {
      assertEquals("1", req.params("param"));
      assertEquals("Hello World!", req.body());
      assertEquals("Hello World!", req.body());
      res.send(expectedResponse);
    });
    
    initServer();
    
    assertResponse(post("/p0$t/1", "Hello World!"), expectedResponse, expectedResponse.length());
  }
  
  @Test
  public void test404() throws Exception {
    app.post("/post/:param", (req, res) -> {
      Assert.fail("should not be called");
    });
    
    initServer();
    
    assertResponse(get("/post/1"), "", 0, HttpStatus.SC_NOT_FOUND);
  }

  @Test
  public void testDecodesParams() throws Exception {
    String param = "hello world";

    app.get("/hello/:param", (req, res) -> {
      assertEquals(param, req.params("param"));
      res.status(HttpStatus.SC_OK);
    });

    initServer();

    assertResponse(get("/hello/hello world"), "", 0, HttpStatus.SC_OK);
  }

  @Test
  public void testMatchesWithEscapedPath() throws Exception {
    app.get("/hello world/:param", (req, res) -> {
      res.status(HttpStatus.SC_OK);
    });

    initServer();

    assertResponse(get("/hello world/1"), "", 0, HttpStatus.SC_OK);
  }

  ///////////////////////////////////
  ///GZIP TESTS/////////////////////
  
  @Test
  public void testGZIP() throws Exception {
    String expectedResponse = "Hello World!";
    app.post("/gzip", (req, res) -> {
      String body = req.body();
      Assert.assertEquals(expectedResponse, body);
      res.send(body);
    }, new GZIPMiddleware());

    initServer();

    assertResponse(post("/gzip", expectedResponse), expectedResponse, expectedResponse.length());

    HttpPost post = new HttpPost();
    GzipCompressingEntity gzipbody = new GzipCompressingEntity(new StringEntity("Hello World!"));
    post.setEntity(gzipbody);
    post.addHeader("Accept-Encoding", "gzip");

    CloseableHttpResponse resp = request(post, "/gzip");

    Assert.assertEquals(HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());
    Assert.assertEquals("Hello World!", IOUtils.toString(resp.getEntity().getContent(), Charset.forName("UTF-8")));
  }
  
  
  ///////////////////////
  ///Test Serializers///
  
  @Test
  public void testJsonSerialization() throws Exception {
    Gson gson = new GsonBuilder().create();
    serializers.registerJsonSerializer(obj -> gson.toJson(obj).getBytes());
    
    app.get("/foo", (req, resp) -> {
      Map<String, String> m = new HashMap<>();
      m.put("foo", "bar");
      resp.json(m);
    });
    
    initServer();
    
    String expectedResponse = "{\"foo\":\"bar\"}"; 
    
    assertResponse(get("/foo"), expectedResponse, expectedResponse.length());
  }
  
  ////////////////////////
  ///Test Deserializers//
  
  private static class JsonDeserializationBody {
    String body;
  }
  
  @Test
  public void testJsonDeserialization() throws Exception {
    Gson gson = new GsonBuilder().create();
    
    deserializers.registerJsonDeserializer(new Deserializer(){
      @Override
      public <T> T deserialize(byte[] o, Class<T> clazz) throws SerializationException {
        try {
          return gson.fromJson(new String(o), clazz);
        } catch(JsonSyntaxException e) {
          throw new SerializationException("Unable to parse json", e);
        } 
      }
    });
    
    String expectedResponse = "OK!";
    
    app.post("/json", (req, res) -> {
      assertEquals("Hello World!", req.body(JsonDeserializationBody.class).body);
      res.send(expectedResponse);
    });
    
    initServer();
    
    assertResponse(post("/json", "{ \"body\": \"Hello World!\"}"), expectedResponse, expectedResponse.length());
  }
  
  @Test
  public void testJsonDeserializationBadSyntax() throws Exception {
    Gson gson = new GsonBuilder().create();
    
    deserializers.registerJsonDeserializer(new Deserializer(){
      @Override
      public <T> T deserialize(byte[] o, Class<T> clazz) throws SerializationException {
        try {
          return gson.fromJson(new String(o), clazz);
        } catch(JsonSyntaxException e) {
          throw new SerializationException("Unable to parse json", e);
        }
      }
    });
    
    app.post("/json", (req, res) -> {
      req.body(JsonDeserializationBody.class);
      Assert.fail("Deserializer should throw exception");
    });
    
    initServer();
    
    assertResponse(post("/json", "{ \"body\": \"Hello World!"), "", 0, HttpServletResponse.SC_BAD_REQUEST);
  }
  
}
