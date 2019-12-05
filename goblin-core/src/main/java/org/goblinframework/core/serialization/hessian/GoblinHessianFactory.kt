package org.goblinframework.core.serialization.hessian

import com.caucho.hessian.io.HessianFactory
import com.caucho.hessian.io.SerializerFactory
import org.goblinframework.core.util.ClassUtils

class GoblinHessianFactory private constructor() : HessianFactory() {

  companion object {
    @JvmField val INSTANCE = GoblinHessianFactory()
  }

  init {
    serializerFactory = SerializerFactory(ClassUtils.getDefaultClassLoader())
  }
}