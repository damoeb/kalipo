package org.kalipo.repository;

import org.joda.time.DateTime;
import org.kalipo.domain.Vote;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Spring Data MongoDB repository for the Vote entity.
 */
public interface VoteRepository extends MongoRepository<Vote, String> {

    @Query(value = "{'authorId': ?0, 'createdDate': {$gte: ?1, $lt: ?2}}", count = true)
    int countWithinDateRange(String currentLogin, DateTime from, DateTime to);

    @Query(value = "{'threadId': ?0, 'isLike' : true}", count = true)
    Integer countLikesOfThread(String threadId);

    List<Vote> findByAuthorId(String authorId, PageRequest pageable);
}
