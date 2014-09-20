package org.kalipo.config.metrics;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.inject.Inject;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class JHipsterHealthIndicatorConfiguration {

    @Inject
    private JavaMailSenderImpl javaMailSender;

    @Inject
    private MongoTemplate mongoTemplate;

    @Bean
    public HealthIndicator dbHealthIndicator() {
        return new DatabaseHealthIndicator(mongoTemplate);
    }

    @Bean
    public HealthIndicator mailHealthIndicator() {
        return new JavaMailHealthIndicator(javaMailSender);
    }
}
