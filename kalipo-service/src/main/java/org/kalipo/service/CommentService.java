package org.kalipo.service;

import org.apache.commons.lang3.BooleanUtils;
import org.joda.time.DateTime;
import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.Throttled;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Notice;
import org.kalipo.domain.Thread;
import org.kalipo.domain.User;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.ThreadRepository;
import org.kalipo.repository.UserRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

@Service
@KalipoExceptionHandler
public class CommentService {

    private static final int PAGE_SIZE = 51;
    private static final int MAX_LEVEL = 8;
    private final Logger log = LoggerFactory.getLogger(CommentService.class);

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private UserRepository userRepository;

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

        return save(comment, null);
    }

    @RolesAllowed(Privileges.CREATE_COMMENT)
    @Throttled
    public Comment update(Comment modified) throws KalipoException {
        Asserts.isNotNull(modified, "comment");
        Asserts.isNotNull(modified.getId(), "id");

        Comment original = commentRepository.findOne(modified.getId());
        Asserts.isCurrentLogin(original.getAuthorId());

        Asserts.nullOrEqual(modified.getStatus(), original.getStatus(), "status");
        modified.setStatus(original.getStatus());

        Asserts.nullOrEqual(modified.getLikes(), original.getLikes(), "likes");
        modified.setLikes(original.getLikes());

        Asserts.nullOrEqual(modified.getDislikes(), original.getDislikes(), "dislikes");
        modified.setDislikes(original.getDislikes());

        Asserts.nullOrEqual(modified.getCreatedDate(), original.getCreatedDate(), "createdDate");
        modified.setCreatedDate(original.getCreatedDate());

        return save(modified, original);
    }

    @RolesAllowed(Privileges.REVIEW_COMMENT)
    @Throttled
    public Comment approve(String id) throws KalipoException {

        Asserts.isNotNull(id, "id");
        Comment comment = commentRepository.findOne(id);
        Asserts.isNotNull(comment, "id");

        return approve(comment);
    }

    @RolesAllowed(Privileges.REVIEW_COMMENT)
    @Throttled
    public Comment approve(Comment comment) throws KalipoException {

        Asserts.isNotNull(comment, "id");

        if (comment.getStatus() == Comment.Status.APPROVED) {
            return comment;
        }

        if (comment.getStatus() != Comment.Status.PENDING) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, "must be pending to be approved");
        }

        final String currentLogin = SecurityUtils.getCurrentLogin();

        // -- Comment Count

        Thread thread = threadRepository.findOne(comment.getThreadId());

        Asserts.isNotNull(thread, "threadId");

        thread.setCommentCount(thread.getCommentCount() + 1);
        threadRepository.save(thread);

        noticeService.notifyAuthorOfParent(comment, currentLogin);

        // --

        log.info(String.format("%s approves comment %s ", currentLogin, comment.getId()));

        comment.setStatus(Comment.Status.APPROVED);
        comment.setReviewerId(currentLogin);

        comment = commentRepository.save(comment);

        noticeService.notifyMentionedUsers(comment, currentLogin);
        noticeService.notifyAsync(comment.getAuthorId(), currentLogin, Notice.Type.APPROVAL, comment.getId());

        return comment;
    }

    @RolesAllowed(Privileges.REVIEW_COMMENT)
    @Throttled
    public Comment reject(String id) throws KalipoException {

        Asserts.isNotNull(id, "id");
        Comment comment = commentRepository.findOne(id);
        Asserts.isNotNull(comment, "id");

        return reject(comment);
    }

    @RolesAllowed(Privileges.REVIEW_COMMENT)
    @Throttled
    public Comment reject(Comment comment) throws KalipoException {

        // todo test, this is new

        Asserts.isNotNull(comment, "id");

        if (comment.getStatus() == Comment.Status.DELETED) {
            return comment;
        }

        if (comment.getStatus() != Comment.Status.PENDING) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, "must be pending to be approved");
        }

        final String currentLogin = SecurityUtils.getCurrentLogin();

        noticeService.notifyAuthorOfParent(comment, currentLogin);

        // --

        log.info(String.format("%s rejects comment %s ", currentLogin, comment.getId()));

        comment.setStatus(Comment.Status.DELETED);
        comment.setReviewerId(currentLogin);

        comment = commentRepository.save(comment);

        return comment;
    }

    @Async
    public Future<List<Comment>> getPendingWithPages(final int pageNumber) {
        PageRequest pageable = new PageRequest(pageNumber, PAGE_SIZE, Sort.Direction.DESC, "createdDate");
        return new AsyncResult<>(commentRepository.findByStatus(Comment.Status.PENDING, pageable));
    }

    @Async
    public Future<List<Comment>> getPendingInThreadWithPages(String threadId, final int pageNumber) {
        PageRequest pageable = new PageRequest(pageNumber, PAGE_SIZE, Sort.Direction.DESC, "createdDate");
        return new AsyncResult<>(commentRepository.findByThreadIdAndStatusIn(threadId, Arrays.asList(Comment.Status.PENDING), pageable).getContent());
    }

    @Async
    public Future<Comment> get(String id) throws KalipoException {
        return new AsyncResult<Comment>(commentRepository.findOne(id));
    }

    @Throttled
    public void delete(String id) throws KalipoException {

        Asserts.isNotNull(id, "id");
        Comment comment = commentRepository.findOne(id);
        Asserts.isNotNull(comment, "id");

        delete(comment);
    }

    @Throttled
    public void delete(Comment comment) throws KalipoException {

        final String currentLogin = SecurityUtils.getCurrentLogin();

        Asserts.isNotNull(comment, "id");

        if (comment.getStatus() != Comment.Status.PENDING && comment.getStatus() != Comment.Status.APPROVED) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, "must be pending to be approved");
        }

        boolean isAuthor = comment.getAuthorId().equals(currentLogin);
        boolean isThreadMod = isThreadMod(comment.getThreadId(), currentLogin);
        boolean isSuperMod = userService.isSuperMod(currentLogin);

        if (!isAuthor && !isThreadMod && !isSuperMod) {
            throw new KalipoException(ErrorCode.PERMISSION_DENIED);
        }

        // punish author if third party is required to delete
        if (isSuperMod || isThreadMod) {
            if (comment.getStatus() == Comment.Status.PENDING) {
                log.info(String.format("Comment %s rejected by mod %s", comment.getId(), currentLogin));
            } else {
                log.info(String.format("Comment %s deleted by mod %s", comment.getId(), currentLogin));
            }

            // todo distinguish report approval vs pending (=learning) -> notification
            reputationService.onCommentDeletion(comment);
            // todo notification will encourage trolls?
            noticeService.notifyAsync(comment.getAuthorId(), currentLogin, Notice.Type.DELETION, comment.getId());
        } else {
            log.info(String.format("Comment %s deleted by owner %s", comment.getId(), currentLogin));
        }

        Long replies = commentRepository.countReplies(comment.getId());

        // 1. delete if no replies
        // 2. clear and leave replies
        if (replies > 0) {
            // empty comment
            log.info(String.format("Comment %s is blanked out due to %s replies", comment.getId(), replies));
            comment.setStatus(Comment.Status.DELETED); // todo read deleted comments to during load
            comment.setAuthorId("");
            comment.setText("");
            commentRepository.save(comment);

        } else {
            log.info(String.format("Comment %s is deleted", comment.getId()));
            commentRepository.delete(comment);
        }

        // todo unsure if we need a ban system
        User author = userRepository.findOne(comment.getAuthorId());
        author.setStrikes(author.getStrikes() + 1);

        if (author.getStrikes() > 2) {
//            author.setStrikes(0);
//            author.setBanned(true);
//            author.setBanCount(author.getBanCount() + 1);
//            DateTime bannedUntilDate = DateTime.now().plusDays(30 * author.getBanCount());
//            author.setBannedUntilDate(bannedUntilDate);
//            log.info("User {} is banned until ", author.getLogin(), bannedUntilDate);
            noticeService.notifySuperModsOfFraudulentUser(author, currentLogin);
        }

        userRepository.save(author);
    }

    // --

    private boolean isThreadMod(String threadId, String currentLogin) {
        return threadRepository.findOne(threadId).getModIds().contains(currentLogin);
    }

    private Comment save(Comment dirty, Comment original) throws KalipoException {

        final boolean isNew = original == null;
        final String currentLogin = SecurityUtils.getCurrentLogin();
        final boolean isSuperMod = userService.isSuperMod(currentLogin);

        Asserts.isNotNull(dirty.getThreadId(), "threadId");

        // -- Quota
        int count = commentRepository.countWithinDateRange(SecurityUtils.getCurrentLogin(), DateTime.now().minusDays(1), DateTime.now());
        int dailyLimit = 100; // todo senseful quota
        if (count >= dailyLimit && !isSuperMod) {
            throw new KalipoException(ErrorCode.METHOD_REQUEST_LIMIT_REACHED, "daily comment quota is " + dailyLimit);
        }

        // -- Display name

        if (BooleanUtils.isTrue(dirty.getAnonymous())) {
            dirty.setDisplayName(null);

        } else {
            dirty.setDisplayName(currentLogin);
        }

        // --

        Comment parent = null;
        // reply only to approved comments
        if (isNew) {

            if (dirty.getParentId() == null) {
                dirty.setLevel(0);
            } else {
                parent = commentRepository.findOne(dirty.getParentId());
                if (parent == null) {
                    throw new KalipoException(ErrorCode.INVALID_PARAMETER, "parentId");
                }

                if (parent.getLevel() + 1 > MAX_LEVEL) {
                    parent = commentRepository.findOne(parent.getParentId());
                }

                dirty.setLevel(parent.getLevel() + 1);

                if (parent.getStatus() != Comment.Status.APPROVED) {
                    throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, "Invalid status of parent. It is not approved yet");
                }
            }
        }

        final Thread thread = threadRepository.findOne(dirty.getThreadId());
        Asserts.isNotNull(thread, "threadId");
        Asserts.isNotReadOnly(thread);

        dirty.setAuthorId(currentLogin);
        dirty.setFingerprint(getFingerprint(parent, thread));

        // todo this part should be async. A separate process analyzes the comment and decides whether it is approved/review-required/spam
        final boolean isMod = thread.getModIds().contains(currentLogin);

        if (isMod || isSuperMod || commentRepository.getApprovedCommentCountOfUser(currentLogin) > 4) {
            dirty.setStatus(Comment.Status.APPROVED);
            log.info(String.format("%s creates comment %s ", currentLogin, dirty.toString()));
        } else {
            dirty.setStatus(Comment.Status.PENDING);
            log.info(String.format("%s creates pending comment %s ", currentLogin, dirty.toString()));
        }

        assignSticky(dirty, original, isNew, isMod, isSuperMod);

        // --

        dirty = commentRepository.save(dirty);

        if (dirty.getStatus() == Comment.Status.PENDING) {
            noticeService.notifyModsOfThread(thread, dirty, currentLogin);
        } else {

            if (isNew) {
                thread.setCommentCount(thread.getCommentCount() + 1);

                threadRepository.save(thread);

                noticeService.notifyAuthorOfParent(dirty, currentLogin);
            }
        }

        noticeService.notifyMentionedUsers(dirty, currentLogin);

        return dirty;
    }

    private String getFingerprint(Comment parent, Thread thread) {
        final String parentFp = parent == null ? "" : parent.getFingerprint();
        final int max = 99999;
        return parentFp + String.format("%05d", max - thread.getCommentCount());
    }

    private void assignSticky(Comment comment, Comment original, boolean isNew, boolean isMod, boolean isSuperMod) throws KalipoException {
        if (isNew) {

            // only mods may set sticky = true
            if (comment.getSticky() != null && comment.getSticky() && !(isMod || isSuperMod)) {
                throw new KalipoException(ErrorCode.PERMISSION_DENIED, "You may not set sticky flag");
            }

        } else {

            // only mods may change flag
            if (!(isMod || isSuperMod)) {
                Asserts.nullOrEqual(comment.getSticky(), original.getSticky(), "sticky");
//                throw new KalipoException(ErrorCode.PERMISSION_DENIED, "You may not alter sticky flag");
            }

            comment.setSticky(original.getSticky());
        }
    }
}
