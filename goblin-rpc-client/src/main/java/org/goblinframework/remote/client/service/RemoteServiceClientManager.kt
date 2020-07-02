package org.goblinframework.remote.client.service

import org.goblinframework.api.annotation.Singleton
import org.goblinframework.api.annotation.ThreadSafe
import org.goblinframework.core.service.GoblinManagedBean
import org.goblinframework.core.service.GoblinManagedLogger
import org.goblinframework.core.service.GoblinManagedObject
import org.goblinframework.rpc.registry.RpcRegistryManager
import org.goblinframework.rpc.service.RemoteServiceId
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Singleton
@ThreadSafe
@GoblinManagedBean("RemoteClient")
@GoblinManagedLogger("goblin.remote.client.service")
class RemoteServiceClientManager private constructor()
  : GoblinManagedObject(), RemoteServiceClientManagerMXBean {

  companion object {
    @JvmField val INSTANCE = RemoteServiceClientManager()
  }

  private val subscriptionManagerReference = AtomicReference<RemoteServiceClientSubscriptionManager?>()
  private val lock = ReentrantReadWriteLock()
  private val buffer = mutableMapOf<RemoteServiceId, RemoteServiceClient>()

  override fun initializeBean() {
    RpcRegistryManager.INSTANCE.getRpcRegistry()?.run {
      subscriptionManagerReference.set(RemoteServiceClientSubscriptionManager(this))
    }
  }

  fun getRemoteService(serviceId: RemoteServiceId): RemoteServiceClient {
    return lock.read { buffer[serviceId] } ?: lock.write {
      buffer[serviceId] ?: kotlin.run {
        val client = RemoteServiceClient(serviceId, subscriptionManagerReference.get())
        buffer[serviceId] = client
        client
      }
    }
  }

  override fun disposeBean() {
    lock.write {
      buffer.values.forEach { it.dispose() }
      buffer.clear()
    }
    subscriptionManagerReference.getAndSet(null)?.dispose()
  }
}