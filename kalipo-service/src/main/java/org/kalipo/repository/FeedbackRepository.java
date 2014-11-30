package org.kalipo.repository;

import org.kalipo.domain.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Feedback entity.
 */
public interface FeedbackRepository extends MongoRepository<Feedback, String> {

}

