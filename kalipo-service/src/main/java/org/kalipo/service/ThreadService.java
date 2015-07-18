package org.kalipo.service;

import org.apache.commons.lang3.StringUtils;
import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.RateLimit;
import org.kalipo.config.Constants;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Site;
import org.kalipo.domain.Thread;
import org.kalipo.repository.*;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
import org.kalipo.service.util.URLNormalizer;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

@SuppressWarnings("unused")
@Service
@KalipoExceptionHandler
public class ThreadService {

    private final Logger log = LoggerFactory.getLogger(ThreadService.class);

    @Inject
    private Environment env;

    @Inject
    private ThreadRepository threadRepository;

    @Inject
    private SiteRepository siteRepository;

    @Inject
    private BanRepository banRepository;

    @Inject
    private VoteRepository voteRepository;

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private PrivilegeRepository privilegeRepository;

    @Inject
    private UserService userService;

    @Inject
    private NotificationService notificationService;

    @Inject
    private MarkupService markupService;

    @RolesAllowed(Privileges.CREATE_THREAD)
    @RateLimit
    public Thread create(Thread thread) throws KalipoException {

        Asserts.isNotNull(thread, "thread");
        Asserts.isNull(thread.getId(), "id");

        thread.setStatus(Thread.Status.OPEN);
        thread.setCommentCount(0);
        thread.setLikes(0);
        thread.setDislikes(0);
        thread.setDisplayName(SecurityUtils.getCurrentLogin());

        // siteId
        if(StringUtils.isBlank(thread.getSiteId())) {
            Site site = siteRepository.findByName(env.getProperty("siteName"));
            Asserts.isNotNull(site, "site");

            thread.setSiteId(site.getId());
        } else {
            Asserts.isTrue(siteRepository.exists(thread.getSiteId()), "siteId");
        }

        final String currentLogin = SecurityUtils.getCurrentLogin();
        thread.setInitiatorId(currentLogin);

        thread = save(thread);

        log.info(String.format("User '%s' created thread %s on site %s", currentLogin, thread.getId(), thread.getSiteId()));

        return thread;
    }

    @RolesAllowed(Privileges.CREATE_THREAD)
    @RateLimit
    public Thread update(Thread dirty) throws KalipoException {
        Asserts.isNotNull(dirty, "thread");
        Asserts.isNotNull(dirty.getId(), "id");

        Thread original = threadRepository.findOne(dirty.getId());
        Asserts.isNotNull(original, "thread");

        Site site = siteRepository.findOne(original.getSiteId());
        Set<String> moderators = site.getModeratorIds();

        // Permissions
        final String currentLogin = SecurityUtils.getCurrentLogin();
        boolean isMod = moderators.contains(currentLogin);
        if (!isMod && !userService.isSuperMod(currentLogin)) {
            throw new KalipoException(ErrorCode.PERMISSION_DENIED, "You must be mod or supermod");
        }

        // read only values

        Asserts.nullOrEqual(dirty.getSiteId(), original.getSiteId(), "siteId");
        Asserts.nullOrEqual(dirty.getCommentCount(), original.getCommentCount(), "commentCount");
        Asserts.nullOrEqual(dirty.getLikes(), original.getLikes(), "likes");
        Asserts.nullOrEqual(dirty.getDislikes(), original.getDislikes(), "dislikes");
        Asserts.nullOrEqual(dirty.getInitiatorId(), original.getInitiatorId(), "initiatorId");

        // update fields

        original.setUriHooks(dirty.getUriHooks());

        if(dirty.getStatus() != null && dirty.getStatus() != original.getStatus()) {
            log.info(String.format("User '%s' changes status of thread %s to %s (before %s)", currentLogin, original.getId(), dirty.getStatus(), original.getStatus()));
            original.setStatus(dirty.getStatus());
        }
        original.setTitle(dirty.getTitle());

        return save(original);
    }

    @Async
    public Future<Page<Thread>> getAllWithPages(Integer page) {
        Sort sort = new Sort(
                new Sort.Order(Sort.Direction.ASC, Constants.PARAM_CREATED_DATE)
        );
        PageRequest pageable = new PageRequest(page, 20, sort);
        return new AsyncResult<>(threadRepository.findAll(pageable));
    }

    @Async
    public Future<Thread> get(String id) throws KalipoException {
        return new AsyncResult<>(threadRepository.findOne(id));
    }

    @Async
    public Future<Page<Comment>> getCommentsWithPages(String id, Integer page) throws KalipoException {
        Sort sort = new Sort(
            new Sort.Order(Sort.Direction.ASC, "fingerprint")
        );
        PageRequest pageable = new PageRequest(page, 60, sort);
        return new AsyncResult<>(commentRepository.findByThreadIdAndStatusIn(id, Arrays.asList(Comment.Status.APPROVED, Comment.Status.PENDING, Comment.Status.DELETED), pageable));
    }

    @Async
    public Future<Page<Comment>> getLatestCommentsWithPages(String id, Integer page) throws KalipoException {
        Sort sort = new Sort(
            new Sort.Order(Sort.Direction.DESC, "lastModifiedDate")
        );
        PageRequest pageable = new PageRequest(page, 3, sort);
        return new AsyncResult<>(commentRepository.findByThreadIdAndStatusIn(id, Collections.singletonList(Comment.Status.APPROVED), pageable));
    }

    public void delete(String id) throws KalipoException {
        // todo check permissons

//       threadRepository.delete(id);
    }

    // --

    // todo move to site
//    private void validateKLine(Thread dirty, Thread original) throws KalipoException {
//        final String currentLogin = SecurityUtils.getCurrentLogin();
//        final Set<String> orgKLine = original.getBans();
//        final Set<String> dirtyKLine = dirty.getBans();
//
//        if (dirtyKLine == null || (orgKLine.size() == dirtyKLine.size() && orgKLine.containsAll(dirtyKLine))) {
//            dirty.setBans(orgKLine);
//        } else {
//            // validate kLine changes from update
//
//            // original.getBans() - thread.getBans()
//            Set<String> removed = orgKLine.stream().filter(uid -> dirtyKLine.contains(uid)).collect(Collectors.toSet());
//
//            // thread.getBans() - original.getBans();
//            Set<String> added = dirtyKLine.stream().filter(uid -> original.getBans().contains(uid)).collect(Collectors.toSet());
//
//            for (String userId : added) {
//                if (!userRepository.exists(userId)) {
//                    throw new KalipoException(ErrorCode.INVALID_PARAMETER, String.format("User %s does not exist", userId));
//                }
//
//                log.info(String.format("%s puts %s on k-Line of thread %s", currentLogin, userId, dirty.getId()));
//            }
//
//            if (removed != null) {
//                for (String userId : removed) {
//                    log.info(String.format("%s removes %s from k-Line of thread %s", currentLogin, userId, dirty.getId()));
//                }
//            }
//        }
//    }

    private Thread save(Thread thread) throws KalipoException {

        renderBody(thread);

        if (StringUtils.isNotBlank(thread.getLink())) {
            try {
                URI uri = markupService.resolveRedirects(thread.getLink());
                thread.setLink(uri.toASCIIString());
                thread.setDomain(uri.getHost());

            } catch (URISyntaxException e) {
                throw new KalipoException(ErrorCode.INVALID_PARAMETER, "link is invalid");
            }
        }

        // url hooks validation
        if (thread.getUriHooks() != null && !thread.getUriHooks().isEmpty()) {
            Asserts.hasPrivilege(Privileges.HOOK_THREAD_TO_URL);

            Set<String> normedUriHooks = new HashSet<>();

            for (String uri : thread.getUriHooks()) {
                if (!UrlUtils.isAbsoluteUrl(uri)) {
                    throw new KalipoException(ErrorCode.INVALID_PARAMETER, String.format("urlHook %s must be an absolute url", uri));
                }

                // todo set property via env.getProperty("domain")
                Asserts.isTrue(!StringUtils.containsIgnoreCase(uri, "kalipo.org"), "You cannot hook kalipo");

                // -- Assure that hooked urls has not been used anywhere else
                final String normed;
                try {
                    normed = URLNormalizer.normalize(uri);
                    normedUriHooks.add(normed);

                } catch (MalformedURLException e) {
                    throw new KalipoException(ErrorCode.INVALID_PARAMETER, String.format("urlHook %s looks nasty", uri));
                }

                Thread conflictingThread = threadRepository.findByUriHook(normed);

                if (conflictingThread != null && (thread.getId() == null || !StringUtils.equals(conflictingThread.getId(), thread.getId()))) {
                    throw new KalipoException(ErrorCode.INVALID_PARAMETER, String.format("urlHook %s is already in use in thread %s", normed, thread.getId()));
                }
            }

            thread.setUriHooks(normedUriHooks);
        }

        return threadRepository.save(thread);
    }

    private void renderBody(Thread thread) {
        // todo implement
        thread.setBodyHtml(thread.getBody());
    }
}
