package org.kalipo.service;

import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.Throttled;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Notice;
import org.kalipo.domain.Thread;
import org.kalipo.domain.User;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@KalipoExceptionHandler
public class CommentService {

    private static final int PAGE_SIZE = 5;
    private final Logger log = LoggerFactory.getLogger(CommentService.class);

    private static final Pattern FIND_USER_REFERENCES = Pattern.compile("@[a-z0-9]+");

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private ThreadRepository threadRepository;

    @Inject
    private ReputationService reputationService;

    @Inject
    private NoticeService noticeService;

    @Inject
    private UserService userService;

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

        Comment original = commentRepository.findOne(comment.getId());
        Asserts.isCurrentLogin(original.getAuthorId());

        Asserts.nullOrEqual(comment.getStatus(), original.getStatus(), "status");
        comment.setStatus(original.getStatus());

        Asserts.nullOrEqual(comment.getLikes(), original.getLikes(), "likes");
        comment.setLikes(original.getLikes());

        Asserts.nullOrEqual(comment.getDislikes(), original.getDislikes(), "dislikes");
        comment.setDislikes(original.getDislikes());

        Asserts.nullOrEqual(comment.getCreatedDate(), original.getCreatedDate(), "createdDate");
        comment.setCreatedDate(original.getCreatedDate());

        return save(comment, false);
    }

    @RolesAllowed(Privileges.REVIEW_COMMENT)
    @Throttled
    public Comment approve(String id) throws KalipoException {
        return approveOrReject(id, Comment.Status.APPROVED);
    }

    @RolesAllowed(Privileges.REVIEW_COMMENT)
    @Throttled
    public Comment reject(String id) throws KalipoException {
        return approveOrReject(id, Comment.Status.REJECTED);
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
        // author or mod can delete

        reputationService.punishDeletingComment(comment);

        commentRepository.delete(id);
    }

    // --

    private Comment approveOrReject(String id, Comment.Status newStatus) throws KalipoException {
        Comment comment = commentRepository.findOne(id);

        Asserts.isNotNull(comment, "id");

        comment.setStatus(newStatus);
        // save reviewer
        comment.setReviewerId(SecurityUtils.getCurrentLogin());

        comment = commentRepository.save(comment);

        if (newStatus == Comment.Status.APPROVED) {
            notifyMentionedUsers(comment);
            noticeService.notifyAsync(comment.getAuthorId(), Notice.Type.APPROVAL, comment.getId());

//            todo implement
//            User author = userService.findOne(comment.getAuthorId());
//            author.setApprovedCommentCount(author.getApprovedCommentCount() + 1);

        } else {
            noticeService.notifyAsync(comment.getAuthorId(), Notice.Type.DELETION, comment.getId());
        }

        return comment;
    }

    private Comment save(Comment comment, boolean isNew) throws KalipoException {

        Asserts.isNotNull(comment.getThreadId(), "threadId");

        Thread thread = threadRepository.findOne(comment.getThreadId());
        Asserts.isNotNull(thread, "threadId");
        Asserts.isNotReadOnly(thread);

        comment.setAuthorId(SecurityUtils.getCurrentLogin());

        User author = userService.findOne(SecurityUtils.getCurrentLogin());

        if (author.getApprovedCommentCount() < 5) {
            comment.setStatus(Comment.Status.PENDING);

        } else {
            comment.setStatus(Comment.Status.APPROVED);
        }

        comment = commentRepository.save(comment);

        if (isNew) {
            thread.setCommentCount(thread.getCommentCount() + 1);
            threadRepository.save(thread);

            notifyAuthorOfParent(comment);

        }

        if (comment.getStatus() == Comment.Status.APPROVED) {
            notifyMentionedUsers(comment);
        }

        return comment;
    }

    private void notifyAuthorOfParent(Comment comment) {
        if (comment.getParentId() != null) {
            Comment parent = commentRepository.findOne(comment.getParentId());
            if (parent != null) {
                noticeService.notifyAsync(parent.getAuthorId(), Notice.Type.REPLY, comment.getId());
            }
        }
    }

    private void notifyMentionedUsers(Comment comment) {

        // find mentioned usernames, starting with @ like @myname
        Matcher matcher = FIND_USER_REFERENCES.matcher(comment.getText());
        Set<String> uqLogins = new HashSet<>();
        while (matcher.find()) {
            String login = matcher.group();
            uqLogins.add(login);
        }

        for (String login : uqLogins) {
            // notify @login
            noticeService.notifyAsync(login, Notice.Type.MENTION, comment.getId());
        }
    }
}
