package org.goblinframework.core.module.spi

import org.goblinframework.api.common.Lifecycle
import org.goblinframework.api.common.Ordered

interface TimerEventGenerator : Lifecycle, Ordered {
}