package org.goblinframework.core.event

class ChannelNotFoundException(channel: String) : RuntimeException("Channel [$channel] not registered") {
  companion object {
    private const val serialVersionUID = 8192185254851464942L
  }
}
