package org.goblinframework.core.container;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.List;

final public class SpringBeanPostProcessorDelegator implements BeanPostProcessor {

  private final List<SpringContainerBeanPostProcessor> processors;

  public SpringBeanPostProcessorDelegator() {
    processors = SpringContainerManager.INSTANCE.getBeanPostProcessors();
  }

  @Override
  public Object postProcessBeforeInitialization(@NotNull Object bean, String beanName) throws BeansException {
    if (processors.isEmpty()) {
      return bean;
    }
    MutableObject<Object> reference = new MutableObject<>(bean);
    for (SpringContainerBeanPostProcessor processor : processors) {
      Object processed = processor.postProcessBeforeInitialization(reference.getValue(), beanName);
      if (processed == null) {
        return null;
      }
      reference.setValue(processed);
    }
    return reference.getValue();
  }

  @Override
  public Object postProcessAfterInitialization(@NotNull Object bean, String beanName) throws BeansException {
    if (processors.isEmpty()) {
      return bean;
    }
    MutableObject<Object> reference = new MutableObject<>(bean);
    for (SpringContainerBeanPostProcessor processor : processors) {
      Object processed = processor.postProcessAfterInitialization(reference.getValue(), beanName);
      if (processed == null) {
        return null;
      }
      reference.setValue(processed);
    }
    return reference.getValue();
  }
}
