package org.kalipo.repository;

import org.kalipo.domain.Notice;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NoticeRepository extends MongoRepository<Notice, String> {

    List<Notice> findByRecipientId(String login, PageRequest pageable);
}
