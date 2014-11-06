package org.kalipo.service;

import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.Throttled;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Notice;
import org.kalipo.domain.Report;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.ReportRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
import org.kalipo.web.rest.KalipoException;
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
 * on approve: reputation + 5 for every reporter, reputation - 100 for author
 * on reject: reputation - 50 for author
 * on report: iff report count > 5 comment will become hidden until reviewed
 * todo: peer review reports of users with reputation >= author
 */
@Service
@KalipoExceptionHandler
public class ReportService {

    private final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Inject
    private ReportRepository reportRepository;

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private ReputationService reputationService;

    @Inject
    private NoticeService noticeService;

    @RolesAllowed(Privileges.CREATE_REPORT)
    @Throttled
    public Report create(Report report) throws KalipoException {

        Asserts.isNull(report.getId(), "id");
        Asserts.isNotNull(report.getCommentId(), "commentId");

        report.setStatus(Report.Status.PENDING);

        Comment comment = commentRepository.findOne(report.getCommentId());

        Asserts.isNotNull(comment, "commentId");

        report.setAuthorId(SecurityUtils.getCurrentLogin());
        report.setStatus(Report.Status.PENDING);
        report.setThreadId(comment.getThreadId());

        report = reportRepository.save(report);

        // todo replace by query
        comment.setReportedCount(comment.getReportedCount() + 1);

        // todo async
        if (comment.getReportedCount() == 1) {
            noticeService.notifyModsOfThread(comment.getThreadId(), report);
        }
        if (comment.getReportedCount() == 3) {
            comment.setHidden(true);
            noticeService.notifySuperMods(comment);
        }
        commentRepository.save(comment);

        return report;
    }

    @RolesAllowed(Privileges.CLOSE_REPORT)
    @Throttled
    public void approve(String id) throws KalipoException {
        approveOrReject(getPendingReport(id).setStatus(Report.Status.APPROVED));
    }

    @RolesAllowed(Privileges.CLOSE_REPORT)
    @Throttled
    public void reject(String id) throws KalipoException {
        approveOrReject(getPendingReport(id).setStatus(Report.Status.REJECTED));
    }

    // todo can be removed?
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
    public Future<Report> get(String id) throws KalipoException {
        return new AsyncResult<Report>(reportRepository.findOne(id));
    }

    /**
     * Delete a pending report
     *
     * @param id the report id
     * @throws org.kalipo.web.rest.KalipoException
     */
    // todo add RolesAllowed
    public void delete(String id) throws KalipoException {

        getPendingReport(id); // will fail if not pending or existing

        reportRepository.delete(id);
    }

    // --

    private void approveOrReject(Report report) throws KalipoException {

        report.setReviewerId(SecurityUtils.getCurrentLogin());

        Comment comment = commentRepository.findOne(report.getCommentId());

        if (report.getStatus() == Report.Status.APPROVED) {
            comment.setDeleted(true);
            commentRepository.save(comment);

            noticeService.notifyAsync(comment.getAuthorId(), Notice.Type.DELETION, comment.getId());

        } else {
            if (comment.getHidden()) {
                comment.setHidden(true);
                commentRepository.save(comment);
            }

            noticeService.notifyAsync(comment.getAuthorId(), Notice.Type.APPROVAL, comment.getId());
        }

        reputationService.onReportApprovalOrRejection(reportRepository.save(report));
    }

    private Report getPendingReport(String id) throws KalipoException {
        Report report = reportRepository.findOne(id);

        Asserts.isNotNull(report, "id");

        if (report.getStatus() != Report.Status.PENDING) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, "Report must be pending");
        }
        return report;
    }
}
