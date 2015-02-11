package org.kalipo.service;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.Throttled;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Privilege;
import org.kalipo.domain.Thread;
import org.kalipo.domain.User;
import org.kalipo.repository.*;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
import org.kalipo.service.util.URLNormalizer;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@KalipoExceptionHandler
public class ThreadService {

    private final Logger log = LoggerFactory.getLogger(ThreadService.class);

    @Inject
    private ThreadRepository threadRepository;

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

    @RolesAllowed(Privileges.CREATE_THREAD)
    @Throttled
    public Thread create(Thread thread) throws KalipoException {

        Asserts.isNotNull(thread, "thread");
        Asserts.isNull(thread.getId(), "id");

        thread.setStatus(Thread.Status.OPEN);
        thread.setCommentCount(0);
        thread.setLikes(0);
        thread.setDislikes(0);
        thread.setDisplayName(SecurityUtils.getCurrentLogin());

        // todo implement + get 48h from properties
        thread.setUglyDucklingSurvivalEndDate(DateTime.now().plusHours(48));

        final String currentLogin = SecurityUtils.getCurrentLogin();
        thread.getModIds().add(currentLogin);
        thread.setInitiatorId(currentLogin);

        thread = save(thread);

        log.info(String.format("%s created thread %s", currentLogin, thread.getId()));

        return thread;
    }

    @RolesAllowed(Privileges.CREATE_THREAD)
    @Throttled
    public Thread update(Thread dirty) throws KalipoException {
        Asserts.isNotNull(dirty, "thread");
        Asserts.isNotNull(dirty.getId(), "id");

        Thread original = threadRepository.findOne(dirty.getId());
        Asserts.isNotNull(original, "thread");

        Set<String> originalModIds = original.getModIds();

        // Permissions
        final String currentLogin = SecurityUtils.getCurrentLogin();
        boolean isMod = originalModIds.contains(currentLogin);
        if (!isMod && !userService.isSuperMod(currentLogin)) {
            throw new KalipoException(ErrorCode.PERMISSION_DENIED, "You must be mod or supermod");
        }

        // mod rule: add any user with MODERATE_THREAD, remove only self, iff not empty
        if (dirty.getModIds() != null && dirty.getModIds().isEmpty()) {
            throw new KalipoException(ErrorCode.INVALID_PARAMETER, "Set of mods cannot be empty");
        }

        // read only values

        Asserts.nullOrEqual(dirty.getCommentCount(), original.getCommentCount(), "commentCount");
        Asserts.nullOrEqual(dirty.getLikes(), original.getLikes(), "likes");
        Asserts.nullOrEqual(dirty.getDislikes(), original.getDislikes(), "dislikes");
        Asserts.nullOrEqual(dirty.getInitiatorId(), original.getInitiatorId(), "initiatorId");
        Asserts.nullOrEqual(dirty.getUglyDucklingSurvivalEndDate(), original.getUglyDucklingSurvivalEndDate(), "uglyDucklingSurvivalEndDate");

        // update fields

        original.setUriHooks(dirty.getUriHooks());
        original.setReadOnly(dirty.getReadOnly());
        original.setTitle(dirty.getTitle());

        validateModIds(dirty, original);
        original.setModIds(dirty.getModIds());

        validateKLine(dirty, original);
        original.setkLine(dirty.getkLine());

        return save(original);
    }

    @Async
    public Future<List<Thread>> getAll() {
        return new AsyncResult<List<Thread>>(threadRepository.findAll());
    }

    @Async
    public Future<Thread> get(String id) throws KalipoException {
        return new AsyncResult<Thread>(threadRepository.findOne(id));
    }

    @Async
    public Future<Page<Comment>> getCommentsWithPages(String id, Integer page) throws KalipoException {
        Sort sort = new Sort(
                new Sort.Order(Sort.Direction.ASC, "fingerprint")
        );
        PageRequest pageable = new PageRequest(page, 200, sort);
        return new AsyncResult<Page<Comment>>(commentRepository.findByThreadIdAndStatusIn(id, Arrays.asList(Comment.Status.APPROVED, Comment.Status.PENDING, Comment.Status.DELETED), pageable));
    }

    @Async
    public Future<List<Comment>> getCommentsSince(String id, DateTime since) throws KalipoException {
        return new AsyncResult<List<Comment>>(commentRepository.findByThreadIdAndCreatedDateAfter(id, since));
    }

    public void delete(String id) throws KalipoException {
        // todo check permissons

//       threadRepository.delete(id);
    }

    public Future<List<Comment>> getFullOutline(String threadId) {
        List<Comment> outline = commentRepository.getInfluenceByThreadId(threadId);
        outline.sort((c1, c2) -> c1.getFingerprint().compareTo(c2.getFingerprint()));
        return new AsyncResult<List<Comment>>(outline);
    }

    // --

    @Scheduled(fixedDelay = 20000)
    public void updateThreadStats() {

        Sort sort = new Sort(Sort.Direction.ASC, "lastModifiedDate");
        PageRequest request = new PageRequest(0, 10, sort);

        List<Thread> threads = threadRepository.findByStatusAndReadOnly(Thread.Status.OPEN, false, request);
        for (Thread thread : threads) {
            log.debug("Updating stats of thread {}", thread.getId());

            thread.setLikes(voteRepository.countLikesOfThread(thread.getId()));
            thread.setCommentCount(commentRepository.countApprovedInThread(thread.getId()));
            thread.setPendingCount(commentRepository.countPendingInThread(thread.getId()));
            thread.setReportedCount(commentRepository.countReportedInThread(thread.getId()));
            thread.setLastModifiedDate(DateTime.now());

//                commentCount, likes, authors

            threadRepository.save(thread);
        }
    }

    // --

    private void validateKLine(Thread dirty, Thread original) throws KalipoException {
        final String currentLogin = SecurityUtils.getCurrentLogin();
        final Set<String> orgKLine = original.getkLine();
        final Set<String> dirtyKLine = dirty.getkLine();

        if (dirtyKLine == null || (orgKLine.size() == dirtyKLine.size() && orgKLine.containsAll(dirtyKLine))) {
            dirty.setkLine(orgKLine);
        } else {
            // validate kLine changes from update

            // original.getkLine() - thread.getkLine()
            Set<String> removed = orgKLine.stream().filter(uid -> dirtyKLine.contains(uid)).collect(Collectors.toSet());

            // thread.getkLine() - original.getkLine();
            Set<String> added = dirtyKLine.stream().filter(uid -> original.getkLine().contains(uid)).collect(Collectors.toSet());

            for (String userId : added) {
                if (!userRepository.exists(userId)) {
                    throw new KalipoException(ErrorCode.INVALID_PARAMETER, String.format("User %s does not exist", userId));
                }

                log.info(String.format("%s puts %s on k-Line of thread %s", currentLogin, userId, dirty.getId()));
            }

            if (removed != null) {
                for (String userId : removed) {
                    log.info(String.format("%s removes %s from k-Line of thread %s", currentLogin, userId, dirty.getId()));
                }
            }
        }

    }

    private void validateModIds(Thread dirty, Thread original) throws KalipoException {
        final String currentLogin = SecurityUtils.getCurrentLogin();
        final Set<String> orgModIds = original.getModIds();
        final Set<String> dirtyModIds = dirty.getModIds();

        if (dirtyModIds == null || (orgModIds.size() == dirtyModIds.size() && orgModIds.containsAll(dirtyModIds))) {
            dirty.setModIds(orgModIds);

        } else {
            // validate modIds changes from update

            // original.getModIds() - thread.getModIds()
            Set<String> removed = orgModIds.stream().filter(uid -> dirtyModIds.contains(uid)).collect(Collectors.toSet());

            // thread.getModIds() - original.getModIds();
            Set<String> added = dirtyModIds.stream().filter(uid -> original.getModIds().contains(uid)).collect(Collectors.toSet());

            Privilege priv = privilegeRepository.findByName(Privileges.CREATE_THREAD);

            for (String userId : added) {
                // Asserts.hasPrivilege(Privileges.CREATE_THREAD);
                User user = userRepository.findOne(userId);
                if (user == null) {
                    throw new KalipoException(ErrorCode.INVALID_PARAMETER, String.format("User %s does not exist", userId));
                }
                if (user.getReputation() < priv.getReputation()) {
                    throw new KalipoException(ErrorCode.PERMISSION_DENIED, String.format("User %s requires %s reputation to become mod", userId, priv.getReputation()));
                }

                log.info(String.format("%s adds %s to moderators of thread %s", currentLogin, userId, dirty.getId()));
            }

            // Asserts.hasPrivilege(Privileges.SUPER_MODERATOR);
            if (!removed.isEmpty()) {
                User user = userRepository.findOne(currentLogin);
                for (String userId : removed) {

                    // 1. currentUser removes himself
                    // 2. superMod removes anyone
                    if (!user.isSuperMod() && !StringUtils.equals(userId, currentLogin)) {
                        throw new KalipoException(ErrorCode.PERMISSION_DENIED, "You have to be superMod or the user himself");
                    }

                    // threadInitiator cannot remove himself from mod-list
                    final boolean isThreadInitiator = StringUtils.equals(original.getInitiatorId(), currentLogin);
                    if (StringUtils.equals(userId, currentLogin) && isThreadInitiator) {
                        throw new KalipoException(ErrorCode.PERMISSION_DENIED, "SuperMod privileges required. You cannot remove yourself, since you are initiator");
                    }

                    log.info(String.format("%s removes %s from moderators of thread %s", currentLogin, userId, dirty.getId()));
                }
            }
        }
    }

    private Thread save(Thread thread) throws KalipoException {

        renderBody(thread);

        // url hooks validation
        if (thread.getUriHooks() != null && !thread.getUriHooks().isEmpty()) {
            Asserts.hasPrivilege(Privileges.HOOK_THREAD_TO_URL);

            Set<String> normedUriHooks = new HashSet<String>();

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
