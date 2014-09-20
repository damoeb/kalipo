package org.kalipo.repository;

import org.kalipo.domain.Vote;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Vote entity.
 */
public interface VoteRepository extends MongoRepository<Vote, String> {

}
