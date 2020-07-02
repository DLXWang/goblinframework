package org.goblinframework.transport.module

import org.goblinframework.api.annotation.Install
import org.goblinframework.core.system.*

@Install
class TransportModule : IModule {

  override fun id(): GoblinModule {
    return GoblinModule.TRANSPORT
  }

  override fun install(ctx: ModuleInstallContext) {
    ctx.createSubModules()
        .module(GoblinSubModule.TRANSPORT_CLIENT)
        .next()
        .module(GoblinSubModule.TRANSPORT_SERVER)
        .install(ctx)
  }

  override fun initialize(ctx: ModuleInitializeContext) {
    ctx.createSubModules()
        .module(GoblinSubModule.TRANSPORT_CLIENT)
        .next()
        .module(GoblinSubModule.TRANSPORT_SERVER)
        .initialize(ctx)
  }

  override fun finalize(ctx: ModuleFinalizeContext) {
    ctx.createSubModules()
        .module(GoblinSubModule.TRANSPORT_SERVER)
        .next()
        .module(GoblinSubModule.TRANSPORT_CLIENT)
        .finalize(ctx)
  }
}