package org.goblinframework.core.serialization.hessian

import com.caucho.hessian.io.Hessian2Input
import com.caucho.hessian.io.Hessian2Output
import org.goblinframework.core.serialization.Serializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class Hessian2Serializer : Serializer {

  override fun id(): Byte {
    return Serializer.HESSIAN2
  }

  override fun serialize(obj: Any, outStream: OutputStream) {
    var ho: Hessian2Output? = null
    try {
      ho = GoblinHessianFactory.INSTANCE.createHessian2Output(outStream)
      ho.writeObject(obj)
      ho.flush()
    } finally {
      GoblinHessianFactory.INSTANCE.freeHessian2Output(ho)
    }
  }

  override fun serialize(obj: Any): ByteArray {
    return ByteArrayOutputStream(512).use {
      serialize(obj, it)
      it.toByteArray()
    }
  }

  override fun deserialize(inStream: InputStream): Any {
    var hi: Hessian2Input? = null
    try {
      hi = GoblinHessianFactory.INSTANCE.createHessian2Input(inStream)
      return hi.readObject()
    } finally {
      GoblinHessianFactory.INSTANCE.freeHessian2Input(hi)
    }
  }

  override fun deserialize(bs: ByteArray): Any {
    return ByteArrayInputStream(bs).use {
      deserialize(it)
    }
  }
}