package org.kalipo.repository;

import org.joda.time.DateTime;
import org.kalipo.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Spring Data MongoDB repository for the User entity.
 */
public interface UserRepository extends MongoRepository<User, String> {
    
    @Query("{activationKey: ?0}")
    User getUserByActivationKey(String activationKey);
    
    @Query("{activation_key: 'false', createdDate: {$gt: ?0}}")
    List<User> findNotActivatedUsersByCreationDateBefore(DateTime dateTime);

    @Query("{superMod: 'true'}")
    List<User> findSuperMods();

    @Query("{loginTries: {$gt: 0}, lastLoginTry: {$lt: ?0}}")
    List<User> findHavingLoginTries(DateTime dateTime);

    @Query(value = "{'recipientId': ?0, 'seen' : false}", count = true)
    int countUnseenOfUser(String login);
}
