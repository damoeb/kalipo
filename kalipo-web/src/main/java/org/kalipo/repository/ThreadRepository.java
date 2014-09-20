package org.kalipo.repository;

import org.kalipo.domain.Thread;
        import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Thread entity.
 */
public interface ThreadRepository extends MongoRepository<Thread, String> {

}
