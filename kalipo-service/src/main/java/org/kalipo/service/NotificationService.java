package org.kalipo.service;

import org.kalipo.config.Constants;
import org.kalipo.domain.*;
import org.kalipo.domain.Thread;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.NotificationRepository;
import org.kalipo.repository.ThreadRepository;
import org.kalipo.repository.UserRepository;
import org.kalipo.service.util.Asserts;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This service is used to write notification messages about likes (LIKE), mentions in a comment (MENTION), replies (REPLY), 3rd party comment deletion (DELETION), reports of comments to mods and supermods (REPORT), REVIEW, APPROVAL
 */
@Service
public class NotificationService {

    private static final int PAGE_SIZE = 20;
    private static final Pattern FIND_MENTIONED_USER = Pattern.compile("@([a-z0-9]+)"); //todo fix pattern to not match email addresses

    private final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Inject
    private NotificationRepository notificationRepository;

    @Inject
    private ThreadRepository threadRepository;

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private UserRepository userRepository;

    public Page<Notification> findByUserWithPages(final String userId, final int pageNumber) throws KalipoException {
        Asserts.isNotNull(userId, "userId");
        Asserts.isCurrentLogin(userId);
        PageRequest pageable = new PageRequest(pageNumber, PAGE_SIZE, Sort.Direction.DESC, Constants.PARAM_CREATED_DATE);
        return notificationRepository.findByRecipientId(userId, pageable);
    }

    // -- ASYNCHRONOUS CALLS -------------------------------------------------------------------------------------------

    @Async
    public void notifyMentionedUsers(Comment comment, String initiatorId) {
        try {
            Asserts.isNotNull(comment, "comment");

            // find mentioned usernames, starting with @ like @myname
            Matcher matcher = FIND_MENTIONED_USER.matcher(comment.getBody());
            Set<String> uqLogins = new HashSet<String>();
            while (matcher.find()) {
                String login = matcher.group(1);
                uqLogins.add(login);
            }

            for (String login : uqLogins) {
                // notify @login
                sendNotice(login, initiatorId, Notification.Type.MENTION, comment.getId());
            }
        } catch (Exception e) {
            log.error(String.format("Unable to notify mentioned user. Reason: %s", e.getMessage()));
        }
    }

    @Async
    public void notifyModsOfThread(String threadId, Report report, String initiatorId) {
        try {
            Asserts.isNotNull(threadId, "threadId");
            Asserts.isNotNull(report, "report");

            Thread thread = threadRepository.findOne(threadId);
            Asserts.isNotNull(thread, "threadId");

            thread.getModIds().forEach(modId -> sendNotice(modId, initiatorId, Notification.Type.REPORT, report.getCommentId()));

        } catch (Exception e) {
            log.error(String.format("Unable to notify mods of thread %s with report %s. Reason: %s", threadId, report, e.getMessage()));
        }
    }

    @Async
    public void notifyAuthorOfParent(Comment comment, String initiatorId) {
        try {
            Asserts.isNotNull(comment, "comment");

            if (comment.getParentId() != null) {
                Comment parent = commentRepository.findOne(comment.getParentId());
                if (parent != null) {
                    sendNotice(parent.getAuthorId(), initiatorId, Notification.Type.REPLY, comment.getId());
                }
            }
        } catch (Exception e) {
            log.error(String.format("Unable to notify author of parent of %s. Reason: %s", comment, e.getMessage()));
        }
    }

    @Async
    public void notifyAsync(String recipientId, String initiatorId, Notification.Type type, String commentId, String message) {
        try {
            Asserts.isNotNull(recipientId, "recipientId");
            Asserts.isNotNull(type, "type");
            Asserts.isNotNull(commentId, "commentId");

            sendNotice(recipientId, initiatorId, type, commentId, message);

        } catch (Exception e) {
            log.error(String.format("Unable to notify %s with %s of %s. Reason: %s", recipientId, type, commentId, e.getMessage()));
        }
    }

    @Async
    public void notifyAsync(String recipientId, String initiatorId, Notification.Type type, String commentId) {
        notifyAsync(recipientId, initiatorId, type, commentId, null);
    }

    @Async
    public void notifySuperModsOfFraudulentComment(Comment comment, String initiatorId) {
        try {
            Asserts.isNotNull(comment, "comment");
            userRepository.findSuperMods().forEach(user -> sendNotice(user.getLogin(), initiatorId, Notification.Type.REPORT, comment.getId()));
        } catch (Exception e) {
            log.error(String.format("Unable to notify superMods of fraud-comment %s. Reason: %s", comment, e.getMessage()));
        }
    }

    @Async
    public void notifySuperModsOfFraudulentUser(User user, String initiatorId) {
        try {

            Asserts.isNotNull(user, "user");
            userRepository.findSuperMods().forEach(mod -> sendNotice(mod.getLogin(), initiatorId, Notification.Type.FRAUDULENT_USER, user.getLogin()));

        } catch (Exception e) {
            log.error(String.format("Unable to notify superMods of fraud-user %s. Reason: %s", user, e.getMessage()));
        }
    }

    @Async
    public void notifyModsOfThread(Thread thread, Comment comment, String initiatorId) {
        try {
            Asserts.isNotNull(thread, "thread");
            Asserts.isNotNull(thread.getModIds(), "modIds");
            Asserts.isNotNull(comment, "comment");

            // todo sendMail
            thread.getModIds().forEach(modId -> sendNotice(modId, initiatorId, Notification.Type.REVIEW, comment.getId()));

        } catch (Exception e) {
            log.error(String.format("Unable to notify mods (thread %s) of comment %s. Reason: %s", thread, comment, e.getMessage()));
        }
    }

    // --

    private void sendNotice(String recipientId, String initiatorId, Notification.Type type, String resourceId, String message) {

        log.debug(String.format("-> notify %s of %s on resource %s", recipientId, type.name(), resourceId));

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setRecipientId(recipientId);
        notification.setInitiatorId(initiatorId);
        notification.setResourceId(resourceId);
        notification.setType(type);

        notificationRepository.save(notification);
    }

    private void sendNotice(String recipientId, String initiatorId, Notification.Type type, String resourceId) {
        sendNotice(recipientId, initiatorId, type, resourceId, null);
    }
}
