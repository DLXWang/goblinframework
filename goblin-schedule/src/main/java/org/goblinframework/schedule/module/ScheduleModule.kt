package org.goblinframework.schedule.module

import org.goblinframework.api.annotation.Install
import org.goblinframework.core.system.*
import org.goblinframework.schedule.cron.CronTaskManager
import org.goblinframework.schedule.module.management.ScheduleManagement

@Install
class ScheduleModule : IModule {

  override fun id(): GoblinModule {
    return GoblinModule.SCHEDULE
  }

  override fun managementEntrance(): String? {
    return "/goblin/schedule/index.do"
  }

  override fun install(ctx: ModuleInstallContext) {
    ctx.registerManagementController(ScheduleManagement.INSTANCE)
  }

  override fun initialize(ctx: ModuleInitializeContext) {
    CronTaskManager.INSTANCE.initialize()
  }

  override fun finalize(ctx: ModuleFinalizeContext) {
    CronTaskManager.INSTANCE.dispose()
  }
}