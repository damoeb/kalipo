package org.kalipo.service;

import org.kalipo.config.Constants;
import org.kalipo.domain.*;
import org.kalipo.domain.Thread;
import org.kalipo.repository.*;
import org.kalipo.service.util.Asserts;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.inject.Inject;
import java.util.*;
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
    private SiteRepository siteRepository;

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private MailService mailService;

    @Inject
    private NonceService nonceService;

    @Inject
    private SpringTemplateEngine templateEngine;

    @Inject
    private Environment environment;

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
    public void notifyAsync(String recipientId, String initiatorId, Notification.Type type, String commentId) {
        try {
            Asserts.isNotNull(recipientId, "recipientId");
            Asserts.isNotNull(type, "type");
            Asserts.isNotNull(commentId, "commentId");

            sendNotice(recipientId, initiatorId, type, commentId, null);

        } catch (Exception e) {
            log.error(String.format("Unable to notify %s with %s of %s. Reason: %s", recipientId, type, commentId, e.getMessage()));
        }
    }

    @Async
    public void notifySuperModsOfFraudulentComment(Comment comment, String initiatorId) {
        try {
            // todo impl
        } catch (Exception e) {
            log.error(String.format("Unable to notify superMods of fraud-comment %s. Reason: %s", comment, e.getMessage()));
        }
    }

    @Async
    public void announcePendingReport(String threadId, Report report) {
        try {
            Asserts.isNotNull(threadId, "threadId");
            Asserts.isNotNull(report, "report");

            Thread thread = threadRepository.findOne(threadId);
            Asserts.isNotNull(thread, "threadId");

            Site site = siteRepository.findOne(thread.getSiteId());

            Locale locale = Locale.ENGLISH;
            String subject = String.format("Pending Report in '%s'", thread.getTitle());

            Comment comment = commentRepository.findOne(report.getCommentId());

            for(String modId : site.getModeratorIds()) {
                User mod = userRepository.findOne(modId);
                String content = createPendingReportEmailFromTemplate(mod, report, comment, locale);
                mailService.sendEmail(mod.getEmail(), subject, content, false, true);
            }

        } catch (Exception e) {
            log.error(String.format("Unable to notify mods in thread %s with report %s. Reason: %s", threadId, report, e.getMessage()));
        }
    }

    @Async
    public void announcePendingComment(Thread thread, Comment comment) {
        try {
            Asserts.isNotNull(thread, "thread");
            Asserts.isNotNull(comment, "comment");

            Site site = siteRepository.findOne(thread.getSiteId());
            Locale locale = Locale.ENGLISH;

            String subject = String.format("Pending Comment in '%s'", thread.getTitle());

            for(String modId : site.getModeratorIds()) {
                User mod = userRepository.findOne(modId);
                String content = createPendingCommentEmailFromTemplate(mod, comment, locale);
                mailService.sendEmail(mod.getEmail(), subject, content, false, true);
            }

        } catch (Exception e) {
            log.error(String.format("Unable to notify mods (site %s) of comment %s. Reason: %s", thread.getSiteId(), comment, e.getMessage()));
        }
    }

    @Async
    public void announceCommentRejected(Comment comment) {

    }

    @Async
    public void announceBan(String userId, String initiatorId) {

    }

    @Async
    public void announceCommentDeleted(Comment comment) {

    }

    @Async
    public void announceReportRejected(Report report) {

    }

    @Async
    public void announceReportApproved(Report report) {

    }

    @Async
    public void announceCommentLiked(Comment comment) {

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

    protected String createPendingReportEmailFromTemplate(User user, final Report report, Comment comment, Locale locale) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("displayName", user.getDisplayName());
        variables.put("baseUrl", environment.getProperty("baseUrl"));
        variables.put("report", report);
        variables.put("reason", String.format("%s %s", report.getReason(), report.getCustomReason()));
        variables.put("nonce", nonceService.createNonce());
        variables.put("comment", comment);
        IContext context = new org.thymeleaf.context.Context(locale, variables);
        return templateEngine.process("pendingReportEmail", context);
    }

    protected String createPendingCommentEmailFromTemplate(User user, final Comment comment, Locale locale) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("displayName", user.getDisplayName());
        variables.put("baseUrl", environment.getProperty("baseUrl"));
        variables.put("nonce", nonceService.createNonce());
        variables.put("comment", comment);
        IContext context = new org.thymeleaf.context.Context(locale, variables);
        return templateEngine.process("pendingCommentEmail", context);
    }
}
