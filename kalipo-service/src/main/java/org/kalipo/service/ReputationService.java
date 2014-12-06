package org.kalipo.service;

import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.Throttled;
import org.kalipo.domain.*;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.RepRevisionRepository;
import org.kalipo.repository.ReputationDefinitionRepository;
import org.kalipo.repository.UserRepository;
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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.Future;

@Service
@KalipoExceptionHandler
public class ReputationService {

    private final Logger log = LoggerFactory.getLogger(ReputationService.class);

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private RepRevisionRepository repRevisionRepository;

    @Inject
    private ReputationDefinitionRepository reputationDefinitionRepository;

    /**
     * Create a reputation revision for a vote
     * on dislikes: reputation -1 of voter and -2 of author
     * on like: reputation +10 of author, probably reputation -1 of voter to hinder meat-puppet issue
     * <p>
     */
    // todo async
    public void onCommentVoting(Vote vote) throws KalipoException {

        Asserts.isNotNull(vote, "vote");
        Asserts.isNotNull(vote.getCommentId(), "commentId");
        vote.setAuthorId(SecurityUtils.getCurrentLogin());

        final Comment comment = commentRepository.findOne(vote.getCommentId());
        Asserts.isNotNull(comment, "commentId");

        final String resourceRef = comment.getId();
        final String authorId = comment.getAuthorId();
        final String voterId = vote.getAuthorId();

        RepRevision rvForAuthor, rvForVoter;

        if (vote.isLike()) {
            rvForAuthor = createRevision(authorId, resourceRef, ReputationDefinition.Type.LIKE);
            rvForVoter = createRevision(voterId, resourceRef, ReputationDefinition.Type.LIKED);
        } else {
            rvForAuthor = createRevision(authorId, resourceRef, ReputationDefinition.Type.DISLIKE);
            rvForVoter = createRevision(voterId, resourceRef, ReputationDefinition.Type.DISLIKED);
        }

        repRevisionRepository.save(rvForAuthor);
        repRevisionRepository.save(rvForVoter);

        updateUserReputation(rvForAuthor);
        updateUserReputation(rvForVoter);
    }

    // todo async
    public void onReportApprovalOrRejection(Report report) throws KalipoException {
        Asserts.isNotNull(report, "report");

        final Comment comment = commentRepository.findOne(report.getCommentId());
        Asserts.isNotNull(comment, "commentId");

        final String resourceRef = report.getId();
        Asserts.isNotNull(resourceRef, "id");

        final String reporterId = report.getAuthorId();
        final String authorId = comment.getAuthorId();

        if (report.getStatus() == Report.Status.APPROVED) {
            /**
             * report is ok, comment is rightly flagged
             */
            RepRevision rvForAuthor = createRevision(authorId, resourceRef, ReputationDefinition.Type.REPORT);
            repRevisionRepository.save(rvForAuthor);

            RepRevision rvForReporter = createRevision(reporterId, resourceRef, ReputationDefinition.Type.REPORTED);
            repRevisionRepository.save(rvForReporter);

            updateUserReputation(rvForAuthor);
            updateUserReputation(rvForReporter);

        } else {
            /**
             * report is invalid
             */
            if (report.isAbused()) {
                RepRevision rvForReporter = createRevision(reporterId, resourceRef, ReputationDefinition.Type.ABUSED_REPORT);
                repRevisionRepository.save(rvForReporter);

                updateUserReputation(rvForReporter);
            }
        }
    }

    /**
     * A newly created user receives a welcome reputation. The background of this is to establish user.reputation=0 as virtual death (the inability of actively joining the discussion)
     *
     * @param user the user
     */
    // todo async
    public void onUserCreation(@Valid @NotNull User user) {
        RepRevision rvForNewUser = createRevision(user.getLogin(), user.getLogin(), ReputationDefinition.Type.WELCOME);
        repRevisionRepository.save(rvForNewUser);

        updateUserReputation(rvForNewUser);

    }

    /**
     * Punish comment deleting by ReputationDefinition.Type.RM_COMMENT
     *
     * @param comment the comment
     * @throws KalipoException
     */
    // todo async
    public void onCommentDeletion(@Valid @NotNull Comment comment) throws KalipoException {
        Asserts.isNotNull(comment, "comment");

        RepRevision rvForUser = createRevision(comment.getAuthorId(), comment.getId(), ReputationDefinition.Type.RM_COMMENT);
        repRevisionRepository.save(rvForUser);

        updateUserReputation(rvForUser);
    }

    @RolesAllowed(Privileges.CREATE_PRIVILEGE)
    @Throttled
    public void update(ReputationDefinition reputationDefinition) throws KalipoException {
        reputationDefinitionRepository.save(reputationDefinition);
    }

    @Async
    public Future<ReputationDefinition> get(String id) throws KalipoException {
        return new AsyncResult<>(reputationDefinitionRepository.findOne(id));
    }


    @Async
    public Future<List<ReputationDefinition>> getAll() {
        return new AsyncResult<>(reputationDefinitionRepository.findAll());
    }

    // --

    private void updateUserReputation(RepRevision revision) {
        User u = userRepository.findOne(revision.getUserId());
        ReputationDefinition definition = reputationDefinitionRepository.findByType(revision.getType());

        log.info(String.format("%s gets %s reputation after %s (rev %s)", revision.getUserId(), definition.getReputation(), revision.getType(), revision.getId()));

        u.setReputation(u.getReputation() + definition.getReputation());
        userRepository.save(u);
    }

    private RepRevision createRevision(String authorId, String resourceRef, ReputationDefinition.Type type) {
        RepRevision rv = new RepRevision();
        rv.setUserId(authorId);
        rv.setResourceRef(resourceRef);
        rv.setType(type);
        return rv;
    }

}
