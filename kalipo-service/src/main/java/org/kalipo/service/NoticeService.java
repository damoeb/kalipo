package org.kalipo.service;

import org.kalipo.domain.Notice;
import org.kalipo.service.util.Asserts;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NoticeService {

    private final Logger log = LoggerFactory.getLogger(NoticeService.class);

    @Async
    public void notify(String recipientId, Notice.Type type, String commentId) {

        try {
            Asserts.isNotNull(recipientId, "recipientId");
            Asserts.isNotNull(type, "type");
            Asserts.isNotNull(commentId, "commentId");

            Notice notice = new Notice();
            notice.setRecipientId(recipientId);
            notice.setCommentId(commentId);
            notice.setType(type);

            // todo save

        } catch (KalipoException e) {
            // todo log
        }
    }
}
