package org.kalipo.service;

import org.joda.time.DateTime;
import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.domain.Report;
import org.kalipo.repository.ReportRepository;
import org.kalipo.security.SecurityUtils;
import org.kalipo.web.rest.KalipoRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Future;

@Service
@EnableArgumentValidation
public class ReportService {

    private final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Inject
    private ReportRepository reportRepository;

    //    @RolesAllowed(Privileges.CREATE_REPORT)
    public void create(Report report) throws KalipoRequestException {

        // todo id must not exist id

        report.setAuthorId(SecurityUtils.getCurrentLogin());
        report.setStatus(Report.Status.PENDING);
        report.setThreadId(1l); // todo remove placeholder
        report.setCreatedDate(DateTime.now());

        reportRepository.save(report);
    }

    //    @RolesAllowed(Privileges.CREATE_REPORT)
    public void update(Report report) throws KalipoRequestException {

        report.setAuthorId(SecurityUtils.getCurrentLogin());
        report.setStatus(Report.Status.PENDING);
        report.setThreadId(1l); // todo remove placeholder

        reportRepository.save(report);
    }

    @Async
    public Future<List<Report>> getAll() {
        return new AsyncResult<>(reportRepository.findAll());
    }

    @Async
    public Future<Report> get(String id) throws KalipoRequestException {
        return new AsyncResult<>(reportRepository.findOne(id));
    }

    public void delete(String id) throws KalipoRequestException {
        reportRepository.delete(id);
    }

}
