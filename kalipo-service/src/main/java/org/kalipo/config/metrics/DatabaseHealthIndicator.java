package org.kalipo.config.metrics;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * SpringBoot Actuator HealthIndicator check for the Database.
 */
public class DatabaseHealthIndicator extends AbstractHealthIndicator {
    
    private MongoTemplate mongoTemplate;

    
    public DatabaseHealthIndicator(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            if (mongoTemplate.getDb().getStats().ok()) {
                builder.up().withDetail("mongodb", "ok");
            } else {
                builder.down().withDetail("mongodb", "error");
            }
        } catch (Exception e) {
            builder.down(e);
        }
    }
}
