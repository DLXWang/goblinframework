package org.goblinframework.registry.module

import org.goblinframework.api.annotation.Install
import org.goblinframework.core.system.*

@Install
class RegistryModule : IModule {

  override fun id(): GoblinModule {
    return GoblinModule.REGISTRY
  }

  override fun install(ctx: ModuleInstallContext) {
    ctx.createSubModules()
        .module(GoblinSubModule.REGISTRY_ZOOKEEPER)
        .install(ctx)
  }

  override fun initialize(ctx: ModuleInitializeContext) {
    ctx.createSubModules()
        .module(GoblinSubModule.REGISTRY_ZOOKEEPER)
        .initialize(ctx)
  }

  override fun finalize(ctx: ModuleFinalizeContext) {
    ctx.createSubModules()
        .module(GoblinSubModule.REGISTRY_ZOOKEEPER)
        .finalize(ctx)
  }

}