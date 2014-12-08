package org.kalipo.repository;

import org.kalipo.domain.Reputation;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Reputation entity.
 */
public interface ReputationRepository extends MongoRepository<Reputation, String> {

    Reputation findByType(Reputation.Type type);
}
