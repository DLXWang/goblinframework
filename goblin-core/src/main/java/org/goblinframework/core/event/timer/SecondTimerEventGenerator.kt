package org.goblinframework.core.event.timer

import org.goblinframework.core.event.EventBus
import org.goblinframework.core.schedule.CronConstants
import org.goblinframework.core.schedule.CronTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class SecondTimerEventGenerator internal constructor() : CronTask {

  private val sequence = AtomicLong()

  override fun name(): String {
    return "SecondTimerEventGenerator"
  }

  override fun cronExpression(): String {
    return CronConstants.SECOND_TIMER
  }

  override fun concurrent(): Boolean {
    return true
  }

  override fun flight(): Boolean {
    return false
  }

  override fun execute() {
    val next = sequence.getAndIncrement()
    val event = GoblinTimerEvent(TimeUnit.SECONDS, next)
    EventBus.publish("/goblin/timer", event)
  }
}