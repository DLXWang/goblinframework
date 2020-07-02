package org.goblinframework.cache.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CacheBuilder {

  @NotNull
  CacheSystem getCacheSystem();

  @Nullable
  Cache getCache(@NotNull String name);

}
