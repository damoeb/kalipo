package org.kalipo.repository;

import org.kalipo.domain.Thread;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * Spring Data MongoDB repository for the Thread entity.
 */
public interface ThreadRepository extends MongoRepository<Thread, String> {

    @Query(value = "{ uriHooks: { $in: [ ?0 ]}}")
    Thread findByUriHook(String sample);
}
