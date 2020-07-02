package org.goblinframework.cache.redis.cache;

import org.goblinframework.cache.core.Cache;
import org.goblinframework.cache.core.CacheValueModifier;
import org.goblinframework.cache.core.CasOperation;
import org.jetbrains.annotations.NotNull;

final public class RedisCacheValueModifier<V> implements CacheValueModifier<V> {

  private final Cache cache;

  RedisCacheValueModifier(@NotNull Cache cache) {
    this.cache = cache;
  }

  private String key;
  private Integer expiration;
  private CasOperation<V> modifier;

  @Override
  public CacheValueModifier<V> key(String key) {
    if (this.key != null) {
      throw new IllegalStateException("Key already set");
    }
    this.key = key;
    return this;
  }

  @Override
  public CacheValueModifier<V> expiration(int expiration) {
    if (this.expiration != null) {
      throw new IllegalStateException("Expiration already set");
    }
    this.expiration = expiration;
    return this;
  }

  @Override
  public CacheValueModifier<V> modifier(CasOperation<V> modifier) {
    if (this.modifier != null) {
      throw new IllegalStateException("Modifier already set");
    }
    this.modifier = modifier;
    return this;
  }

  @Override
  public void execute() {
    if (key == null) {
      throw new IllegalStateException("Key not set");
    }
    if (expiration == null) {
      throw new IllegalStateException("Expiration not set");
    }
    if (modifier == null) {
      throw new IllegalStateException("Modifier not set");
    }
    if (!cache.cas(key, expiration, null, modifier)) {
      cache.delete(key);
    }
  }
}
