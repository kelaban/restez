package com.k317h.restez.errors;

import javax.servlet.http.HttpServletResponse;

public class BadRequestException extends RestezApiException {

  public BadRequestException(String message) {
    super(message, HttpServletResponse.SC_BAD_REQUEST);
  }
  
  public BadRequestException(String message, Throwable cause) {
    super(message, HttpServletResponse.SC_BAD_REQUEST, cause);
  }

}
