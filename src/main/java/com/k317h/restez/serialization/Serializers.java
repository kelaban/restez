package com.k317h.restez.serialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.http.MimeTypes;

public class Serializers {
  
  @FunctionalInterface
  public interface Serializer {
    public byte[] serialize(Object o) throws IOException;
  }
  
  private Serializer defaultSerializer = o -> o.toString().getBytes(StandardCharsets.UTF_8);
  private final Map<String, Serializer> serializers = new HashMap<>();
  
  private final boolean failOnMissingSerializer;
  
  public Serializers() {
    this(true);
  }
  
  public Serializers(boolean failOnMissingSerializer) {
    this.failOnMissingSerializer = failOnMissingSerializer;
  }
  
  public Serializers registerJsonSerializer(Serializer jsonSerializer) {
    return registerSerializer(MimeTypes.Type.APPLICATION_JSON.toString(), jsonSerializer);
  }
  
  public Serializers registerDefaultSerializer(Serializer defaultSerializer) {
    this.defaultSerializer = defaultSerializer;
    return this;
  }
  
  public Serializers registerSerializer(String mimeType, Serializer serializer) {
    serializers.put(mimeType, serializer);
    return this;
  }
  
  public byte[] serializeDefault(Object o) throws IOException {
    return defaultSerializer.serialize(o);
  }
  
  public byte[] serialize(Object o, String type) throws IOException {
    Serializer s = serializers.get(type);
    
    if(null == s) {
      if(failOnMissingSerializer) {
        throw new IllegalArgumentException("Serializer for " + type + " does not exist");
      }
      return serializeDefault(o);
    } 
    
    return s.serialize(o);
  }

}
