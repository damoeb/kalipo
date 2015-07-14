package org.kalipo.repository;

import org.kalipo.domain.Thread;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Spring Data MongoDB repository for the Thread entity.
 */
public interface ThreadRepository extends MongoRepository<Thread, String> {

    @Query(value = "{ uriHooks: { $in: [ ?0 ]}}")
    Thread findByUriHook(String sample);

    List<Thread> findByStatus(Thread.Status open, PageRequest request);
}
