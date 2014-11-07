package org.kalipo.repository;

import org.kalipo.domain.Privilege;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Privilege entity.
 */
public interface PrivilegeRepository extends MongoRepository<Privilege, String> {

    Privilege findByName(String name);
}
