package org.goblinframework.transport.server.channel

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.slf4j.LoggerFactory

class TransportServerChannelHandler(private val channelManager: TransportServerChannelManager)
  : SimpleChannelInboundHandler<Any?>() {

  companion object {
    private val logger = LoggerFactory.getLogger(TransportServerChannelHandler::class.java)
  }

  override fun channelRead0(ctx: ChannelHandlerContext, msg: Any?) {
    channelManager.getChannel(ctx.channel().id())?.onMessage(msg)
  }

  override fun channelActive(ctx: ChannelHandlerContext) {
    channelManager.register(ctx.channel())
  }

  override fun channelInactive(ctx: ChannelHandlerContext) {
    channelManager.unregister(ctx.channel().id())
  }

  override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.error("Exception caught at channel {}", ctx.channel(), cause)
    ctx.close()
  }
}