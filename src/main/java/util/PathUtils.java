package util;

public class PathUtils {

  public static String trimSlashes(String path) {
    return path.replaceFirst("^/*", "").replaceFirst("/*$", "");
  }
  
  public static String concatPath(String first, String second) {
    StringBuilder nextPath = new StringBuilder();
    
    return nextPath
        .append("/")
        .append(trimSlashes(first))
        .append("/")
        .append(trimSlashes(second))
        .toString();
  }
}
