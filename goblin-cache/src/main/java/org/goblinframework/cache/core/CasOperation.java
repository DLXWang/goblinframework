package org.goblinframework.cache.core;

import org.jetbrains.annotations.Nullable;

public interface CasOperation<T> {

  default int getMaxTries() {
    return 0;
  }

  @Nullable
  T changeCacheObject(@Nullable T currentValue);

}
