package org.kalipo.repository;

import org.kalipo.domain.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the Report entity.
 */
public interface ReportRepository extends MongoRepository<Report, String> {

    List<Report> findByStatus(Report.Status status);
}
