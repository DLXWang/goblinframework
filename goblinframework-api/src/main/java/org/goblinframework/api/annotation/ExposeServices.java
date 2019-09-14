package org.goblinframework.api.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExposeServices {

  ExposeService[] value();

  boolean enable() default true;

}
