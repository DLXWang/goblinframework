package org.goblinframework.remote.client.service

import org.goblinframework.api.annotation.Singleton
import org.goblinframework.core.service.GoblinManagedBean
import org.goblinframework.core.service.GoblinManagedObject
import org.goblinframework.registry.core.Registry
import org.goblinframework.remote.client.module.config.RemoteClientConfigManager
import org.goblinframework.remote.core.GoblinRemoteException
import java.util.concurrent.atomic.AtomicReference

@Singleton
@GoblinManagedBean(type = "RemoteClient")
class RemoteClientRegistryManager private constructor()
  : GoblinManagedObject(), RemoteClientRegistryManagerMXBean {

  companion object {
    @JvmField val INSTANCE = RemoteClientRegistryManager()
  }

  private val registry = AtomicReference<Registry?>()

  init {
    RemoteClientConfigManager.INSTANCE.getRemoteClientConfig()?.registryLocation?.run {
      val registry = system.getRegistry(name) ?: kotlin.run {
        throw GoblinRemoteException("Registry [$this] not available")
      }
      this@RemoteClientRegistryManager.registry.set(registry)
    }
  }

  fun getRegistry(): Registry? {
    return registry.get()
  }

}