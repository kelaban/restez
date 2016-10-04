package com.k317h.restez;

import com.k317h.restez.io.Request;
import com.k317h.restez.io.Response;

@FunctionalInterface
public interface Handler {
  public void handle(Request req, Response res) throws Exception;
}
