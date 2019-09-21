package org.goblinframework.core.bootstrap

import org.goblinframework.core.event.GoblinEventChannel
import org.goblinframework.core.event.GoblinEventContext
import org.goblinframework.core.event.GoblinEventListener

@GoblinEventChannel("/goblin/core")
class GoblinChildModuleEventListener private constructor() : GoblinEventListener {

  companion object {
    @JvmField val INSTANCE = GoblinChildModuleEventListener()
  }

  override fun accept(context: GoblinEventContext): Boolean {
    return context.event is GoblinChildModuleEvent
  }

  override fun onEvent(context: GoblinEventContext) {
    val event = context.event as GoblinChildModuleEvent
    when (event.ctx) {
      is GoblinModuleInstallContext -> {
        event.childModules.forEach {
          it.install(event.ctx)
          GoblinSystem.LOGGER.info("Install [${it.name()}]")
        }
      }
      is GoblinModuleBootstrapContext -> {
        event.childModules.forEach {
          it.bootstrap(event.ctx)
          GoblinSystem.LOGGER.info("Bootstrap [${it.name()}]")
        }
      }
      is GoblinModuleFinalizeContext -> {
        event.childModules.reversed().forEach {
          it.finalize(event.ctx)
          GoblinSystem.LOGGER.info("Finalize [${it.name()}]")
        }
      }
    }
  }
}