package org.kalipo.service;

import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.RateLimit;
import org.kalipo.domain.*;
import org.kalipo.repository.AchievementRepository;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.ReputationModifierRepository;
import org.kalipo.repository.UserRepository;
import org.kalipo.security.Privileges;
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
public class ReputationModifierService {

    private final Logger log = LoggerFactory.getLogger(ReputationModifierService.class);

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private AchievementRepository achievementRepository;

    @Inject
    private ReputationModifierRepository reputationModifierRepository;

    /**
     * Create a reputation revision for a vote
     * on dislikes: reputation -1 of voter and -2 of author
     * on like: reputation +10 of author, probably reputation -1 of voter to hinder meat-puppet issue
     *
     * @param vote the vote
     * @param currentLogin the current login, since async
     * @throws KalipoException
     */
    @Async
    public void onCommentVoting(Vote vote, String currentLogin) throws KalipoException {

        Asserts.isNotNull(vote, "vote");
        Asserts.isNotNull(vote.getCommentId(), "commentId");
        Asserts.isNotNull(currentLogin, "login");
        vote.setAuthorId(currentLogin);

        final Comment comment = commentRepository.findOne(vote.getCommentId());
        Asserts.isNotNull(comment, "commentId");

        final String resourceRef = comment.getId();
        final String authorId = comment.getAuthorId();
        final String voterId = vote.getAuthorId();

        Achievement rvForAuthor, rvForVoter;

        if (vote.isLike()) {
            rvForAuthor = createRevision(authorId, resourceRef, ReputationModifier.Type.LIKE);
            rvForVoter = createRevision(voterId, resourceRef, ReputationModifier.Type.LIKED);
        } else {
            rvForAuthor = createRevision(authorId, resourceRef, ReputationModifier.Type.DISLIKE);
            rvForVoter = createRevision(voterId, resourceRef, ReputationModifier.Type.DISLIKED);
        }

        achievementRepository.save(rvForAuthor);
        achievementRepository.save(rvForVoter);

        updateUserReputation(rvForAuthor);
        updateUserReputation(rvForVoter);
    }

    @Async
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
            Achievement rvForAuthor = createRevision(authorId, resourceRef, ReputationModifier.Type.REPORT);
            achievementRepository.save(rvForAuthor);

            Achievement rvForReporter = createRevision(reporterId, resourceRef, ReputationModifier.Type.REPORTED);
            achievementRepository.save(rvForReporter);

            updateUserReputation(rvForAuthor);
            updateUserReputation(rvForReporter);

        } else {
            /**
             * report is invalid
             */
            if (report.isAbused()) {
                Achievement rvForReporter = createRevision(reporterId, resourceRef, ReputationModifier.Type.ABUSED_REPORT);
                achievementRepository.save(rvForReporter);

                updateUserReputation(rvForReporter);
            }
        }
    }

    /**
     * A newly created user receives a welcome reputation. The background of this is to establish user.reputation=0 as virtual death (the inability of actively joining the discussion)
     *
     * @param user the user
     */
    @Async
    public void onUserCreation(@Valid @NotNull User user) {
        Achievement rvForNewUser = createRevision(user.getLogin(), user.getLogin(), ReputationModifier.Type.WELCOME);
        achievementRepository.save(rvForNewUser);

        updateUserReputation(rvForNewUser);

    }

    /**
     * Punish comment deleting by Reputation.Type.RM_COMMENT
     *
     * @param comment the comment
     * @throws KalipoException
     */
    @Async
    public void onCommentDeletion(@Valid @NotNull Comment comment) throws KalipoException {
        Asserts.isNotNull(comment, "comment");

        Achievement rvForUser = createRevision(comment.getAuthorId(), comment.getId(), ReputationModifier.Type.RM_COMMENT);
        achievementRepository.save(rvForUser);

        updateUserReputation(rvForUser);
    }

    @RolesAllowed(Privileges.CREATE_PRIVILEGE)
    @RateLimit
    public void update(ReputationModifier reputationModifier) throws KalipoException {
        reputationModifierRepository.save(reputationModifier);
    }

    @Async
    public Future<ReputationModifier> get(String id) throws KalipoException {
        return new AsyncResult<>(reputationModifierRepository.findOne(id));
    }


    @Async
    public Future<List<ReputationModifier>> getAll() {
        return new AsyncResult<>(reputationModifierRepository.findAll());
    }

    // --

    private void updateUserReputation(Achievement revision) {
        User u = userRepository.findOne(revision.getUserId());
        ReputationModifier definition = reputationModifierRepository.findByType(revision.getType());

        log.info(String.format("%s gets %s reputation after %s (rev %s)", revision.getUserId(), definition.getReputation(), revision.getType(), revision.getId()));

        u.setReputation(u.getReputation() + definition.getReputation());
        userRepository.save(u);
    }

    private Achievement createRevision(String authorId, String resourceRef, ReputationModifier.Type type) {
        Achievement rv = new Achievement();
        rv.setUserId(authorId);
        rv.setResourceRef(resourceRef);
        rv.setType(type);
        return rv;
    }

}
