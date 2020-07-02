package org.goblinframework.transport.client.channel

import org.goblinframework.api.annotation.Singleton
import org.goblinframework.core.event.GoblinEventContext
import org.goblinframework.core.event.timer.SecondTimerEventListener
import org.goblinframework.transport.client.module.TransportClientModule

@Singleton
class TransportClientHeartbeatGenerator private constructor() : SecondTimerEventListener() {

  companion object {
    @JvmField val INSTANCE = TransportClientHeartbeatGenerator()
  }

  private val periodSeconds = TransportClientModule.heartbeatIntervalInSeconds

  override fun periodSeconds(): Long {
    return periodSeconds
  }

  override fun onEvent(context: GoblinEventContext) {
    TransportClientManager.INSTANCE.sendHeartbeat()
  }
}