package org.kalipo.aop;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Throttle concurrent access of a method
 * Created by damoeb on 22.09.14.
 */
@Component
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Throttled {
    int limit() default 1;
}
