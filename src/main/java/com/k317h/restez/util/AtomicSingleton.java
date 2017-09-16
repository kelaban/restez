package com.k317h.restez.util;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AtomicSingleton<T> {
  
  @FunctionalInterface
  public interface ThrowingSupplier<T>{
    public T get() throws IOException;
  }
  
  AtomicBoolean isSet = new AtomicBoolean(false);
  T object = null;

  public T getOrSet(ThrowingSupplier<T> supplier) throws IOException {
    if (isSet.get()) {
      return object;
    } else {
      setWith(supplier);
    }

    return object;
  }

  private synchronized void setWith(ThrowingSupplier<T> supplier) throws IOException {
    if (isSet.get()) {
      return;
    }

    object = supplier.get();
    isSet.set(true);
  }
}
