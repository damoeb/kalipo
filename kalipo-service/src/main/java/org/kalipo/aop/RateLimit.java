package org.kalipo.aop;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Limit rate for accessing a method
 * todo limit to what?
 * Created by damoeb on 22.09.14.
 */
@Component
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RateLimit {
}
