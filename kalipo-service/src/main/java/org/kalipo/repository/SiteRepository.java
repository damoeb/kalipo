package org.kalipo.repository;

import org.kalipo.domain.Site;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Site entity.
 */
public interface SiteRepository extends MongoRepository<Site, String> {
    Site findByName(String name);
}
