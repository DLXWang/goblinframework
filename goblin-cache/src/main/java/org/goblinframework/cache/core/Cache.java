package org.goblinframework.cache.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public interface Cache {

  @NotNull
  CacheSystem getCacheSystem();

  @NotNull
  String getCacheName();

  @NotNull
  Object getNativeCache();

  @NotNull
  <K, V> CacheValueLoader<K, V> loader();

  @NotNull
  <V> CacheValueModifier<V> modifier();

  @Nullable
  <T> T load(@Nullable String key);

  @NotNull
  <T> GetResult<T> get(@Nullable String key);

  @NotNull
  <T> Map<String, GetResult<T>> gets(@Nullable Collection<String> keys);

  boolean delete(@Nullable String key);

  void deletes(@Nullable Collection<String> keys);

  <T> boolean add(@Nullable String key, int expirationInSeconds, @Nullable T value);

  <T> boolean set(@Nullable String key, int expirationInSeconds, @Nullable T value);

  <T> boolean replace(@Nullable String key, int expirationInSeconds, @Nullable T value);

  <T> boolean append(@Nullable String key, @Nullable T value);

  boolean touch(@Nullable String key, int expirationInSeconds);

  long ttl(@Nullable String key);

  long incr(@Nullable String key, long delta, long initialValue, int expirationInSeconds);

  long decr(@Nullable String key, long delta, long initialValue, int expirationInSeconds);

  <T> boolean cas(@Nullable String key, int expirationInSeconds, @Nullable GetResult<T> getResult, @Nullable CasOperation<T> casOperation);

  <T> boolean cas(@Nullable String key, int expirationInSeconds, @Nullable GetResult<T> getResult, int maxTries, @Nullable CasOperation<T> casOperation);

  void flush();
}
