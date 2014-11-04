package org.kalipo.service;

import org.kalipo.aop.Throttled;
import org.kalipo.domain.Notice;
import org.kalipo.repository.NoticeRepository;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class NoticeService {

    private static final int PAGE_SIZE = 20;
    private final Logger log = LoggerFactory.getLogger(NoticeService.class);

    @Inject
    private NoticeRepository noticeRepository;

    //    todo @Async
    public void notify(String recipientId, Notice.Type type, String commentId) {

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

        // just read field can be changed

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
}
