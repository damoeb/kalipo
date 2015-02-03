package org.kalipo.service;

import org.joda.time.DateTime;
import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.Throttled;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Notice;
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

import javax.annotation.security.RolesAllowed;
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
    private ReputationService reputationService;

    @Inject
    private UserService userService;

    @Inject
    private NoticeService noticeService;

    @Inject
    private CommentRepository commentRepository;

    @RolesAllowed(Privileges.CREATE_VOTE)
    @Throttled
    public Vote create(@Valid Vote vote) throws KalipoException {

        Asserts.isNull(vote.getId(), "id");

        final String currentLogin = SecurityUtils.getCurrentLogin();
        final boolean isSuperMod = userService.isSuperMod(currentLogin);

        // -- Quota

        int count = voteRepository.countWithinDateRange(SecurityUtils.getCurrentLogin(), DateTime.now().minusDays(1), DateTime.now());
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

        reputationService.onCommentVoting(vote);

        vote.setAuthorId(SecurityUtils.getCurrentLogin());
        vote.setThreadId(comment.getThreadId());

//        todo replace by scheduled job, with delay to prevent bandwaggon effect

        if (vote.isLike()) {
            log.info(String.format("%s likes comment %s", SecurityUtils.getCurrentLogin(), comment.getId()));
            comment.setLikes(NumUtils.nullToZero(comment.getLikes()) + 1);
            noticeService.notifyAsync(comment.getAuthorId(), currentLogin, Notice.Type.LIKE, comment.getId());
        } else {
            log.info(String.format("%s dislikes comment %s", SecurityUtils.getCurrentLogin(), comment.getId()));
            comment.setDislikes(NumUtils.nullToZero(comment.getDislikes()) + 1);
            // don't notify about dislikes, it's bad for the motivation and feeds a troll
        }
        commentRepository.save(comment);

        BroadcastUtils.broadcast(vote);

        return voteRepository.save(vote);
    }

    @Async
    public Future<List<Vote>> getVotes(String userId, int pageNumber) {
        PageRequest pageable = new PageRequest(pageNumber, 10, Sort.Direction.DESC, "createdDate");
        // todo this does not work
        return new AsyncResult<>(voteRepository.findByAuthorId(userId, pageable));
    }

}
