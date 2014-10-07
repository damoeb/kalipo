package org.kalipo.service;

import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.aop.Throttled;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Report;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.ReportRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
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

/**
 * todo: on approve: reputation + 5 for every reporter, reputation - 100 for author
 * todo: on reject: reputation - 50 for author
 * todo: on report: iff count > 5 comment will become deleted until peer review
 * todo: peer review reports of users with reputation >= author
 * todo: support updates for approve, reject
 */
@Service
@EnableArgumentValidation
public class ReportService {

    private final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Inject
    private ReportRepository reportRepository;

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private ReputationService reputationService;


    @RolesAllowed(Privileges.CREATE_REPORT)
    @Throttled
    public void create(Report report) throws KalipoRequestException {

        Asserts.isNull(report.getId(), "id");

        report.setStatus(Report.Status.PENDING);

        Comment comment = commentRepository.findOne(report.getCommentId());

        Asserts.isNotNull(comment, "commentId");

        report.setAuthorId(SecurityUtils.getCurrentLogin());
        report.setStatus(Report.Status.PENDING);
        report.setThreadId(comment.getThreadId());

        reportRepository.save(report);
    }

    @RolesAllowed(Privileges.CLOSE_REPORT)
    @Throttled
    public void approve(String id) throws KalipoRequestException {
        approveOrReject(getPendingReport(id).setStatus(Report.Status.APPROVED));
    }

    @RolesAllowed(Privileges.CLOSE_REPORT)
    @Throttled
    public void reject(String id) throws KalipoRequestException {
        approveOrReject(getPendingReport(id).setStatus(Report.Status.REJECTED));
    }

    // todo add RolesAllowed
    @Async
    public Future<List<Report>> getAll() {
        return new AsyncResult<>(reportRepository.findAll());
    }

    // todo add RolesAllowed
    @Async
    public Future<List<Report>> getAllPending() {
        return new AsyncResult<>(reportRepository.findByStatus(Report.Status.PENDING));
    }

    @Async
    public Future<Report> get(String id) throws KalipoRequestException {
        return new AsyncResult<Report>(reportRepository.findOne(id));
    }

    /**
     * Delete a pending report
     *
     * @param id the report id
     * @throws KalipoRequestException
     */
    public void delete(String id) throws KalipoRequestException {

        getPendingReport(id); // will fail if not pending or existing

        reportRepository.delete(id);
    }

    // --

    private void approveOrReject(Report report) throws KalipoRequestException {
        reputationService.approveOrRejectReport(report);

        report.setReviewerId(SecurityUtils.getCurrentLogin());

        reportRepository.save(report);
    }

    private Report getPendingReport(String id) throws KalipoRequestException {
        Report report = reportRepository.findOne(id);

        Asserts.isNotNull(report, "id");

        if (report.getStatus() != Report.Status.PENDING) {
            throw new KalipoRequestException(ErrorCode.CONSTRAINT_VIOLATED, "Report must be pending");
        }
        return report;
    }
}
