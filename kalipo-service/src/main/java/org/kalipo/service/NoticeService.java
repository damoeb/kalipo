package org.kalipo.service;

import org.kalipo.aop.Throttled;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Notice;
import org.kalipo.domain.Report;
import org.kalipo.domain.Thread;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.NoticeRepository;
import org.kalipo.repository.ThreadRepository;
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

    @Async
    public void notifyMentionedUsers(Comment comment) {
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
                notifyAsync(login, Notice.Type.MENTION, comment.getId());
            }
        }
    }

    @Async
    public void notifyModsOfThread(String threadId, Report report) {
        for (String modId : threadRepository.findOne(threadId).getModIds()) {
            notifyAsync(modId, Notice.Type.REPORT, report.getCommentId());
        }
    }

    @Async
    public void notifyAuthorOfParent(Comment comment) {
        if (comment.getParentId() != null) {
            Comment parent = commentRepository.findOne(comment.getParentId());
            if (parent != null) {
                notifyAsync(parent.getAuthorId(), Notice.Type.REPLY, comment.getId());
            }
        }
    }

    @Async
    public void notifyAsync(String recipientId, Notice.Type type, String commentId) {

        try {
            Asserts.isNotNull(recipientId, "recipientId");
            Asserts.isNotNull(type, "type");
            Asserts.isNotNull(commentId, "commentId");

            Notice notice = new Notice();
            notice.setRecipientId(recipientId);
            notice.setInitiatorId(SecurityUtils.getCurrentLogin());
            notice.setCommentId(commentId);
            notice.setType(type);

            noticeRepository.save(notice);

        } catch (KalipoException e) {
            log.error(String.format("Unable to notify recipient %s about %s on %s", recipientId, type, commentId), e);
        }
    }

    public List<Notice> findByUser(final String login, final int pageNumber) {
        PageRequest pageable = new PageRequest(pageNumber, PAGE_SIZE, Sort.Direction.DESC, "createdDate");
        return noticeRepository.findByRecipientId(login, pageable);
    }

    //    @RolesAllowed(Privileges.CREATE_COMMENT)
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

    public void notifySuperMods(Comment comment) {
//          todo implement: bad comment, add supermod field to user
    }

    public void notifyModsOfThread(Thread thread, Comment comment) {
        for (String modId : thread.getModIds()) {
            notifyAsync(modId, Notice.Type.REVIEW, comment.getId());
        }
    }
}
