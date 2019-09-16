package org.goblinframework.core.conversion

import org.goblinframework.core.container.SpringManagedBean
import org.goblinframework.core.util.DateFormatUtils
import org.goblinframework.test.runner.GoblinTestRunner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import java.time.Instant
import java.util.*

@RunWith(GoblinTestRunner::class)
@ContextConfiguration("/UT.xml")
class ConversionServiceTest : SpringManagedBean() {

  @Test
  fun calendarToLong() {
    val cs = ConversionService.INSTANCE
    assertTrue(cs.canConvert(Calendar::class.java, Long::class.java))
    val source = Calendar.getInstance()
    val target = cs.convert(source, Long::class.java)
    assertEquals(source.timeInMillis, target!!.toLong())
  }

  @Test
  fun calendarToString() {
    val cs = ConversionService.INSTANCE
    assertTrue(cs.canConvert(Calendar::class.java, String::class.java))
    val source = Calendar.getInstance()
    val target = cs.convert(source, String::class.java)
    val date = DateFormatUtils.parse(target)!!
    assertEquals(source.timeInMillis, date.time)
  }

  @Test
  fun dateToLong() {
    val cs = ConversionService.INSTANCE
    assertTrue(cs.canConvert(Date::class.java, Long::class.java))
    val source = Date()
    val target = cs.convert(source, Long::class.java)
    assertEquals(source.time, target!!.toLong())
  }

  @Test
  fun dateToString() {
    val cs = ConversionService.INSTANCE
    assertTrue(cs.canConvert(Date::class.java, String::class.java))
    val source = Date()
    val target = cs.convert(source, String::class.java)
    val date = DateFormatUtils.parse(target)
    assertEquals(source.time, date!!.time)
  }

  @Test
  fun instantToLong() {
    val cs = ConversionService.INSTANCE
    assertTrue(cs.canConvert(Instant::class.java, Long::class.java))
    val source = Instant.now()
    val target = cs.convert(source, Long::class.java)
    assertEquals(source.toEpochMilli(), target!!.toLong())
  }

  @Test
  fun instantToString() {
    val cs = ConversionService.INSTANCE
    assertTrue(cs.canConvert(Instant::class.java, String::class.java))
    val source = Instant.now()
    val target = cs.convert(source, String::class.java)
    val date = DateFormatUtils.parse(target)
    assertEquals(source.toEpochMilli(), date!!.time)
  }
}