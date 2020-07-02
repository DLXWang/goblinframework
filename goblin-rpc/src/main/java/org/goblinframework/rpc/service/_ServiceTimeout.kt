package org.goblinframework.rpc.service

import org.goblinframework.api.rpc.ServiceTimeout
import java.lang.reflect.Method

fun calculateServiceTimeout(interfaceClass: Class<*>, defaultTimeout: Long): Long {
  check(interfaceClass.isInterface)
  val annotation = interfaceClass.getAnnotation(ServiceTimeout::class.java)
  val timeout = calculateServiceTimeout(annotation) ?: return defaultTimeout
  return if (timeout <= 0) defaultTimeout else timeout
}

fun calculateServiceTimeout(method: Method): Long? {
  val serviceTimeout = method.getAnnotation(ServiceTimeout::class.java)
  return calculateServiceTimeout(serviceTimeout)
}

fun calculateServiceTimeout(annotation: ServiceTimeout?): Long? {
  return annotation?.run {
    if (this.enable) {
      val t: Int = this.timeout
      if (t < 0) {
        null
      } else {
        val timeout: Long = this.unit.toMillis(t.toLong())
        return if (timeout < 0) null else timeout
      }
    } else {
      null
    }
  }
}