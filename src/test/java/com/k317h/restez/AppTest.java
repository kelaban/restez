package com.k317h.restez;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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

import com.k317h.restez.io.Request;
import com.k317h.restez.io.Response;
import com.k317h.restez.middleware.BufferedSend;
import com.k317h.restez.middleware.GZIPMiddleware;
import com.k317h.restez.middleware.LoggingMiddleware;



public class AppTest {
  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  private int port = 8080;
  private Server server = null;
  private CloseableHttpClient httpclient;
  private Router app;
  
  
  public void initServer() throws Exception {
    server = new Server(8080);

    ServletHandler handler = new ServletHandler();
    server.setHandler(handler);

    ServletHolder s = new ServletHolder();
    s.setServlet(new Application(app));

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
    
    app = new Router();
    app.use(new LoggingMiddleware(), (req, res, next) -> {
      try {
        next.handle(req, res);
      } catch (Throwable t) {
        log.error("", t);
        res.status(500);
      }
    }, new BufferedSend());
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
  
  private CloseableHttpResponse post(String path, String body) throws Exception {
    HttpPost post = new HttpPost(u(path));
    post.setEntity(new StringEntity(body));
    return httpclient.execute(post);
  }
  
  private String u(String path) {
    return "http://localhost:" + port + path;
  }
  
  private void assertGet(String path) throws Exception{
    assertResponse(get(path), path);
  }
  
  private void assertResponse(CloseableHttpResponse resp, String body) throws Exception {
    assertResponse(resp, body, 200);
  }
  
  private void assertResponse(CloseableHttpResponse resp, String body, int status) throws Exception {
    Assert.assertEquals(status, resp.getStatusLine().getStatusCode());
    Assert.assertEquals(body, IOUtils.toString(resp.getEntity().getContent(), Charset.forName("UTF-8")));
  }
  
  private void echoPath(Request req, Response res) throws IOException {
    res.header("Content-Type", "text/plain");
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
    app.post("/p0$t/:param", (req, res) -> {
      Assert.assertEquals("1", req.params("param"));
      Assert.assertEquals("Hello World!", req.body());
      Assert.assertEquals("Hello World!", req.body());
      res.send("OK!");
    });
    
    initServer();
    
    assertResponse(post("/p0$t/1", "Hello World!"), "OK!");
  }
  
  @Test
  public void test404() throws Exception {
    app.post("/post/:param", (req, res) -> {
      Assert.fail("should not be called");
    });
    
    initServer();
    
    assertResponse(get("/post/1"), "", 404);
  }

  
  ///////////////////////////////////
  ///GZIP TESTS/////////////////////
  
  @Test
  public void testGZIP() throws Exception {
    app.post("/gzip", (req, res) -> {
      String body = (String)req.body();
      Assert.assertEquals("Hello World!", body);
      res.send(body);
    }, new GZIPMiddleware());
    
    initServer();
  
  
    assertResponse(post("/gzip", "Hello World!"), "Hello World!");
    
    HttpPost post = new HttpPost(u("/gzip"));
    GzipCompressingEntity gzipbody = new GzipCompressingEntity(new StringEntity("Hello World!")); 
    post.setEntity(gzipbody);
    post.addHeader("Accept-Encoding", "gzip");
    CloseableHttpResponse resp = httpclient.execute(post);
    
    Assert.assertEquals(200, resp.getStatusLine().getStatusCode());
    Assert.assertEquals("Hello World!", IOUtils.toString(resp.getEntity().getContent(), Charset.forName("UTF-8")));
  }
  
  
}
