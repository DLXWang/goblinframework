package org.goblinframework.embedded.core.http

import org.junit.Assert.assertNotNull
import org.junit.Test

class HttpServletResponseAdapterTest {

  @Test
  fun getAdapter() {
    assertNotNull(HttpServletResponseAdapter.adapter)
  }
}