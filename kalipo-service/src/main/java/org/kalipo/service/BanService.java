package org.kalipo.service;

import org.joda.time.DateTime;
import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.RateLimit;
import org.kalipo.config.Constants;
import org.kalipo.domain.Ban;
import org.kalipo.domain.Site;
import org.kalipo.domain.Thread;
import org.kalipo.repository.BanRepository;
import org.kalipo.repository.SiteRepository;
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
import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.Future;

@SuppressWarnings("unused")
@Service
@KalipoExceptionHandler
public class BanService {

    private final Logger log = LoggerFactory.getLogger(BanService.class);

    @Inject
    private BanRepository banRepository;

    @Inject
    private ThreadRepository threadRepository;

    @Inject
    private SiteRepository siteRepository;

    @Inject
    private NotificationService notificationService;

    @RateLimit
    public Ban create(@Valid Ban ban) throws KalipoException {

        Asserts.isNotNull(ban, "ban");
        Asserts.isNull(ban.getId(), "id");

        banRepository.save(ban);

        String userId = ban.getUserId();
        String currentLogin = SecurityUtils.getCurrentLogin();

        log.info("User '%s' bans '%s' on site %s until %s", currentLogin, userId, ban.getSiteId(), ban.getValidUntil());
        notificationService.announceBan(userId, currentLogin);

        return banRepository.save(ban);
    }

    @Async
    public Future<List<Ban>> getBansWithPages(String siteId, int pageNumber) {
        PageRequest pageable = new PageRequest(pageNumber, 10, Sort.Direction.DESC, Constants.PARAM_CREATED_DATE);
        return new AsyncResult<>(banRepository.findBySiteId(siteId, pageable));
    }

    public void delete(String id) {
        banRepository.delete(id);
    }

    @RolesAllowed(Privileges.BAN_USER)
    public void banUser(String userId, String threadId) throws KalipoException {
        Asserts.isNotNull(userId, "userId");
        Asserts.isNotNull(threadId, "threadId");

        Thread thread = threadRepository.findOne(threadId);
        Site site = siteRepository.findOne(thread.getSiteId());
//      todo check permissons is mod of thread
//      cannot ban a mod userId

        Ban ban = new Ban();
        ban.setUserId(userId);
        ban.setSiteId(site.getId());
        DateTime until = DateTime.now().plusDays(3);
        ban.setValidUntil(until);

        create(ban);
    }

    public Ban get(String id) {
        return banRepository.findOne(id);
    }
}
