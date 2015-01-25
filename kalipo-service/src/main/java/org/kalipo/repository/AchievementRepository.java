package org.kalipo.repository;

import org.kalipo.domain.Achievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the RepRevision entity.
 */
public interface AchievementRepository extends MongoRepository<Achievement, String> {

    Page<Achievement> findByUserId(String userId, Pageable pageable);
}
