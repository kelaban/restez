package util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class AtomicSingleton<T> {
  AtomicBoolean isSet = new AtomicBoolean(false);
  T object = null;

  public T getOrSet(Supplier<T> supplier) {
    if (isSet.get()) {
      return object;
    } else {
      setWith(supplier);
    }

    return object;
  }

  private synchronized void setWith(Supplier<T> supplier) {
    if (isSet.get()) {
      return;
    }

    object = supplier.get();
    isSet.set(true);
  }
}
