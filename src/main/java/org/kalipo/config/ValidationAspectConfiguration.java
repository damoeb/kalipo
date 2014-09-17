package org.kalipo.config;

import org.kalipo.aop.ArgumentValidationAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Created by damoeb on 17.09.14.
 */
@Configuration
@EnableAspectJAutoProxy
public class ValidationAspectConfiguration {

    @Bean
    public ArgumentValidationAspect argumentValidationAspect() {
        return new ArgumentValidationAspect();
    }

}
