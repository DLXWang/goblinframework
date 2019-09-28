package org.goblinframework.api.cache;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GoblinCacheBean {

  Class<?> type();

  CacheSystem system() default CacheSystem.CBS;

  String name();

  boolean wrapper() default false;

  GoblinCacheExpiration expiration() default @GoblinCacheExpiration(enable = false);

  boolean enable() default true;

}