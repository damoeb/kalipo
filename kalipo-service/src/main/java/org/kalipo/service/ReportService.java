package org.kalipo.service;

import org.joda.time.DateTime;
import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.aop.Throttled;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Report;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.ReportRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.web.rest.KalipoRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Future;

@Service
@EnableArgumentValidation
public class ReportService {

    private final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Inject
    private ReportRepository reportRepository;

    @Inject
    private CommentRepository commentRepository;

    @RolesAllowed(Privileges.CREATE_REPORT)
    @Throttled
    public void create(Report report) throws KalipoRequestException {

        // todo id must not exist id

        report.setStatus(Report.Status.PENDING);

        save(report);
    }

    @RolesAllowed(Privileges.CREATE_REPORT)
    @Throttled
    public void update(Report report) throws KalipoRequestException {

        report.setStatus(Report.Status.PENDING);

        save(report);
    }

    private void save(Report report) throws KalipoRequestException {

        Comment comment = commentRepository.findOne(report.getCommentId());
        if (comment == null) {
            throw new KalipoRequestException(ErrorCode.INVALID_PARAMETER, "commentId");
        }

        report.setAuthorId(SecurityUtils.getCurrentLogin());
        report.setStatus(Report.Status.PENDING);

        report.setThreadId(comment.getThreadId());
        report.setCreatedDate(DateTime.now());

        reportRepository.save(report);
    }

    @Async
    public Future<List<Report>> getAll() {
        return new AsyncResult<>(reportRepository.findAll());
    }

    @Async
    public Future<Report> get(String id) throws KalipoRequestException {
        return new AsyncResult<Report>(reportRepository.findOne(id));
    }

    public void delete(String id) throws KalipoRequestException {
        reportRepository.delete(id);
    }

}
