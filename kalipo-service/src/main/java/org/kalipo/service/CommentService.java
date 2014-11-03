package org.kalipo.service;

import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.Throttled;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Thread;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.ThreadRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
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
import java.util.List;
import java.util.concurrent.Future;

@Service
@KalipoExceptionHandler
public class CommentService {

    private static final int PAGE_SIZE = 5;
    private final Logger log = LoggerFactory.getLogger(CommentService.class);

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private ThreadRepository threadRepository;

    @Inject
    private ReputationService reputationService;

    @RolesAllowed(Privileges.CREATE_COMMENT)
    @Throttled
    public Comment create(Comment comment) throws KalipoException {

        Asserts.isNotNull(comment, "comment");
        Asserts.isNull(comment.getId(), "id");

        return save(comment, true);
    }

    @RolesAllowed(Privileges.CREATE_COMMENT)
    @Throttled
    public Comment update(Comment comment) throws KalipoException {
        Asserts.isNotNull(comment, "comment");
        Asserts.isNotNull(comment.getId(), "id");

        Comment orgComment = commentRepository.findOne(comment.getId());
        Asserts.isCurrentLogin(orgComment.getAuthorId());
        // todo assert same status

        return save(comment, false);
    }

    @RolesAllowed(Privileges.REVIEW_COMMENT)
    @Throttled
    public Comment approve(String id) throws KalipoException {
        // todo implement
        return null;
    }

    @RolesAllowed(Privileges.REVIEW_COMMENT)
    @Throttled
    public Comment reject(String id) throws KalipoException {
        // todo implement
        return null;
    }

    private Comment save(Comment comment, boolean isNew) throws KalipoException {

        Asserts.isNotNull(comment.getThreadId(), "threadId");

        Thread thread = threadRepository.findOne(comment.getThreadId());
        Asserts.isNotNull(thread, "threadId");
        Asserts.isNotReadOnly(thread);

        if (isNew) {
            thread.setCommentCount(thread.getCommentCount() + 1);
            threadRepository.save(thread);
        }

        comment.setAuthorId(SecurityUtils.getCurrentLogin());

        // todo status is pending, as long the user has < 5 approved comments
        comment.setStatus(Comment.Status.APPROVED);
        return commentRepository.save(comment);
    }

    @Async
    public Future<List<Comment>> findAllUnderReview(final int pageNumber) {
        PageRequest pageable = new PageRequest(pageNumber, PAGE_SIZE, Sort.Direction.DESC, "createdDate");
        return new AsyncResult<>(commentRepository.findByStatus(Comment.Status.PENDING, pageable));
    }

    @Async
    public Future<Comment> get(String id) throws KalipoException {
        return new AsyncResult<>(commentRepository.findOne(id));
    }

    @Throttled
    public void delete(String id) throws KalipoException {

        Comment comment = commentRepository.findOne(id);
        Asserts.isNotNull(comment, "id");

        // todo check permissions
        reputationService.punishDeletingComment(comment);

        commentRepository.delete(id);
    }
}
