package org.kalipo.repository;

import org.kalipo.domain.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Report entity.
 */
public interface ReportRepository extends MongoRepository<Report, String> {

    Report findByCommentIdAndAuthorId(String commentId, String authorId);

    Page<Report> findByThreadIdAndStatus(String threadId, Report.Status status, Pageable pageable);
}
