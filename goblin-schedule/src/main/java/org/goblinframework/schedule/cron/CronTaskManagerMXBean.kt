package org.goblinframework.schedule.cron

import java.lang.management.PlatformManagedObject

interface CronTaskManagerMXBean : PlatformManagedObject {

  fun getCronTaskList(): Array<CronTaskMXBean>

}