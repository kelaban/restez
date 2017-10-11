package com.k317h.restez.io;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.k317h.restez.errors.BadRequestException;

public class QueryParam {
  private final String name;
  private final Optional<String[]> values;

  public QueryParam(String name, Optional<String[]> values) {
    this.name = name;
    this.values = values;
  }

  private static Function<String[], Optional<String>> PARAM_TO_FIRST_VALUE = params -> {
    if (params.length > 0) {
      return Optional.of(params[0]);
    } else {
      return Optional.empty();
    }
  };

  public Optional<String> asString() {
    return values.flatMap(PARAM_TO_FIRST_VALUE);
  }
  public Optional<List<String>> asListOfStrings() {
    return values.map(Arrays::asList);
  }
  
  

  public Optional<Integer> asInteger() {
    return asMappedValue(Integer::parseInt);
  }
  public Optional<List<Integer>> asListOfIntegers() {
    return asListOfMappedValues(Integer::parseInt);
  }
  
  
  
  public Optional<Long> asLong() {
    return asMappedValue(Long::parseLong);
  }
  public Optional<List<Long>> asListOfLongs() {
    return asListOfMappedValues(Long::parseLong);
  }
  
  

  public Optional<Float> asFloat() {
    return asMappedValue(Float::parseFloat);
  }
  public Optional<List<Float>> asListOfFloats() {
    return asListOfMappedValues(Float::parseFloat);
  }
  
  

  public Optional<Double> asDouble() {
    return asMappedValue(Double::parseDouble);
  }
  public Optional<List<Double>> asListOfDoubles() {
    return asListOfMappedValues(Double::parseDouble);
  }
  
 

  public Optional<Boolean> asBoolean() {
    return asMappedValue(Boolean::parseBoolean);
  }
  public Optional<List<Boolean>> asListOfBooleans() {
    return asListOfMappedValues(Boolean::parseBoolean);
  }
  
  

  public <T> Optional<T> asMappedValue(Function<String, T> mapper) {
    try {
      return asString().map(mapper);
    } catch(Exception e) {
      throw new BadRequestException("Unable to parse query param '"+name+"':'"+ asString().get() + "' into expected type", e);
    }
    
  }
  public <T> Optional<List<T>> asListOfMappedValues(Function<String, T> mapper) {
    try {
      return asListOfStrings().map(ps -> ps.stream().map(mapper).collect(Collectors.toList()));
    } catch(Exception e) {
      throw new BadRequestException("Unable to parse query param '"+name+"':'"+ asString().get() + "' into expected type", e);
    }
  }

}
