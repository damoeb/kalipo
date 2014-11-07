package org.kalipo.service;

import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.Throttled;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Notice;
import org.kalipo.domain.Vote;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.VoteRepository;
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
    private NoticeService noticeService;

    @Inject
    private CommentRepository commentRepository;

    @RolesAllowed(Privileges.CREATE_VOTE)
    @Throttled
    public Vote create(@Valid Vote vote) throws KalipoException {

        Asserts.isNull(vote.getId(), "id");

        Comment comment = commentRepository.findOne(vote.getCommentId());
        Asserts.isNotNull(comment, "commentId");

        reputationService.onCommentVoting(vote);

        vote.setAuthorId(SecurityUtils.getCurrentLogin());

//        todo replace by scheduled job, with delay to prevent bandwaggon effect

        if (vote.getIsLike()) {
            log.info(String.format("%s likes comment %s", SecurityUtils.getCurrentLogin(), comment.getId()));
            comment.setLikes(comment.getLikes() + 1);
            noticeService.notifyAsync(comment.getAuthorId(), Notice.Type.LIKE, comment.getId());
        } else {
            log.info(String.format("%s dislikes comment %s", SecurityUtils.getCurrentLogin(), comment.getId()));
            comment.setDislikes(comment.getDislikes() + 1);
            // don't notify about dislikes, it's bad for the motivation and feeds a troll
        }
        commentRepository.save(comment);

        return voteRepository.save(vote);
    }

    @Async
    public Future<List<Vote>> getAll() {
        return new AsyncResult<>(voteRepository.findAll());
    }

    @Async
    public Future<Vote> get(String id) throws KalipoException {
        return new AsyncResult<>(voteRepository.findOne(id));
    }

    public void delete(String id) throws KalipoException {
        // todo remove
        voteRepository.delete(id);
    }
}
