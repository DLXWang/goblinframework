package org.goblinframework.cache.core.cache;

public interface CacheHitListener {

  default void failSafeOnCacheHit(String key) {
    try {
      onCacheHit(key);
    } catch (Exception ignored) {
    }
  }

  void onCacheHit(String key);
}
