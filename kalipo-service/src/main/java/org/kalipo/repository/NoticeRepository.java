package org.kalipo.repository;

import org.kalipo.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NoticeRepository extends MongoRepository<Notice, String> {

    Page<Notice> findByRecipientId(String login, Pageable pageable);
}
