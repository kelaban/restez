package com.k317h.restez;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.junit.Assert;
import org.junit.Test;

import com.k317h.restez.route.RouteMatch;
import com.k317h.restez.route.RouteSpec;
import com.k317h.restez.route.RegexPathMatcher.PathParams;
import com.k317h.restez.util.MockHttpServletRequest;

public class RouteMatchTest {
  private RouteSpec s(String path, HttpMethod verb) {
    return RouteSpec.builder().path(path).verb(verb).build();
  }

  @Test
  public void testParamNamesMustBeUnique() throws Exception {
    assertThrows("Should throw illegal argument", IllegalArgumentException.class, () -> {
      new RouteMatch(s("/:a/:a", HttpMethod.get), null, null);
    });
  }
  
  @Test
  public void testCannotHaveTwoSplats() throws Exception {
    assertThrows("Should throw illegal argument", IllegalArgumentException.class, () -> {
      new RouteMatch(s("/*/*", HttpMethod.get), null, null);
    });
  }
  
  @Test
  public void testCannotHaveAnythingAfterSplat() throws Exception {
    assertThrows("Should throw illegal argument", IllegalArgumentException.class, () -> {
      new RouteMatch(s("/*/a", HttpMethod.get), null, null);
    });
  }
  
  private HttpServletRequest req(HttpMethod method, String path) {
    return new MockHttpServletRequest() {
      @Override
      public String getRequestURI() {
        return path;
      }
      
      @Override
      public String getMethod() {
        return method.toString().toUpperCase();
      }
    };
  }
  
  @Test
  public void testBasicSplatMatch() throws Exception {
    RouteMatch rm = new RouteMatch(s("/a/b/c/*", HttpMethod.get), null, null);
    Assert.assertTrue("Things after splat should match", rm.matches(req(HttpMethod.get, "/a/b/c/$/^/*/\\")));
    Assert.assertFalse("Prefix should not match", rm.matches(req(HttpMethod.get, "/d/e/f/a/b/c/d")));
  }
  
  @Test
  public void testNamedParmsMatch() throws Exception {
    RouteMatch rm = new RouteMatch(s("/:a/:b/:c", HttpMethod.get), null, null);
    Assert.assertTrue("Name Parmas should match", rm.matches(req(HttpMethod.get, "/1/2/3")));
    Assert.assertFalse("Extra params should not match", rm.matches(req(HttpMethod.get, "/1/2/3/4")));
  }

  
  @Test
  public void testEmptyPath() throws Exception {
    RouteMatch rm = new RouteMatch(s("", HttpMethod.get), null, null);
    Assert.assertTrue("Empty path matches root", rm.matches(req(HttpMethod.get, "/")));
    Assert.assertFalse(rm.matches(req(HttpMethod.get, "/a")));
  }
  
  @Test
  public void testRootPath() throws Exception {
    RouteMatch rm = new RouteMatch(s("/", HttpMethod.get), null, null);
    Assert.assertTrue(rm.matches(req(HttpMethod.get, "/")));
    Assert.assertFalse(rm.matches(req(HttpMethod.get, "/a")));
  }
  
  @Test
  public void testRootSplat() throws Exception {
    RouteMatch rm = new RouteMatch(s("/*", HttpMethod.get), null, null);
    Assert.assertTrue(rm.matches(req(HttpMethod.get, "/")));
    Assert.assertTrue(rm.matches(req(HttpMethod.get, "/a")));
  }
  
  @Test
  public void testComplexPath() throws Exception {
    RouteMatch rm = new RouteMatch(s("/:a/:b/:c/end/*", HttpMethod.get), null, null);
    PathParams pp = rm.parsePathParam("/1/2/3/end/foo/bar/baz");
    
    Assert.assertEquals(3, pp.namedParams.size());
    Assert.assertEquals("1", pp.namedParams.get("a"));
    Assert.assertEquals("2", pp.namedParams.get("b"));
    Assert.assertEquals("3", pp.namedParams.get("c"));
    
    Assert.assertEquals(3, pp.splatParams.size());
    Assert.assertEquals("foo", pp.splatParams.get(0));
    Assert.assertEquals("bar", pp.splatParams.get(1));
    Assert.assertEquals("baz", pp.splatParams.get(2));
    
    Assert.assertEquals("foo/bar/baz", pp.rawSplat);
  }
  
  @Test
  public void testHttpMethod() throws Exception {
    for(HttpMethod m: HttpMethod.values()) {
      RouteMatch rm = new RouteMatch(s("/", m), null, null);
      for(HttpMethod m2: HttpMethod.values()) {
        if(m.equals(m2)) {
          Assert.assertTrue(rm.matches(req(m2, "/")));
        } else {
          Assert.assertFalse(rm.matches(req(m2, "/")));
        }
      }
    }
  }


  private void assertThrows(
    String message,
    Class<? extends Exception> expectedException, 
    Runnable fn
  ) {
    try {
      fn.run();
    } catch (Throwable e) {
      Assert.assertTrue(message, expectedException.isInstance(e));
      return;
    }
    
    Assert.fail(message);
  }
}
