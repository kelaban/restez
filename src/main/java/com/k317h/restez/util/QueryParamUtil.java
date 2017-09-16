//package com.k317h.restez.util;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//import com.k317h.restez.io.Request;
//
//public class QueryParamUtil {
//  
//  private static Function<String[], Optional<String>> PARAM_TO_FIRST_VALUE = params -> {
//    if(params.length > 0) {
//      return Optional.of(params[0]);
//    } else {
//      return Optional.empty();
//    }
//  };
//  
//  public static Optional<String> asString(Request req, String paramName) {
//    return req.query(paramName).flatMap(PARAM_TO_FIRST_VALUE);
//  }
//  public static Optional<List<String>> asListOfStrings(Request req, String paramName) {
//    return req.query(paramName).map(params -> Arrays.asList(params));
//  }
//  
//  
//  
//  public static Optional<Integer> qpAsInteger(Request req, String paramName) {
//    return qpAsMappedValue(req, paramName, p -> Integer.parseInt(p));
//  }
//  public static Optional<List<Integer>> qpAsListOfIntegers(Request req, String paramName) {
//    return qpAsListOfMappedValue(req, paramName, p -> Integer.parseInt(p));
//  }
//  
// 
//  
//  public static Optional<Float> qpAsFloat(Request req, String paramName) {
//    return qpAsMappedValue(req, paramName, p -> Float.parseFloat(p));
//  }
//  public static Optional<List<Float>> qpAsListOfFloat(Request req, String paramName) {
//    return qpAsListOfMappedValue(req, paramName, p -> Float.parseFloat(p));
//  }
//  
//  
//  
//  public static Optional<Double> qpAsDouble(Request req, String paramName) {
//    return qpAsMappedValue(req, paramName, p -> Double.parseDouble(p));
//  }
//  public static Optional<List<Double>> qpAsListOfDouble(Request req, String paramName) {
//    return qpAsListOfMappedValue(req, paramName, p -> Double.parseDouble(p));
//  }
//  
//  
//  public static Optional<Boolean> qpAsBoolean(Request req, String paramName) {
//    return qpAsMappedValue(req, paramName, p -> Boolean.parseBoolean(p));
//  }
//  public static Optional<List<Boolean>> qpAsListOfBoolean(Request req, String paramName) {
//    return qpAsListOfMappedValue(req, paramName, p -> Boolean.parseBoolean(p));
//  }
//  
//  
//  public static <T> Optional<T> qpAsMappedValue(Request req, String paramName, Function<String, T> mapper) {
//    return asString(req, paramName).map(mapper);
//  } 
//  
//  public static <T> Optional<List<T>> qpAsListOfMappedValue(Request req, String paramName, Function<String, T> mapper) {
//    return asListOfStrings(req, paramName).map(ps -> ps.stream().map(mapper).collect(Collectors.toList()));
//  }
//
//}
