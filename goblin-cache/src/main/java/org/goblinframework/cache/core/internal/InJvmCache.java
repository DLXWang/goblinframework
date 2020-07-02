package org.goblinframework.cache.core.internal;

import org.apache.commons.collections4.map.LRUMap;
import org.goblinframework.api.annotation.ThreadSafe;
import org.goblinframework.cache.core.*;
import org.goblinframework.cache.module.monitor.VMC;
import org.goblinframework.core.service.GoblinManagedBean;
import org.goblinframework.core.service.GoblinManagedObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ThreadSafe
@GoblinManagedBean(type = "Cache")
final public class InJvmCache extends GoblinManagedObject implements Cache, CacheMXBean {

  private final String cacheName;
  private final Timer watchdogTimer;
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final LRUMap<String, CacheItem> buffer = new LRUMap<>(65536);

  InJvmCache(@NotNull String cacheName) {
    this.cacheName = cacheName;
    watchdogTimer = new Timer("InJvmCacheWatchdogTimer", true);
    watchdogTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        clean();
      }
    }, 10000, 10000);
  }

  private static class CacheItem {
    private AtomicLong expireAt = new AtomicLong();
    private Object value;
    private final AtomicLong cas = new AtomicLong();

    private void touchExpirationInSeconds(int expirationInSeconds) {
      if (expirationInSeconds == 0) {
        expireAt.set(0);
      } else {
        expireAt.set(System.currentTimeMillis() + (expirationInSeconds * 1000));
      }
    }

    private boolean isExpired() {
      long e = expireAt.get();
      return e != 0 && System.currentTimeMillis() >= e;
    }
  }

  @Nullable
  private CacheItem retrieve(@NotNull String key) {
    CacheItem ci;
    lock.readLock().lock();
    try {
      ci = buffer.get(key);
    } finally {
      lock.readLock().unlock();
    }
    if (ci == null) return null;
    if (!ci.isExpired()) return ci;
    lock.writeLock().lock();
    try {
      buffer.remove(key);
    } finally {
      lock.writeLock().unlock();
    }
    return null;
  }

  @NotNull
  @Override
  public CacheSystem getCacheSystem() {
    return CacheSystem.JVM;
  }

  @NotNull
  @Override
  public String getCacheName() {
    return cacheName;
  }

  @NotNull
  @Override
  public Object getNativeCache() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public <K, V> CacheValueLoader<K, V> loader() {
    return new CacheValueLoaderImpl<>(this);
  }

  @NotNull
  @Override
  public <V> CacheValueModifier<V> modifier() {
    return new CacheValueModifierImpl<>(this);
  }

  @Nullable
  @Override
  public <T> T load(@Nullable String key) {
    GetResult<T> gr = get(key);
    return gr.value;
  }

  @NotNull
  @Override
  public <T> GetResult<T> get(@Nullable String key) {
    if (key == null) {
      return new GetResult<>(null);
    }
    try (VMC instruction = new VMC()) {
      instruction.operation = "get";
      instruction.keys = Collections.singletonList(key);

      CacheItem ci = retrieve(key);
      if (ci == null) {
        return new GetResult<>(key);
      }
      GetResult<T> gr = new GetResult<>(key);
      gr.cas = ci.cas.get();
      gr.hit = true;
      if (ci.value instanceof CacheValueWrapper) {
        gr.wrapper = true;
        gr.uncheckedSetValue(((CacheValueWrapper) ci.value).getValue());
      } else {
        gr.uncheckedSetValue(ci.value);
      }
      return gr;
    }
  }

  @NotNull
  @Override
  public <T> Map<String, GetResult<T>> gets(@Nullable Collection<String> keys) {
    if (keys == null || keys.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, GetResult<T>> result = new LinkedHashMap<>();
    keys.stream().distinct().forEach(id -> {
      GetResult<T> gr = get(id);
      result.put(id, gr);
    });
    return result;
  }

  @Override
  public boolean delete(@Nullable String key) {
    if (key == null) {
      return false;
    }
    try (VMC instruction = new VMC()) {
      instruction.operation = "delete";
      instruction.keys = Collections.singletonList(key);

      CacheItem ci;
      lock.writeLock().lock();
      try {
        ci = buffer.remove(key);
      } finally {
        lock.writeLock().unlock();
      }
      return ci != null && !ci.isExpired();
    }
  }

  @Override
  public void deletes(@Nullable Collection<String> keys) {
    if (keys == null || keys.isEmpty()) return;
    keys.stream().filter(Objects::nonNull).distinct().forEach(this::delete);
  }

  @Override
  public <T> boolean add(@Nullable String key, int expirationInSeconds, @Nullable T value) {
    if (key == null || expirationInSeconds < 0) {
      return false;
    }
    try (VMC instruction = new VMC()) {
      instruction.operation = "add";
      instruction.keys = Collections.singletonList(key);

      CacheItem ci = new CacheItem();
      ci.value = value;
      ci.touchExpirationInSeconds(expirationInSeconds);
      lock.writeLock().lock();
      try {
        return buffer.putIfAbsent(key, ci) == null;
      } finally {
        lock.writeLock().unlock();
      }
    }
  }

  @Override
  public <T> boolean set(@Nullable String key, int expirationInSeconds, @Nullable T value) {
    if (key == null || expirationInSeconds < 0) {
      return false;
    }
    try (VMC instruction = new VMC()) {
      instruction.operation = "set";
      instruction.keys = Collections.singletonList(key);

      CacheItem ci = retrieve(key);
      if (ci != null) {
        ci.touchExpirationInSeconds(expirationInSeconds);
        ci.value = value;
        ci.cas.incrementAndGet();
        return true;
      }
      return add(key, expirationInSeconds, value);
    }
  }

  @Override
  public <T> boolean replace(@Nullable String key, int expirationInSeconds, @Nullable T value) {
    if (key == null || expirationInSeconds < 0) {
      return false;
    }
    try (VMC instruction = new VMC()) {
      instruction.operation = "replace";
      instruction.keys = Collections.singletonList(key);

      CacheItem ci = retrieve(key);
      if (ci == null) {
        return false;
      }
      ci.touchExpirationInSeconds(expirationInSeconds);
      ci.value = value;
      ci.cas.incrementAndGet();
      return true;
    }
  }

  @Override
  public <T> boolean append(@Nullable String key, @Nullable T value) {
    if (key == null || !(value instanceof CharSequence)) {
      return false;
    }
    try (VMC instruction = new VMC()) {
      instruction.operation = "append";
      instruction.keys = Collections.singletonList(key);

      CacheItem ci = retrieve(key);
      if (ci == null || !(ci.value instanceof String)) {
        return false;
      }
      ci.value = ci.value.toString() + value.toString();
      ci.cas.incrementAndGet();
      return true;
    }
  }

  @Override
  public boolean touch(@Nullable String key, int expirationInSeconds) {
    if (key == null || expirationInSeconds < 0) {
      return false;
    }
    try (VMC instruction = new VMC()) {
      instruction.operation = "touch";
      instruction.keys = Collections.singletonList(key);

      CacheItem ci = retrieve(key);
      if (ci == null) return false;
      ci.touchExpirationInSeconds(expirationInSeconds);
      return true;
    }
  }

  @Override
  public long ttl(@Nullable String key) {
    if (key == null) {
      throw new IllegalArgumentException();
    }
    try (VMC instruction = new VMC()) {
      instruction.operation = "ttl";
      instruction.keys = Collections.singletonList(key);

      CacheItem ci = retrieve(key);
      if (ci == null) return -1;
      long e = ci.expireAt.get();
      if (e == 0) return 0;
      long delta = e - System.currentTimeMillis();
      if (delta < 0) return -1;
      long ttl = new BigDecimal(delta).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_UP).longValue();
      return Math.max(ttl, 1);
    }
  }

  @Override
  public long incr(@Nullable String key, long delta, long initialValue, int expirationInSeconds) {
    if (key == null || expirationInSeconds < 0) {
      throw new IllegalArgumentException();
    }
    try (VMC instruction = new VMC()) {
      instruction.operation = "incr";
      instruction.keys = Collections.singletonList(key);

      CacheItem ci = retrieve(key);
      if (ci == null) {
        if (add(key, expirationInSeconds, Long.toString(initialValue))) {
          return initialValue;
        } else {
          throw new ConcurrentModificationException();
        }
      }
      long v = Long.parseLong(ci.value.toString()) + delta;
      ci.touchExpirationInSeconds(expirationInSeconds);
      ci.value = Long.toString(v);
      ci.cas.incrementAndGet();
      return v;
    }
  }

  @Override
  public long decr(@Nullable String key, long delta, long initialValue, int expirationInSeconds) {
    return incr(key, -delta, initialValue, expirationInSeconds);
  }

  @Override
  public <T> boolean cas(@Nullable String key, int expirationInSeconds, @Nullable GetResult<T> getResult, @Nullable CasOperation<T> casOperation) {
    int maxTries = casOperation == null ? 0 : casOperation.getMaxTries();
    return cas(key, expirationInSeconds, getResult, maxTries, casOperation);
  }

  @Override
  public <T> boolean cas(@Nullable String key, int expirationInSeconds, @Nullable GetResult<T> getResult, int maxTries, @Nullable CasOperation<T> casOperation) {
    if (key == null || expirationInSeconds < 0 || getResult == null || casOperation == null) {
      return false;
    }
    try (VMC instruction = new VMC()) {
      instruction.operation = "cas";
      instruction.keys = Collections.singletonList(key);

      int max = Math.max(0, maxTries);
      int retries = 0;
      GetResult<T> gr = getResult;
      while (retries <= max) {
        if (retries > 0) {
          gr = get(key);
          if (!gr.hit) return false;
        }
        CacheItem ci = retrieve(key);
        if (ci == null) return false;
        if (ci.cas.longValue() == gr.cas) {
          @SuppressWarnings("unchecked")
          T newValue = casOperation.changeCacheObject((T) ci.value);
          ci.value = newValue;
          ci.touchExpirationInSeconds(expirationInSeconds);
          ci.cas.incrementAndGet();
          return true;
        }
        retries++;
      }
      return false;
    }
  }

  @Override
  public void flush() {
    try (VMC instruction = new VMC()) {
      instruction.operation = "flush";
      lock.writeLock().lock();
      try {
        buffer.clear();
        logger.info("JVM cache flushed");
      } finally {
        lock.writeLock().unlock();
      }
    }
  }

  private void clean() {
    List<String> expires = new LinkedList<>();
    lock.readLock().lock();
    try {
      buffer.forEach((key, ci) -> {
        if (ci.isExpired()) expires.add(key);
      });
    } finally {
      lock.readLock().unlock();
    }
    if (expires.isEmpty()) return;
    for (String key : expires) {
      retrieve(key);
    }
  }

  @Override
  protected void disposeBean() {
    watchdogTimer.cancel();
    lock.writeLock().lock();
    try {
      buffer.clear();
    } finally {
      lock.writeLock().unlock();
    }
  }
}
