package org.kalipo.repository;

import org.kalipo.domain.Privilege;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Spring Data MongoDB repository for the Privilege entity.
 */
public interface PrivilegeRepository extends MongoRepository<Privilege, String> {

    Privilege findByName(String name);

    @Query(value = "{'reputation': { $lte: ?0}}")
    List<Privilege> findByReputationLowerThanOrEqual(int reputation);
}
