package org.kalipo.repository;

import org.kalipo.domain.ReputationDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the ReputationDefinition entity.
 */
public interface ReputationDefinitionRepository extends MongoRepository<ReputationDefinition, String> {

    ReputationDefinition findByType(ReputationDefinition.Type type);
}
