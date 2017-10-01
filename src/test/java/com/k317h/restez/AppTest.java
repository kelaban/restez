package com.k317h.restez;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
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
import com.k317h.restez.io.Request;
import com.k317h.restez.io.Response;
import com.k317h.restez.middleware.BufferedSend;
import com.k317h.restez.middleware.GZIPMiddleware;
import com.k317h.restez.middleware.LoggingMiddleware;
import com.k317h.restez.serialization.Deserializers;
import com.k317h.restez.serialization.Deserializers.Deserializer;
import com.k317h.restez.serialization.Serializers;

import errors.SerializationException;



public class AppTest {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  private int port = 8080;
  private Server server = null;
  private CloseableHttpClient httpclient;
  private Router app;
  private Serializers serializers;
  private Deserializers deserializers;
  
  
  public void initServer() throws Exception {
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
  
  private CloseableHttpResponse get(String path) throws Exception {
    CloseableHttpResponse res = httpclient.execute(new HttpGet(u(path)));
    log.info("{}", res.toString());
    return res;
  }
  
  private CloseableHttpResponse head(String path) throws Exception {
    CloseableHttpResponse res = httpclient.execute(new HttpHead(u(path)));
    log.info("{}", res.toString());
    return res;
  }
  
  private CloseableHttpResponse post(String path, String body) throws Exception {
    HttpPost post = new HttpPost(u(path));
    post.setEntity(new StringEntity(body));
    post.setHeader("Content-Type", "application/json");
    return httpclient.execute(post);
  }
  
  private String u(String path) {
    return "http://localhost:" + port + path;
  }
  
  private void assertGet(String path) throws Exception{
    assertResponse(get(path), path, path.length());
  }
  
  private void assertHead(String path) throws Exception{
    assertResponse(head(path), null, path.length());
  }
  
  private void assertResponse(CloseableHttpResponse resp, String body, int contentLength) throws Exception {
    assertResponse(resp, body, contentLength, 200);
  }
  
  private void assertResponse(CloseableHttpResponse resp, String body, int contentLength, int status) throws Exception {
    Assert.assertEquals(status, resp.getStatusLine().getStatusCode());
    Assert.assertEquals(""+contentLength, resp.getFirstHeader("content-length").getValue());
    
    HttpEntity entity = resp.getEntity(); 
    String actualBody = null != entity ? IOUtils.toString(entity.getContent(), Charset.forName("UTF-8")) : null;   
    Assert.assertEquals(body, actualBody);
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
      Assert.assertEquals("1", req.params("param"));
      Assert.assertEquals("Hello World!", req.body());
      Assert.assertEquals("Hello World!", req.body());
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
    
    assertResponse(get("/post/1"), "", 0, 404);
  }

  
  ///////////////////////////////////
  ///GZIP TESTS/////////////////////
  
  @Test
  public void testGZIP() throws Exception {
    String expectedResponse = "Hello World!";
    app.post("/gzip", (req, res) -> {
      String body = (String)req.body();
      Assert.assertEquals(expectedResponse, body);
      res.send(body);
    }, new GZIPMiddleware());
    
    initServer();
  
  
    assertResponse(post("/gzip", expectedResponse), expectedResponse, expectedResponse.length());
    
    HttpPost post = new HttpPost(u("/gzip"));
    GzipCompressingEntity gzipbody = new GzipCompressingEntity(new StringEntity("Hello World!")); 
    post.setEntity(gzipbody);
    post.addHeader("Accept-Encoding", "gzip");
    CloseableHttpResponse resp = httpclient.execute(post);
    
    Assert.assertEquals(200, resp.getStatusLine().getStatusCode());
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
      Assert.assertEquals("Hello World!", req.body(JsonDeserializationBody.class).body);
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
