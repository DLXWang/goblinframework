package org.goblinframework.core.config

import java.lang.management.PlatformManagedObject

interface MappingLocationScannerMXBean : PlatformManagedObject {

  fun getConfigPath(): String?

}