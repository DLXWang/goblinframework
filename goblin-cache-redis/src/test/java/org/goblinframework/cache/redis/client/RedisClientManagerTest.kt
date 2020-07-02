package org.goblinframework.cache.redis.client

import org.goblinframework.test.runner.GoblinTestRunner
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration

@RunWith(GoblinTestRunner::class)
@ContextConfiguration("/UT.xml")
class RedisClientManagerTest {

  @Test
  fun getRedisClient() {
    val client = RedisClientManager.INSTANCE.getRedisClient("_ut")
    assertNotNull(client)
    client?.run {
      val connection = openPooledConnection()
      returnPooledConnection(connection)
    }
    RedisClientManager.INSTANCE.closeRedisClient("_ut")
  }
}