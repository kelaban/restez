package com.k317h.restez;

import org.junit.Assert;
import org.junit.Test;

import com.k317h.restez.RouteMatch.PathParams;

public class RouteMatchTest {

  @Test
  public void testParamNamesMustBeUnique() throws Exception {
    assertThrows("Should throw illegal argument", IllegalArgumentException.class, () -> {
      new RouteMatch("/:a/:a", HttpMethod.get, null, null);
    });
  }
  
  @Test
  public void testCannotHaveTwoSplats() throws Exception {
    assertThrows("Should throw illegal argument", IllegalArgumentException.class, () -> {
      new RouteMatch("/*/*", HttpMethod.get, null, null);
    });
  }
  
  @Test
  public void testCannotHaveAnythingAfterSplat() throws Exception {
    assertThrows("Should throw illegal argument", IllegalArgumentException.class, () -> {
      new RouteMatch("/*/a", HttpMethod.get, null, null);
    });
  }
  
  @Test
  public void testBasicSplatMatch() throws Exception {
    RouteMatch rm = new RouteMatch("/a/b/c/*", HttpMethod.get, null, null);
    Assert.assertTrue("Things after splat should match", rm.matches(HttpMethod.get, "/a/b/c/$/^/*/\\"));
    Assert.assertFalse("Prefix should not match", rm.matches(HttpMethod.get, "/d/e/f/a/b/c/d"));
  }
  
  @Test
  public void testNamedParmsMatch() throws Exception {
    RouteMatch rm = new RouteMatch("/:a/:b/:c", HttpMethod.get, null, null);
    Assert.assertTrue("Name Parmas should match", rm.matches(HttpMethod.get, "/1/2/3"));
    Assert.assertFalse("Extra params should not match", rm.matches(HttpMethod.get, "/1/2/3/4"));
  }

  
  @Test
  public void testEmptyPath() throws Exception {
    RouteMatch rm = new RouteMatch("", HttpMethod.get, null, null);
    Assert.assertTrue("Empty path matches root", rm.matches(HttpMethod.get, "/"));
    Assert.assertFalse(rm.matches(HttpMethod.get, "/a"));
  }
  
  @Test
  public void testRootPath() throws Exception {
    RouteMatch rm = new RouteMatch("/", HttpMethod.get, null, null);
    Assert.assertTrue(rm.matches(HttpMethod.get, "/"));
    Assert.assertFalse(rm.matches(HttpMethod.get, "/a"));
  }
  
  @Test
  public void testRootSplat() throws Exception {
    RouteMatch rm = new RouteMatch("/*", HttpMethod.get, null, null);
    Assert.assertTrue(rm.matches(HttpMethod.get, "/"));
    Assert.assertTrue(rm.matches(HttpMethod.get, "/a"));
  }
  
  @Test
  public void testComplexPath() throws Exception {
    RouteMatch rm = new RouteMatch("/:a/:b/:c/end/*", HttpMethod.get, null, null);
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
      RouteMatch rm = new RouteMatch("/", m, null, null);
      for(HttpMethod m2: HttpMethod.values()) {
        if(m.equals(m2)) {
          Assert.assertTrue(rm.matches(m2, "/"));
        } else {
          Assert.assertFalse(rm.matches(m2, "/"));
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
