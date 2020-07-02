package org.goblinframework.webmvc.listener

import org.goblinframework.core.system.GoblinSystem
import org.goblinframework.webmvc.container.WebappSpringContainer
import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent

open class ContextLoaderListener : org.springframework.web.context.ContextLoaderListener() {

  override fun determineContextClass(servletContext: ServletContext?): Class<*> {
    return WebappSpringContainer::class.java
  }

  override fun contextInitialized(event: ServletContextEvent) {
    GoblinSystem.install()
    super.contextInitialized(event)
  }

  override fun contextDestroyed(event: ServletContextEvent) {
    super.contextDestroyed(event)
    GoblinSystem.uninstall()
  }
}