package org.goblinframework.core.compress

import org.apache.commons.lang3.RandomStringUtils
import org.goblinframework.api.compression.Compressor
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class CompressionManagerTest {

  @Test
  fun testCompression() {
    val cm = CompressionManager.instance
    for (compressor in Compressor.values()) {
      val c = cm.getCompression(compressor)

      val s = RandomStringUtils.randomAlphanumeric(1024 * 1024)
      var i = s.byteInputStream(Charsets.UTF_8)
      var o = ByteArrayOutputStream()
      c.compress(i, o)
      val compressed = o.toByteArray()
      i.close()
      o.close()

      i = ByteArrayInputStream(compressed)
      o = ByteArrayOutputStream()
      c.decompress(i, o)
      val t = o.toByteArray().toString(Charsets.UTF_8)
      i.close()
      o.close()

      Assert.assertEquals(s, t)
    }
  }
}