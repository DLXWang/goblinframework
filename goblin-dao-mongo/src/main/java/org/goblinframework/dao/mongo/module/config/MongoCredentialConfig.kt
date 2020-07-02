package org.goblinframework.dao.mongo.module.config

import org.goblinframework.core.service.GoblinManagedBean
import org.goblinframework.core.service.GoblinManagedObject

@GoblinManagedBean(type = "MongoDao")
class MongoCredentialConfig internal constructor(val mapper: MongoCredentialConfigMapper)
  : GoblinManagedObject(), MongoCredentialConfigMXBean {

  override fun getDatabase(): String {
    return mapper.database!!
  }

  override fun getUsername(): String {
    return mapper.username!!
  }

  override fun getPassword(): String {
    throw UnsupportedOperationException()
  }
}