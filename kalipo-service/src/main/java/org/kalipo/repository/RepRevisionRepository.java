package org.kalipo.repository;

import org.kalipo.domain.RepRevision;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the RepRevision entity.
 */
public interface RepRevisionRepository extends MongoRepository<RepRevision, String> {

}
