package org.kalipo.repository;

import org.kalipo.domain.Tag;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Tag entity.
 */
public interface TagRepository extends MongoRepository<Tag, String> {

}
