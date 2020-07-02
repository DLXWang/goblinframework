package org.goblinframework.cache.redis.module.config

import org.goblinframework.api.core.CompressorMode
import org.goblinframework.api.core.SerializerMode
import org.goblinframework.core.compression.CompressionThreshold
import java.io.Serializable

class RedisConfigMapper : Serializable {

  var name: String? = null
  var servers: String? = null
  var password: String? = null
  var mode: RedisServerMode? = null
  var serializer: SerializerMode? = null
  var compressor: CompressorMode? = null
  var compressionThreshold: CompressionThreshold? = null
  var maxTotal: Int? = null
  var maxIdle: Int? = null
  var minIdle: Int? = null
  var maxWaitMillis: Long? = null
  var testOnCreate: Boolean? = null
  var testOnBorrow: Boolean? = null
  var testOnReturn: Boolean? = null
  var testWhileIdle: Boolean? = null
  var flushable: Boolean? = null

  companion object {
    private const val serialVersionUID = -3756108596956143695L
  }
}
