package org.kalipo.service;

import org.joda.time.DateTime;
import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.RateLimit;
import org.kalipo.config.Constants;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Vote;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.VoteRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
import org.kalipo.service.util.BroadcastUtils;
import org.kalipo.service.util.NumUtils;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.Future;

@Service
@KalipoExceptionHandler
public class VoteService {

    private final Logger log = LoggerFactory.getLogger(VoteService.class);

    @Inject
    private VoteRepository voteRepository;

    @Inject
    private ReputationModifierService reputationModifierService;

    @Inject
    private UserService userService;

    @Inject
    private NotificationService notificationService;

    @Inject
    private CommentRepository commentRepository;

    @RateLimit
    public Vote create(@Valid Vote vote) throws KalipoException {

        Asserts.isNull(vote.getId(), "id");

        if(vote.isLike()) {
            Asserts.hasPrivilege(Privileges.VOTE_UP);
        } else {
            Asserts.hasPrivilege(Privileges.VOTE_DOWN);
        }

        final String currentLogin = SecurityUtils.getCurrentLogin();
        final boolean isSuperMod = userService.isSuperMod(currentLogin);

        // -- Quota

        int count = voteRepository.countWithinDateRange(currentLogin, DateTime.now().minusDays(1), DateTime.now());
        int dailyLimit = 100; // todo senseful quota
        if (count >= dailyLimit && !isSuperMod) {
            throw new KalipoException(ErrorCode.METHOD_REQUEST_LIMIT_REACHED, "daily vote quota is " + dailyLimit);
        }

        // --

        final Comment comment = commentRepository.findOne(vote.getCommentId());
        Asserts.isNotNull(comment, "commentId");

        // todo enable
//        if(currentLogin.equals(comment.getAuthorId())) {
//            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, "You can't vote on your comment");
//        }

        reputationModifierService.onCommentVoting(vote, currentLogin);

        vote.setAuthorId(SecurityUtils.getCurrentLogin());
        vote.setThreadId(comment.getThreadId());

//        todo replace by scheduled job, with delay to prevent bandwaggon effect

        if (vote.isLike()) {
            log.info(String.format("User '%s' likes comment %s", SecurityUtils.getCurrentLogin(), comment.getId()));
            comment.setLikes(NumUtils.nullToZero(comment.getLikes()) + 1);
            notificationService.announceCommentLiked(comment);
        } else {
            log.info(String.format("User '%s' dislikes comment %s", SecurityUtils.getCurrentLogin(), comment.getId()));
            comment.setDislikes(NumUtils.nullToZero(comment.getDislikes()) + 1);
            // don't notify about dislikes, it's bad for the motivation and feeds a troll
        }
        commentRepository.save(comment);

        BroadcastUtils.broadcast(comment.getThreadId(), BroadcastUtils.Type.VOTE, vote.anonymized());

        return voteRepository.save(vote);
    }

    @Async
    public Future<List<Vote>> getVotesWithPages(String userId, int pageNumber) {
        PageRequest pageable = new PageRequest(pageNumber, 10, Sort.Direction.DESC, Constants.PARAM_CREATED_DATE);
        // todo this does not work
        return new AsyncResult<>(voteRepository.findByAuthorId(userId, pageable));
    }

}
