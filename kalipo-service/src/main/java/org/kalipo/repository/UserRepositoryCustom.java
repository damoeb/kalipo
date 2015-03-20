package org.kalipo.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the User entity.
 */
@Repository
public class UserRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void setAllNoticesSeen(String userId) {
        Query query = new Query(Criteria.where("recipientId").is(userId).and("seen").is(false));

        Update update = new Update();
        update.set("seen", true);

        mongoTemplate.updateMulti(query, update, "T_NOTICE");
    }
}
