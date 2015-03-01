package org.kalipo.config;

import org.kalipo.aop.ExceptionHandlerAspect;
import org.kalipo.aop.RateLimitAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Created by damoeb on 17.09.14.
 */
@Configuration
@EnableAspectJAutoProxy
public class AspectConfiguration {

    @Bean
    public ExceptionHandlerAspect exceptionHandlerAspect() {
        return new ExceptionHandlerAspect();
    }

    @Bean
    public RateLimitAspect methodThrottleAspect() {
        return new RateLimitAspect();
    }

}
