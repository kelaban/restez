package com.k317h.restez.serialization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import com.k317h.restez.errors.SerializationException;
import com.k317h.restez.http.MimeTypes;

public class Deserializers {

    @FunctionalInterface
    public interface Deserializer {
      public <T> T deserialize(byte[] o, Class<T> clazz) throws SerializationException;
    }

    private final Map<String, Deserializer> deserializers = new HashMap<>();

    public Deserializers() { }


    public Deserializers registerJsonDeserializer(Deserializer jsonDeserializer) {
      return registerSerializer(MimeTypes.APPLICATION_JSON, jsonDeserializer);
    }


    public Deserializers registerSerializer(String mimeType, Deserializer serializer) {
      deserializers.put(mimeType, serializer);
      return this;
    }

    public <T> T deserialize(byte[] o, String contentType, Class<T> clazz) throws IOException {
      Deserializer s = deserializers.get(contentType);

      if(null == s) {
        throw new SerializationException("Deserializer for '" + contentType + "' does not exist");
      }

      return s.deserialize(o, clazz);
    }

}
