package org.goblinframework.cache.redis.module.config

import org.goblinframework.api.core.SerializerMode
import org.goblinframework.core.config.BufferedConfigParser
import org.goblinframework.core.config.ConfigManager
import org.goblinframework.core.config.GoblinConfigException
import org.goblinframework.core.util.StringUtils

class RedisConfigParser internal constructor() : BufferedConfigParser<RedisConfig>() {

  companion object {
    private const val DEFAULT_REDIS_PORT = 6379
  }

  override fun initializeBean() {
    val mapping = ConfigManager.INSTANCE.getMapping()
    parseToMap(mapping, "redis", RedisConfigMapper::class.java)
        .map { it.value.also { c -> c.name = it.key } }
        .map { RedisConfig(it) }
        .forEach { putIntoBuffer(it.getName(), it) }
  }

  override fun doProcessConfig(config: RedisConfig) {
    val mapper = config.mapper
    if (StringUtils.isBlank(mapper.servers)) {
      throw GoblinConfigException("RedisConfig.servers is required")
    }
    if (mapper.mode == null) {
      throw GoblinConfigException("RedisConfig.mode is required")
    }
    val servers = StringUtils.formalizeServers(mapper.servers, " ") { DEFAULT_REDIS_PORT }
    mapper.servers = servers

    mapper.serializer ?: kotlin.run { mapper.serializer = SerializerMode.HESSIAN2 }
    mapper.maxTotal ?: kotlin.run { mapper.maxTotal = 16 }
    mapper.maxIdle ?: kotlin.run { mapper.maxIdle = 2 }
    mapper.minIdle ?: kotlin.run { mapper.minIdle = 0 }
    mapper.maxWaitMillis ?: kotlin.run { mapper.maxWaitMillis = -1L }
    mapper.testOnCreate ?: kotlin.run { mapper.testOnCreate = false }
    mapper.testOnBorrow ?: kotlin.run { mapper.testOnBorrow = false }
    mapper.testOnReturn ?: kotlin.run { mapper.testOnReturn = false }
    mapper.testWhileIdle ?: kotlin.run { mapper.testWhileIdle = false }
    mapper.flushable ?: kotlin.run { mapper.flushable = false }
  }
}