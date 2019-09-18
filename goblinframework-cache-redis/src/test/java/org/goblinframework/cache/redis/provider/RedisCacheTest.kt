package org.goblinframework.cache.redis.provider

import org.bson.types.ObjectId
import org.goblinframework.cache.core.cache.CasOperation
import org.goblinframework.cache.redis.module.test.FlushRedisCache
import org.goblinframework.core.util.RandomUtils
import org.goblinframework.test.runner.GoblinTestRunner
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import java.util.concurrent.CountDownLatch
import kotlin.math.abs

@RunWith(GoblinTestRunner::class)
@ContextConfiguration("/UT.xml")
@FlushRedisCache
class RedisCacheTest {

  @Test
  fun add() {
    val cache = RedisCacheBuilder.INSTANCE.getCache("goblin")!!
    val key = RandomUtils.nextObjectId()
    val value = ObjectId()
    val ret = cache.add(key, 1800, value)
    assertTrue(ret!!)
    val gr = cache.get<ObjectId>(key)!!
    assertEquals(value, gr.value)
    assertTrue(gr.hit)
    assertFalse(gr.wrapper)
  }

  @Test
  fun append() {
    val cache = RedisCacheBuilder.INSTANCE.getCache("goblin")!!
    val key = RandomUtils.nextObjectId()
    cache.set(key, 1800, "HELLO")
    cache.append(key, " WORLD")
  }

  @Test
  fun incr() {
    val cache = RedisCacheBuilder.INSTANCE.getCache("goblin")!!
    // no initial value
    var key = RandomUtils.nextObjectId()
    var value = cache.incr(key, 10, 100, 3600)
    assertEquals(100, value!!.toLong())
    value = cache.incr(key, 10, 200, 7200) // initial and expiration skipped
    assertEquals(110, value!!.toLong())
    var ttl = cache.ttl(key)
    assertTrue(abs(ttl!!.toLong() - 3600) < 10)

    // initial value present
    key = RandomUtils.nextObjectId()
    cache.set(key, 1800, "50")
    value = cache.incr(key, 10, 200, 3600) // initial and expiration skipped
    assertEquals(60, value!!.toLong())
    value = cache.incr(key, 10, 200, 3600) // initial and expiration skipped
    assertEquals(70, value!!.toLong())
    ttl = cache.ttl(key)
    assertTrue(abs(ttl!!.toLong() - 1800) < 10)
  }

  @Test
  fun decr() {
    val cache = RedisCacheBuilder.INSTANCE.getCache("goblin")!!
    // no initial value
    var key = RandomUtils.nextObjectId()
    var value = cache.decr(key, 10, 100, 3600)
    assertEquals(100, value!!.toLong())
    value = cache.decr(key, 10, 200, 7200) // initial and expiration skipped
    assertEquals(90, value!!.toLong())
    var ttl = cache.ttl(key)
    assertTrue(abs(ttl!!.toLong() - 3600) < 10)

    // initial value present
    key = RandomUtils.nextObjectId()
    cache.set(key, 1800, "50")
    value = cache.decr(key, 10, 200, 3600) // initial and expiration skipped
    assertEquals(40, value!!.toLong())
    value = cache.decr(key, 10, 200, 3600) // initial and expiration skipped
    assertEquals(30, value!!.toLong())
    ttl = cache.ttl(key)
    assertTrue(abs(ttl!!.toLong() - 1800) < 10)
  }

  @Test
  fun cas() {
    val cache = RedisCacheBuilder.INSTANCE.getCache("goblin")!!
    val key = RandomUtils.nextObjectId()
    cache.set(key, 1800, mutableMapOf<Int, String>())
    val success = mutableListOf<Int>()
    val latch = CountDownLatch(10)
    for (i in 0..9) {
      val thread = object : Thread() {
        override fun run() {
          try {
            val co = CasOperation<MutableMap<Int, String>> { currentValue ->
              currentValue?.run { currentValue[i] = RandomUtils.nextObjectId() }
              currentValue
            }
            val ret = cache.cas(key, 1800, null, 0, co)
            if (ret != null && ret) {
              success.add(i)
            }
          } finally {
            latch.countDown()
          }
        }
      }
      thread.isDaemon = true
      thread.start()
    }
    latch.await()
    assertFalse(success.isEmpty())
    val map = cache.get<MutableMap<Int, String>>(key)!!.value
    for (i in success) {
      assertTrue(map.containsKey(i))
    }
  }
}