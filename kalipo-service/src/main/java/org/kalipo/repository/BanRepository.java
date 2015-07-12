package org.kalipo.repository;

import org.kalipo.domain.Ban;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Ban entity.
 */
public interface BanRepository extends MongoRepository<Ban, String> {
    <V> V findBySiteId(String siteId, PageRequest pageable);
}
