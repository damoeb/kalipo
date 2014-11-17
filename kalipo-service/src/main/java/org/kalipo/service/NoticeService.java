package org.kalipo.service;

import org.kalipo.aop.Throttled;
import org.kalipo.domain.*;
import org.kalipo.domain.Thread;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.NoticeRepository;
import org.kalipo.repository.ThreadRepository;
import org.kalipo.repository.UserRepository;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This service is used to write notification messages about likes (LIKE), mentions in a comment (MENTION), replies (REPLY), 3rd party comment deletion (DELETION), reports of comments to mods and supermods (REPORT), REVIEW, APPROVAL
 */
@Service
public class NoticeService {

    private static final int PAGE_SIZE = 20;
    private static final Pattern FIND_USER_REFERENCES = Pattern.compile("@[a-z0-9]+"); //todo fix pattern to not match email addresses

    private final Logger log = LoggerFactory.getLogger(NoticeService.class);

    @Inject
    private NoticeRepository noticeRepository;

    @Inject
    private ThreadRepository threadRepository;

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private UserRepository userRepository;

    public List<Notice> findByUser(final String login, final int pageNumber) {
        PageRequest pageable = new PageRequest(pageNumber, PAGE_SIZE, Sort.Direction.DESC, "createdDate");
        return noticeRepository.findByRecipientId(login, pageable);
    }

    //    @RolesAllowed(Privileges.CREATE_COMMENT)
    // todo remove
    @Throttled
    public Notice update(Notice notice) throws KalipoException {
        Asserts.isNotNull(notice, "notice");
        Asserts.isNotNull(notice.getId(), "id");

        // just field <read> can be changed

        Notice original = noticeRepository.findOne(notice.getId());
        Asserts.isCurrentLogin(original.getRecipientId());

        Asserts.nullOrEqual(notice.getCommentId(), original.getCommentId(), "commentId");
        notice.setCommentId(original.getCommentId());

        Asserts.nullOrEqual(notice.getInitiatorId(), original.getInitiatorId(), "initiatorId");
        notice.setInitiatorId(original.getInitiatorId());

        Asserts.nullOrEqual(notice.getRecipientId(), original.getRecipientId(), "recipientId");
        notice.setRecipientId(original.getRecipientId());

        Asserts.nullOrEqual(notice.getType(), original.getType(), "type");
        notice.setType(original.getType());

        Asserts.nullOrEqual(notice.getCreatedDate(), original.getCreatedDate(), "createdDate");
        notice.setCreatedDate(original.getCreatedDate());

        return noticeRepository.save(notice);
    }

    // -- ASYNCHRONOUS CALLS -------------------------------------------------------------------------------------------

    @Async
    public void notifyMentionedUsers(Comment comment) {
        try {
            Asserts.isNotNull(comment, "comment");

            if (comment.getStatus() == Comment.Status.APPROVED) {
                // find mentioned usernames, starting with @ like @myname
                Matcher matcher = FIND_USER_REFERENCES.matcher(comment.getText());
                Set<String> uqLogins = new HashSet<String>();
                while (matcher.find()) {
                    String login = matcher.group();
                    uqLogins.add(login);
                }

                for (String login : uqLogins) {
                    // notify @login
                    sendNotice(login, Notice.Type.MENTION, comment.getId());
                }
            }
        } catch (Exception e) {
            log.error(String.format("Unable to notify mentioned user. Reason: %s", e.getMessage()));
        }
    }

    @Async
    public void notifyModsOfThread(String threadId, Report report) {
        try {
            Asserts.isNotNull(threadId, "threadId");
            Asserts.isNotNull(report, "report");

            Thread thread = threadRepository.findOne(threadId);
            Asserts.isNotNull(thread, "threadId");

            thread.getModIds().forEach(modId -> sendNotice(modId, Notice.Type.REPORT, report.getCommentId()));

        } catch (Exception e) {
            log.error(String.format("Unable to notify mods of thread %s with report %s. Reason: %s", threadId, report, e.getMessage()));
        }
    }

    @Async
    public void notifyAuthorOfParent(Comment comment) {
        try {
            Asserts.isNotNull(comment, "comment");

            if (comment.getParentId() != null) {
                Comment parent = commentRepository.findOne(comment.getParentId());
                if (parent != null) {
                    sendNotice(parent.getAuthorId(), Notice.Type.REPLY, comment.getId());
                }
            }
        } catch (Exception e) {
            log.error(String.format("Unable to notify author of parent of %s. Reason: %s", comment, e.getMessage()));
        }
    }

    @Async
    public void notifyAsync(String recipientId, Notice.Type type, String commentId) {
        try {
            Asserts.isNotNull(recipientId, "recipientId");
            Asserts.isNotNull(type, "type");
            Asserts.isNotNull(commentId, "commentId");

            sendNotice(recipientId, type, commentId);

        } catch (Exception e) {
            log.error(String.format("Unable to notify %s with %s of %s. Reason: %s", recipientId, type, commentId, e.getMessage()));
        }
    }

    @Async
    public void notifySuperModsOfFraudulentComment(Comment comment) {
        try {
            Asserts.isNotNull(comment, "comment");
            userRepository.findSuperMods().forEach(user -> sendNotice(user.getLogin(), Notice.Type.REPORT, comment.getId()));
        } catch (Exception e) {
            log.error(String.format("Unable to notify superMods of fraud-comment %s. Reason: %s", comment, e.getMessage()));
        }
    }

    @Async
    public void notifySuperModsOfFraudulentUser(User user) {
        try {
//          todo implement: fraud user
//            Asserts.isNotNull(comment, "comment");
//            userRepository.findSuperMods().forEach(user -> sendNotice(user.getLogin(), Notice.Type.REPORT, comment.getId()));
        } catch (Exception e) {
            log.error(String.format("Unable to notify superMods of fraud-user %s. Reason: %s", user, e.getMessage()));
        }
    }

    @Async
    public void notifyModsOfThread(Thread thread, Comment comment) {
        try {
            Asserts.isNotNull(thread, "thread");
            Asserts.isNotNull(thread.getModIds(), "modIds");
            Asserts.isNotNull(comment, "comment");

            thread.getModIds().forEach(modId -> sendNotice(modId, Notice.Type.REVIEW, comment.getId()));

        } catch (Exception e) {
            log.error(String.format("Unable to notify mods (thread %s) of comment %s. Reason: %s", thread, comment, e.getMessage()));
        }
    }

    // --

    private void sendNotice(String recipientId, Notice.Type type, String commentId) {

        Notice notice = new Notice();
        notice.setRecipientId(recipientId);
        notice.setInitiatorId(SecurityUtils.getCurrentLogin());
        notice.setCommentId(commentId);
        notice.setType(type);

        noticeRepository.save(notice);
    }
}
