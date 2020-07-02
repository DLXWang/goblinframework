package org.goblinframework.monitor.message

import org.goblinframework.core.event.GoblinEventContext
import org.goblinframework.core.event.timer.MinuteTimerEventListener

class TimedTouchableMessageBufferScheduler
internal constructor(private val manager: TimedTouchableMessageBufferManager)
  : MinuteTimerEventListener() {

  override fun periodMinutes(): Long {
    return 10
  }

  override fun onEvent(context: GoblinEventContext) {
    manager.executeActions()
  }
}