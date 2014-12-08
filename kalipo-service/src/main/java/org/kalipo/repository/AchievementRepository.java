package org.kalipo.repository;

import org.kalipo.domain.Achievement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the RepRevision entity.
 */
public interface AchievementRepository extends MongoRepository<Achievement, String> {

    List<Achievement> findByUserId(String userId, PageRequest pageable);
}
