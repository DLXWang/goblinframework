package org.goblinframework.embedded.netty.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.timeout.IdleStateHandler
import org.goblinframework.api.function.Disposable
import org.goblinframework.core.exception.GoblinInitializationException
import org.goblinframework.core.util.NetworkUtils
import org.goblinframework.embedded.setting.ServerSetting
import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class NettyEmbeddedServerImpl(private val setting: ServerSetting) : Disposable {

  companion object {
    private const val MAX_HTTP_CONTENT_LENGTH = 10 * 1024 * 1024
  }

  val boss: NioEventLoopGroup
  val worker: NioEventLoopGroup
  val host: String
  val port: Int

  private val executor: ThreadPoolExecutor

  init {
    val bossThreads = 1
    val workerThreads = 4
    boss = NioEventLoopGroup(bossThreads)
    worker = NioEventLoopGroup(workerThreads)

    executor = ThreadPoolExecutor(
        setting.threadPoolSetting().corePoolSize(),
        setting.threadPoolSetting().maximumPoolSize(),
        setting.threadPoolSetting().keepAliveTime(),
        setting.threadPoolSetting().unit(),
        LinkedBlockingQueue())

    val bootstrap = ServerBootstrap()
        .group(boss, worker)
        .channel(NioServerSocketChannel::class.java)
        .localAddress(setting.networkSetting().toInetSocketAddress())
        .childHandler(object : ChannelInitializer<SocketChannel>() {
          override fun initChannel(ch: SocketChannel) {
            val pipeline = ch.pipeline()
            pipeline.addLast("idleStateHandler", IdleStateHandler(60, 0, 0))
            pipeline.addLast("readerIdleConnectionHandler", ReaderIdleConnectionHandler())
            pipeline.addLast("encoder", HttpResponseEncoder())
            pipeline.addLast("decoder", HttpRequestDecoder())
            pipeline.addLast("httpKeepAlive", HttpServerKeepAliveHandler())
            pipeline.addLast("aggregator", HttpObjectAggregator(MAX_HTTP_CONTENT_LENGTH))
            pipeline.addLast("compressor", HttpContentCompressor())
            pipeline.addLast(NettyHttpRequestHandler(setting, executor))
          }
        })
    val future = bootstrap.bind().sync()
    if (!future.isSuccess) {
      throw GoblinInitializationException(future.cause())
    }
    val channel = future.channel()
    var h = (channel.localAddress() as InetSocketAddress).address.hostAddress
    if (h == "0:0:0:0:0:0:0:0") {
      h = NetworkUtils.ALL_HOST
    }
    host = h
    port = (channel.localAddress() as InetSocketAddress).port
  }

  override fun dispose() {
    boss.shutdownGracefully().awaitUninterruptibly()
    worker.shutdownGracefully().awaitUninterruptibly()
    executor.shutdown()
    executor.awaitTermination(5, TimeUnit.SECONDS)
  }
}