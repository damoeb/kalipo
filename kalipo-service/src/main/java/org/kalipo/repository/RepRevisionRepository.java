package org.kalipo.repository;

import org.kalipo.domain.RepRevision;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the RepRevision entity.
 */
public interface RepRevisionRepository extends MongoRepository<RepRevision, String> {

    List<RepRevision> findByUserId(String userId, PageRequest pageable);
}
