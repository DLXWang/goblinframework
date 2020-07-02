package org.goblinframework.cache.module.container;

import org.goblinframework.cache.exception.GoblinCacheException;

public class InternalInvocationTargetException extends GoblinCacheException {
  private static final long serialVersionUID = 7820802986696337666L;

  public InternalInvocationTargetException(Throwable cause) {
    super(cause);
  }
}
