package org.goblinframework.dao.mapping

import java.lang.management.PlatformManagedObject

interface EntityMappingMXBean : PlatformManagedObject {

  fun getIdFieldName(): String

  fun getRevisionFieldName(): String?

}