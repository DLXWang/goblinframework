package org.goblinframework.remote.client.dispatcher.request

import org.goblinframework.core.event.GoblinEventContext
import org.goblinframework.core.event.GoblinEventListener
import org.goblinframework.core.service.GoblinManagedBean
import org.goblinframework.core.service.GoblinManagedObject

@GoblinManagedBean("RemoteClient")
class RemoteClientRequestEventListener internal constructor()
  : GoblinManagedObject(), GoblinEventListener, RemoteClientRequestEventListenerMXBean {

  private val threadPool = RemoteClientRequestThreadPool()

  override fun accept(context: GoblinEventContext): Boolean {
    return context.event is RemoteClientRequestEvent
  }

  override fun onEvent(context: GoblinEventContext) {
    val event = context.event as RemoteClientRequestEvent
    threadPool.onInvocation(event.invocation)
  }

  override fun disposeBean() {
    threadPool.dispose()
  }
}