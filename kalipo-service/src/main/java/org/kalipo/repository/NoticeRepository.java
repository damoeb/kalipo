package org.kalipo.repository;

import org.kalipo.domain.Notice;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NoticeRepository extends MongoRepository<Notice, String> {

}
