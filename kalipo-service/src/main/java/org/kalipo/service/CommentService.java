package org.kalipo.service;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.RateLimit;
import org.kalipo.config.Constants;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Notification;
import org.kalipo.domain.Site;
import org.kalipo.domain.Thread;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.SiteRepository;
import org.kalipo.repository.ThreadRepository;
import org.kalipo.repository.UserRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
import org.kalipo.service.util.BroadcastUtils;
import org.kalipo.service.util.NumUtils;
import org.kalipo.web.filter.AnonUtil;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.util.concurrent.Future;

@Service
@KalipoExceptionHandler
public class CommentService {

    private final Logger log = LoggerFactory.getLogger(CommentService.class);

    private static final int MAX_LEVEL = 8;

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ThreadRepository threadRepository;

    @Inject
    private SiteRepository siteRepository;

    @Inject
    private ReputationModifierService reputationModifierService;

    @Inject
    private NotificationService notificationService;

    @Inject
    private UserService userService;

    @Inject
    private BanService banService;

    @Inject
    private ThreadService threadService;

    @Inject
    private MarkupService markupService;

    @RateLimit
    public Comment create(Comment comment) throws KalipoException {

        Asserts.isNotNull(comment, "comment");
        Asserts.isNull(comment.getId(), "id");

        if (comment.getParentId() == null) {
            Asserts.hasPrivilege(Privileges.CREATE_COMMENT_SOLO);
        } else {
            Asserts.hasPrivilege(Privileges.CREATE_COMMENT_REPLY);
        }

        return save(comment, null);
    }

    @RolesAllowed(Privileges.EDIT_COMMENT)
    @RateLimit
    public Comment update(Comment modified) throws KalipoException {
        Asserts.isNotNull(modified, "comment");
        Asserts.isNotNull(modified.getId(), "id");

        Comment original = commentRepository.findOne(modified.getId());
        Asserts.isCurrentLogin(original.getAuthorId());

        Asserts.nullOrEqual(modified.getStatus(), original.getStatus(), "status");
        modified.setStatus(original.getStatus());

        Asserts.nullOrEqual(modified.getParentId(), original.getParentId(), "parentId");
        modified.setParentId(original.getParentId());

        Asserts.nullOrEqual(modified.getLikes(), original.getLikes(), "likes");
        modified.setLikes(original.getLikes());

        Asserts.nullOrEqual(modified.getDislikes(), original.getDislikes(), "dislikes");
        modified.setDislikes(original.getDislikes());

        Asserts.nullOrEqual(modified.getCreatedDate(), original.getCreatedDate(), Constants.PARAM_CREATED_DATE);
        modified.setCreatedDate(original.getCreatedDate());

        return save(modified, original);
    }

    @RolesAllowed(Privileges.REVIEW_COMMENT)
    @RateLimit
    public Comment approve(String id) throws KalipoException {

        Asserts.isNotNull(id, "id");
        Comment comment = commentRepository.findOne(id);
        Asserts.isNotNull(comment, "id");

        return approve(comment);
    }

    @RolesAllowed(Privileges.REVIEW_COMMENT)
    @RateLimit
    public Comment approve(Comment comment) throws KalipoException {

        Asserts.isNotNull(comment, "id");

        if (comment.getStatus() == Comment.Status.APPROVED) {
            return comment;
        }

        if (comment.getStatus() != Comment.Status.PENDING) {
            throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, "must be pending to be approved");
        }

        final String currentLogin = SecurityUtils.getCurrentLogin();

        // todo add status PRE_APPROVE to let CommentAgent do the approval, do handle notifications,

        // -- Comment Count

        Thread thread = threadRepository.findOne(comment.getThreadId());

        Asserts.isNotNull(thread, "threadId");

        thread.setCommentCount(thread.getCommentCount() + 1);
        threadRepository.save(thread);

        // --

        log.info(String.format("%s approves comment %s ", currentLogin, comment.getId()));

        comment.setStatus(Comment.Status.APPROVED);
        comment.setReviewerId(currentLogin);

        comment = commentRepository.save(comment);

        notificationService.notifyMentionedUsers(comment, currentLogin);
        notificationService.notifyAsync(comment.getAuthorId(), currentLogin, Notification.Type.APPROVAL, comment.getId());

        return comment;
    }

    @RolesAllowed(Privileges.REVIEW_COMMENT)
    @RateLimit
    public Comment reject(String id) throws KalipoException {

        Asserts.isNotNull(id, "id");
        Comment comment = commentRepository.findOne(id);
        Asserts.isNotNull(comment, "id");

        return reject(comment);
    }

    @RolesAllowed(Privileges.REVIEW_COMMENT)
    @RateLimit
    public Comment spam(String id) throws KalipoException {

        Asserts.isNotNull(id, "id");
        Comment comment = commentRepository.findOne(id);
        Asserts.isNotNull(comment, "id");

        comment.setStatus(Comment.Status.SPAM);
        BroadcastUtils.broadcast(BroadcastUtils.Type.COMMENT_DELETED, comment.anonymized());

        return comment;
    }

    @RolesAllowed(Privileges.REVIEW_COMMENT)
    @RateLimit
    public Comment deleteAndBan(String id) throws KalipoException {

        Asserts.isNotNull(id, "id");
        Comment comment = commentRepository.findOne(id);
        Asserts.isNotNull(comment, "id");

        delete(comment);

        banService.banUser(comment.getAuthorId(), comment.getThreadId());

        return comment;
    }

    @RolesAllowed(Privileges.REVIEW_COMMENT)
    @RateLimit
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

        notificationService.notifyAuthorOfParent(comment, currentLogin);

        // --

        log.info(String.format("%s rejects comment %s ", currentLogin, comment.getId()));

        comment.setStatus(Comment.Status.DELETED);
        comment.setReviewerId(currentLogin);

        comment = commentRepository.save(comment);

        return comment;
    }

    @Async
    public Future<Comment> get(String id) throws KalipoException {
        return new AsyncResult<Comment>(commentRepository.findOne(id));
    }

    @RateLimit
    public void delete(String id) throws KalipoException {

        Asserts.isNotNull(id, "id");
        Comment comment = commentRepository.findOne(id);
        Asserts.isNotNull(comment, "id");

        delete(comment);
    }

    @RateLimit
    public void delete(Comment comment) throws KalipoException {

        final String currentLogin = SecurityUtils.getCurrentLogin();

        Asserts.isNotNull(comment, "id");

        boolean isAuthor = comment.getAuthorId().equals(currentLogin);
        Thread thread = threadRepository.findOne(comment.getThreadId());
        Asserts.isNotNull(thread, "threadId");
        boolean isSiteMod = isSiteMod(thread, currentLogin);
        boolean isSuperMod = userService.isSuperMod(currentLogin);

        if (!isAuthor && !isSiteMod && !isSuperMod) {
            throw new KalipoException(ErrorCode.PERMISSION_DENIED);
        }

        // punish author if third party is required to delete
        if (isSuperMod || isSiteMod) {
            if (comment.getStatus() == Comment.Status.PENDING) {
                log.info(String.format("Mod '%s' rejects comment %s", currentLogin, comment.getId()));
            } else {
                log.info(String.format("Mod '%s' deletes %s", currentLogin, comment.getId()));
            }

            // todo distinguish report approval vs pending (=learning) -> notification
            reputationModifierService.onCommentDeletion(comment);
            // todo notification will encourage trolls?
            notificationService.notifyAsync(comment.getAuthorId(), currentLogin, Notification.Type.DELETION, comment.getId());
        } else {
            log.info(String.format("Comment owner '%s' deletes comment %s", currentLogin, comment.getId()));
        }

        Long replies = commentRepository.countReplies(comment.getId());

        // 1. delete if no replies
        // 2. clear and leave replies
        if (replies > 0) {
            // empty comment
            log.info(String.format("Comment %s is blanked out due to %s replies", comment.getId(), replies));
            comment.setStatus(Comment.Status.DELETED);
            comment.setDisplayName("");
            comment.setBody("");
            comment.setBodyHtml("");
            comment.setLikes(0);
            comment.setDislikes(0);
            comment.setInfluence(0d);
            commentRepository.save(comment);

        } else {
            log.info(String.format("Comment %s is deleted", comment.getId()));
            commentRepository.delete(comment);
        }

        BroadcastUtils.broadcast(BroadcastUtils.Type.COMMENT_DELETED, comment.anonymized());
    }


    // --

    private boolean isSiteMod(Thread thread, String currentLogin) {
        Site site = siteRepository.findOne(thread.getSiteId());
        return site.getModeratorIds().contains(currentLogin);
    }

    private Comment save(Comment dirty, Comment original) throws KalipoException {

        final boolean isNew = original == null;
        final String currentLogin = SecurityUtils.getCurrentLogin();
        final boolean isSuperMod = userService.isSuperMod(currentLogin);

        Asserts.isNotNull(dirty.getThreadId(), "threadId");

        // -- Quota
        int count = commentRepository.countWithinDateRange(SecurityUtils.getCurrentLogin(), DateTime.now().minusDays(1), DateTime.now());
        int dailyLimit = 100; // todo senseful quota, centralize conf params, depending on user level?
        if (count >= dailyLimit && !isSuperMod) {
            // todo send mail
            throw new KalipoException(ErrorCode.METHOD_REQUEST_LIMIT_REACHED, "daily comment quota is " + dailyLimit);
        }

        // -- Display name

        if (BooleanUtils.isTrue(dirty.getAnonymous())) {
            dirty.setDisplayName(null);

        } else {
            dirty.setDisplayName(currentLogin);
        }

        // --

        final Thread thread = threadRepository.findOne(dirty.getThreadId());
        Asserts.isNotNull(thread, "threadId");
        Asserts.isNotReadOnly(thread);

        final boolean isMod = isSiteMod(thread, currentLogin);

        Comment parent = null;
        // reply only to approved comments
        if (isNew) {

            dirty.setCreatedByMod((isSuperMod || isMod) ? true : null);

            if (StringUtils.isBlank(dirty.getParentId())) {
                dirty.setParentId(null);
                dirty.setLevel(0);
            } else {
                parent = commentRepository.findOne(dirty.getParentId());
                if (parent == null) {
                    throw new KalipoException(ErrorCode.INVALID_PARAMETER, "parentId");
                }

                /*
                 fault tolerant: if discussion becomes too deep, dig up until valid
                */
                while (parent.getLevel() + 1 > MAX_LEVEL) {
                    parent = commentRepository.findOne(parent.getParentId());
                }

                parent.setRepliesCount(NumUtils.nullToZero(parent.getRepliesCount()) + 1);

                dirty.setLevel(parent.getLevel() + 1);

                if (parent.getStatus() != Comment.Status.APPROVED) {
                    throw new KalipoException(ErrorCode.CONSTRAINT_VIOLATED, "Invalid status of parent. It is not approved yet");
                }
            }
        }

        dirty.setAuthorId(currentLogin);
        dirty.setFingerprint(getFingerprint(parent, thread));

        dirty.setBodyHtml(markupService.toHtml(dirty.getBody()));

        dirty.setStatus(Comment.Status.NONE);
        log.info(String.format("User '%s' creates comment %s", currentLogin, dirty.toString()));

        assignSticky(dirty, original, isNew, isMod, isSuperMod);

        // --

        dirty = commentRepository.save(dirty);

        return dirty;
    }

    private String getFingerprint(Comment parent, Thread thread) {
        final String parentFp = parent == null ? "" : parent.getFingerprint();
        final int max = 99999;
        return parentFp + String.format("%05d", max - thread.getCommentCount());
    }

    /**
     * Sticky-field may only be set/changed by mods and supermods
     *
     * @param comment    the new comment
     * @param original   the original comment
     * @param isNew      helper, TRUE iff original is null
     * @param isMod      is current user a mod
     * @param isSuperMod is current user a supermod
     * @throws KalipoException
     */
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
            }

            comment.setSticky(original.getSticky());
        }
    }

    @Async
    public void logForward(String commentId, String url, String remoteAddr) throws KalipoException {
        Comment comment = commentRepository.findOne(commentId);
        log.info(String.format("forward %s via %s", AnonUtil.maskIp(remoteAddr), commentId));
        for (Comment.Link link : comment.getLinks()) {
            if (StringUtils.equals(url, link.getUrl())) {
                link.incrImpression();
                break;
            }
        }
        commentRepository.save(comment);
    }

    public Page<Comment> filtered(String userId, Comment.Status status, int page) {

        PageRequest pageable = new PageRequest(page, Constants.PAGE_SIZE, Sort.Direction.DESC, Constants.PARAM_CREATED_DATE);

        // todo implement a dynamic filter method
        // todo allow only owner or admin/mods

        if (StringUtils.isNotEmpty(userId)) {
            return commentRepository.findByAuthorId(userId, pageable);
        }

        if (status != null) {
            return commentRepository.findByStatus(status, pageable);
        }

        return null;
    }
}
