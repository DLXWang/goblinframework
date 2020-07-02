package org.goblinframework.cache.core.internal

import org.goblinframework.cache.core.*
import org.goblinframework.core.service.GoblinManagedBean
import org.goblinframework.core.service.GoblinManagedObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@GoblinManagedBean("Cache")
class InJvmCacheBuilder : GoblinManagedObject(), CacheBuilder, CacheBuilderMXBean {

  private val buffer = ConcurrentHashMap<String, InJvmCache>()
  private val lock = ReentrantLock()

  override fun getCacheSystem(): CacheSystem {
    return CacheSystem.JVM
  }

  override fun getCache(name: String): Cache {
    buffer[name]?.run { return this }
    lock.withLock {
      buffer[name]?.run { return this }
      val cache = InJvmCache(name)
      buffer[name] = cache
      return cache
    }
  }

  override fun getCacheList(): Array<CacheMXBean> {
    return buffer.values.sortedBy { it.cacheName }.toTypedArray()
  }
}