package org.goblinframework.registry.zookeeper.module

import org.goblinframework.api.annotation.Install
import org.goblinframework.core.system.GoblinSubModule
import org.goblinframework.core.system.ISubModule
import org.goblinframework.core.system.ModuleFinalizeContext
import org.goblinframework.core.system.ModuleInstallContext
import org.goblinframework.registry.core.RegistrySystem
import org.goblinframework.registry.core.module.RegistryModule
import org.goblinframework.registry.zookeeper.client.ZookeeperClientManager
import org.goblinframework.registry.zookeeper.module.config.ZookeeperConfigManager
import org.goblinframework.registry.zookeeper.provider.ZookeeperRegistryBuilder

@Install
class RegistryZookeeperModule : ISubModule {

  override fun id(): GoblinSubModule {
    return GoblinSubModule.REGISTRY_ZOOKEEPER
  }

  override fun install(ctx: ModuleInstallContext) {
    ctx.registerConfigParser(ZookeeperConfigManager.INSTANCE.configParser)
    val parent = ctx.getExtension(RegistryModule::class.java)
    parent?.registerRegistryBuilder(RegistrySystem.ZKP, ZookeeperRegistryBuilder.INSTANCE)
  }

  override fun finalize(ctx: ModuleFinalizeContext) {
    ZookeeperClientManager.INSTANCE.dispose()
    ZookeeperConfigManager.INSTANCE.dispose()
  }
}