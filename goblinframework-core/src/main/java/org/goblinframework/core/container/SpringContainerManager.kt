package org.goblinframework.core.container

import org.goblinframework.core.mbean.GoblinManagedBean
import org.goblinframework.core.mbean.GoblinManagedObject
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

@GoblinManagedBean("CORE")
class SpringContainerManager private constructor() : GoblinManagedObject(), SpringContainerManagerMXBean {

  companion object {
    @JvmField val INSTANCE = SpringContainerManager()
  }

  private val lock = ReentrantReadWriteLock()
  private val containers = LinkedHashMap<String, SpringContainer>()

  fun register(ctx: ApplicationContext) {
    if (ctx !is SpringContainerId) {
      throw UnsupportedOperationException()
    }
    val id = (ctx as SpringContainerId).uniqueId()
    lock.write { containers.putIfAbsent(id, SpringContainer(ctx)) }
  }

  fun unregister(ctx: ApplicationContext) {
    if (ctx !is SpringContainerId) {
      throw UnsupportedOperationException()
    }
    val id = (ctx as SpringContainerId).uniqueId()
    lock.write { containers.remove(id) }?.close()
  }

  fun close() {
    unregisterIfNecessary()
    lock.write {
      containers.values.map { it.applicationContext }
          .filterIsInstance<ConfigurableApplicationContext>()
          .forEach { it.close() }
      containers.clear()
    }
  }
}