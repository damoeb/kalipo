package org.kalipo.repository;

import org.kalipo.domain.ReputationModifier;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the ReputationModifier entity.
 */
public interface ReputationModifierRepository extends MongoRepository<ReputationModifier, String> {

    ReputationModifier findByType(ReputationModifier.Type type);
}
