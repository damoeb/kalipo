package org.kalipo.repository;

import org.kalipo.domain.Nonce;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Nonce entity.
 */
public interface NonceRepository extends MongoRepository<Nonce, String> {

}

