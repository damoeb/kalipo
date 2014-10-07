package org.kalipo.service;

import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.domain.*;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.RepRevisionRepository;
import org.kalipo.repository.ReputationDefinitionRepository;
import org.kalipo.repository.UserRepository;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
import org.kalipo.web.rest.KalipoRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Service
@EnableArgumentValidation
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
     * <p>
     * todo: on dislikes: reputation -1 of voter and -2 of author
     * todo: on like: reputation +10 of author, probably reputation -1 of voter to hinder meat-puppet issue
     */
    public void likeOrDislikeComment(Vote vote) throws KalipoRequestException {

        Asserts.isNotNull(vote, "vote");
        vote.setAuthorId(SecurityUtils.getCurrentLogin());

        final Comment comment = commentRepository.findOne(vote.getCommentId());
        Asserts.isNotNull(comment, "commentId");

        final String resourceRef = comment.getId();
        final String authorId = comment.getAuthorId();
        final String voterId = vote.getAuthorId();

        RepRevision rvForAuthor, rvForVoter;

        if (vote.getIsLike()) {
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

    public void approveOrRejectReport(Report report) throws KalipoRequestException {
        Asserts.isNotNull(report, "report");

        final Comment comment = commentRepository.findOne(report.getCommentId());
        Asserts.isNotNull(comment, "commentId");

        // todo resourceRef must not be null
        final String resourceRef = report.getId();
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
            // todo use abuse flag?
            if (report.isAbused()) {
                RepRevision rvForReporter = createRevision(reporterId, resourceRef, ReputationDefinition.Type.ABUSED_REPORT);
                repRevisionRepository.save(rvForReporter);

                updateUserReputation(rvForReporter);
            }
        }
    }

    public void initUser(@Valid @NotNull User user) {
        RepRevision rvForNewUser = createRevision(user.getLogin(), user.getLogin(), ReputationDefinition.Type.WELCOME);
        repRevisionRepository.save(rvForNewUser);

        updateUserReputation(rvForNewUser);

    }

    public void punishDeletingComment(@Valid @NotNull Comment comment) throws KalipoRequestException {
        Asserts.isNotNull(comment, "comment");

        RepRevision rvForUser = createRevision(comment.getAuthorId(), comment.getId(), ReputationDefinition.Type.RM_COMMENT);
        repRevisionRepository.save(rvForUser);

        updateUserReputation(rvForUser);
    }

    // --

    private void updateUserReputation(RepRevision revision) {
        User u = userRepository.findOne(revision.getUserId());
        u.setReputation(u.getReputation() + reputationDefinitionRepository.findByType(revision.getType()).getReputation());
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
