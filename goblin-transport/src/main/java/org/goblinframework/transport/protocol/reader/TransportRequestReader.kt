package org.goblinframework.transport.protocol.reader

import org.goblinframework.api.core.CompressorMode
import org.goblinframework.core.compression.CompressorManager
import org.goblinframework.core.serialization.SerializerManager
import org.goblinframework.core.util.StringUtils
import org.goblinframework.transport.protocol.TransportRequest

class TransportRequestReader(private val request: TransportRequest) {

  fun request(): TransportRequest {
    return request
  }

  fun readPayload(): Any? {
    if (!request.hasPayload) {
      return null
    }
    var data = request.payload

    if (request.compressor != 0.toByte()) {
      val mode = CompressorMode.resolve(request.compressor)
          ?: throw IllegalArgumentException("Unrecognized compressor ${request.compressor}")
      val compressor = CompressorManager.INSTANCE.getCompressor(mode)
      data = compressor.decompress(data)
    }

    if (request.rawPayload) {
      return data
    }

    return if (request.serializer == 0.toByte()) {
      if (data.isEmpty()) StringUtils.EMPTY else String(data, Charsets.UTF_8)
    } else {
      val serializer = SerializerManager.INSTANCE.getSerializer(request.serializer)
          ?: throw IllegalArgumentException("Unrecognized serializer ${request.serializer}")
      serializer.deserialize(data)
    }
  }
}