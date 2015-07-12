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

        Achievement achievementForAuthor, achievementForVoter;

        if (vote.isLike()) {
            achievementForAuthor = createAchievement(authorId, resourceRef, ReputationModifier.Type.LIKE);
            achievementForVoter = createAchievement(voterId, resourceRef, ReputationModifier.Type.LIKED);
        } else {
            achievementForAuthor = createAchievement(authorId, resourceRef, ReputationModifier.Type.DISLIKE);
            achievementForVoter = createAchievement(voterId, resourceRef, ReputationModifier.Type.DISLIKED);
        }

        achievementRepository.save(achievementForAuthor);
        achievementRepository.save(achievementForVoter);

        updateUserReputation(achievementForAuthor);
        updateUserReputation(achievementForVoter);
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
            Achievement rvForAuthor = createAchievement(authorId, resourceRef, ReputationModifier.Type.REPORT);
            achievementRepository.save(rvForAuthor);

            Achievement rvForReporter = createAchievement(reporterId, resourceRef, ReputationModifier.Type.REPORTED);
            achievementRepository.save(rvForReporter);

            updateUserReputation(rvForAuthor);
            updateUserReputation(rvForReporter);

        } else {
            /**
             * report is rejected
             */
            if (report.isAbused()) {
                Achievement achievementForReporter = createAchievement(reporterId, resourceRef, ReputationModifier.Type.ABUSED_REPORT);
                achievementRepository.save(achievementForReporter);

                updateUserReputation(achievementForReporter);
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
        Achievement achievementForNewUser = createAchievement(user.getLogin(), user.getLogin(), ReputationModifier.Type.WELCOME);
        achievementRepository.save(achievementForNewUser);

        updateUserReputation(achievementForNewUser);
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

        Achievement achievementForUser = createAchievement(comment.getAuthorId(), comment.getId(), ReputationModifier.Type.RM_COMMENT);
        achievementRepository.save(achievementForUser);

        updateUserReputation(achievementForUser);
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

    private void updateUserReputation(Achievement achievement) {
        User u = userRepository.findOne(achievement.getUserId());
        ReputationModifier modifier = reputationModifierRepository.findByType(achievement.getType());

        u.setReputation(u.getReputation() + modifier.getReputation());
        if (modifier.getReputation() < 0) {
            log.info(String.format("User '%s' reputation decreases to %s on %s", achievement.getUserId(), u.getReputation(), achievement.getType()));
        } else {
            log.info(String.format("User '%s' reputation increases to %s on %s", achievement.getUserId(), u.getReputation(), achievement.getType()));
        }

        userRepository.save(u);
    }

    private Achievement createAchievement(String authorId, String resourceRef, ReputationModifier.Type type) {
        Achievement achievement = new Achievement();
        achievement.setUserId(authorId);
        achievement.setResourceRef(resourceRef);
        achievement.setType(type);
        return achievement;
    }

}
