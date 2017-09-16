package com.k317h.restez.io;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;



public class QueryParamTest {

  
  @Test
  public void testAsInteger() {
    Assert.assertEquals(Integer.valueOf(1), makeParams("1").asInteger().get());
  }
  
  @Test
  public void testAsListOfIntegers() {
    Assert.assertEquals(Arrays.asList(1,2,3), makeParams("1", "2", "3").asListOfIntegers().get());
  }
  
  @Test
  public void testAsLong() {
    Assert.assertEquals(Long.valueOf(1), makeParams("1").asLong().get());
  }
  
  @Test
  public void testAsListOfLongs() {
    Assert.assertEquals(Arrays.asList(1L,2L,3L), makeParams("1", "2", "3").asListOfLongs().get());
  }
  
  
  @Test
  public void testAsDouble() {
    Assert.assertEquals(Double.valueOf(1.0), makeParams("1.0").asDouble().get());
  }
  
  @Test
  public void testAsListOfDoubles() {
    Assert.assertEquals(Arrays.asList(1.0,1.1,1.2), makeParams("1.0","1.1","1.2").asListOfDoubles().get());
  }
  
  @Test
  public void testAsFloat() {
    Assert.assertEquals(Double.valueOf(1.0), makeParams("1.0").asDouble().get());
  }
  
  @Test
  public void testAsListOfFloats() {
    Assert.assertEquals(Arrays.asList(1.0f,1.1f,1.2f), makeParams("1.0","1.1","1.2").asListOfFloats().get());
  }
  
  @Test
  public void testAsBoolean() {
    Assert.assertEquals(Boolean.valueOf(true), makeParams("true").asBoolean().get());
  }
  
  @Test
  public void testAsListOfBooleans() {
    Assert.assertEquals(Arrays.asList(true, false, false, false), makeParams("true", "false", "yes", "no").asListOfBooleans().get());
  }
  
  @Test
  public void testAsMappedValueDate() {
    Instant now = Instant.now();
    Assert.assertEquals(now, makeParams(now.toString()).asMappedValue(Instant::parse).get());
  }
  
  
  
  private QueryParam makeParams(String... params) {
    return new QueryParam(Optional.ofNullable(params));
  }
}
