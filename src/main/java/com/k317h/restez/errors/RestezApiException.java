package com.k317h.restez.errors;

public class RestezApiException extends RuntimeException {
  
  private final int code;
  
  public RestezApiException(String message, int code) {
    super(message);
    this.code = code;
  }
  
  public RestezApiException(String message, int code, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public int getCode() {
    return code;
  }

}
