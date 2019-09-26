package org.goblinframework.core.event

import java.lang.management.PlatformManagedObject

interface EventBusWorkerMXBean : PlatformManagedObject {

  fun getId(): String

  fun getUpTime(): String

  fun getChannel(): String

  fun getBufferSize(): Int

  fun getRemainingCapacity(): Int

  fun getWorkers(): Int

  fun getPublishedCount(): Long

  fun getDiscardedCount(): Long

  fun getReceivedCount(): Long

  fun getSucceedCount(): Long

  fun getFailedCount(): Long

  fun getEventListenerList(): Array<GoblinEventListenerMXBean>
}