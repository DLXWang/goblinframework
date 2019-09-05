package org.goblinframework.core.event

import com.lmax.disruptor.WorkHandler

class EventBusWorkerEventHandler private constructor() : WorkHandler<EventBusWorkerEvent> {

  companion object {
    @JvmField val INSTANCE = EventBusWorkerEventHandler()
  }

  override fun onEvent(event: EventBusWorkerEvent) {
    try {
      processEventBusWorkerEvent(event)
    } finally {
      event.clear()
    }
  }

  private fun processEventBusWorkerEvent(event: EventBusWorkerEvent) {
    val ctx = event.ctx!!
    val listeners = event.listeners!!
    listeners.forEach { it.onEvent(ctx) }
  }
}