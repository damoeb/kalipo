package org.kalipo.aop;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validate all arguments against null
 * <p>
 * Created by damoeb on 17.09.14.
 */
@Component
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Deprecated
public @interface EnableArgumentValidation {
}
