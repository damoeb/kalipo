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
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.PrivilegeRepository;
import org.kalipo.repository.ThreadRepository;
import org.kalipo.repository.UserRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
import org.kalipo.service.util.URLNormalizer;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
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
    private CommentRepository commentRepository;

    @Inject
    private CommentService commentService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private PrivilegeRepository privilegeRepository;

    @Inject
    private UserService userService;

    @Inject
    private Environment env;

    @RolesAllowed(Privileges.CREATE_THREAD)
    @Throttled
    public Thread create(Thread thread) throws KalipoException {

        Asserts.isNotNull(thread, "thread");
        Asserts.isNull(thread.getId(), "id");

        thread.setStatus(Thread.Status.OPEN);
        thread.setCommentCount(0);
        thread.setLikes(0);
        thread.setDislikes(0);

        // todo implement + get 48h from properties
        thread.setUglyDucklingSurvivalEndDate(DateTime.now().plusHours(48));

        final String currentLogin = SecurityUtils.getCurrentLogin();
        thread.getModIds().add(currentLogin);
        thread.setInitiatorId(currentLogin);

        thread = save(thread);

        // create lead comment
        Comment comment = new Comment();
        comment.setThreadId(thread.getId());
        comment.setText(thread.getText());

        comment = commentService.create(comment);
        comment = commentService.approve(comment);

        thread.setLeadCommentId(comment.getId());

        save(thread);

        log.info(String.format("%s created thread %s", currentLogin, thread.getId()));

        return thread;
    }

    @RolesAllowed(Privileges.CREATE_THREAD)
    @Throttled
    public Thread update(Thread thread) throws KalipoException {
        Asserts.isNotNull(thread, "thread");
        Asserts.isNotNull(thread.getId(), "id");

        Thread original = threadRepository.findOne(thread.getId());
        Asserts.isNotNull(original, "thread");

        Set<String> originalModIds = original.getModIds();

        // Permissions
        String currentLogin = SecurityUtils.getCurrentLogin();
        boolean isMod = originalModIds.contains(currentLogin);
        if (!isMod && !userService.isSuperMod(currentLogin)) {
            throw new KalipoException(ErrorCode.PERMISSION_DENIED);
        }

        // mod rule: add any user with MODERATE_THREAD, remove only self, iff not empty
        if (thread.getModIds() != null && thread.getModIds().isEmpty()) {
            throw new KalipoException(ErrorCode.INVALID_PARAMETER, "Set of mods cannot be empty");
        }

        // read only values

        // todo test this

        Asserts.nullOrEqual(thread.getLeadCommentId(), original.getLeadCommentId(), "leadCommentId");
        Asserts.nullOrEqual(thread.getCommentCount(), original.getCommentCount(), "commentCount");
        Asserts.nullOrEqual(thread.getLikes(), original.getLikes(), "likes");
        Asserts.nullOrEqual(thread.getDislikes(), original.getDislikes(), "dislikes");
        Asserts.nullOrEqual(thread.getInitiatorId(), original.getInitiatorId(), "initiatorId");
        Asserts.nullOrEqual(thread.getUglyDucklingSurvivalEndDate(), original.getUglyDucklingSurvivalEndDate(), "uglyDucklingSurvivalEndDate");

        // update fields

        original.setUriHooks(thread.getUriHooks());
        original.setReadOnly(thread.getReadOnly());
        original.setTitle(thread.getTitle());

        validateModIds(thread, original);
        original.setModIds(thread.getModIds());

        validateKLine(thread, original);
        original.setkLine(thread.getkLine());

        return save(original);
    }

    private void validateKLine(Thread thread, Thread original) throws KalipoException {
        final String currentLogin = SecurityUtils.getCurrentLogin();
        final Set<String> originalkLine = original.getkLine();

        if (thread.getkLine() == null || originalkLine.containsAll(thread.getkLine())) {
            thread.setkLine(originalkLine);
        } else {
            // validate kLine changes from update

            // original.getkLine() - thread.getkLine()
            Set<String> removed = originalkLine.stream().filter(uid -> thread.getkLine().contains(uid)).collect(Collectors.toSet());

            // thread.getkLine() - original.getkLine();
            Set<String> added = thread.getkLine().stream().filter(uid -> original.getkLine().contains(uid)).collect(Collectors.toSet());

            for (String userId : added) {
                if (!userRepository.exists(userId)) {
                    throw new KalipoException(ErrorCode.INVALID_PARAMETER, String.format("User %s does not exist", userId));
                }

                log.info(String.format("%s puts %s on k-Line of thread %s", currentLogin, userId, thread.getId()));
            }

            if (removed != null) {
                for (String userId : removed) {
                    log.info(String.format("%s removes %s from k-Line of thread %s", currentLogin, userId, thread.getId()));
                }
            }
        }

    }

    private void validateModIds(Thread thread, Thread original) throws KalipoException {
        final String currentLogin = SecurityUtils.getCurrentLogin();
        final Set<String> originalModIds = original.getModIds();

        if (thread.getModIds() == null || originalModIds.containsAll(thread.getModIds())) {
            thread.setModIds(originalModIds);
        } else {
            // validate modIds changes from update

            // original.getModIds() - thread.getModIds()
            Set<String> removed = originalModIds.stream().filter(uid -> thread.getModIds().contains(uid)).collect(Collectors.toSet());

            // thread.getModIds() - original.getModIds();
            Set<String> added = thread.getModIds().stream().filter(uid -> original.getModIds().contains(uid)).collect(Collectors.toSet());

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

                log.info(String.format("%s adds %s to moderators of thread %s", currentLogin, userId, thread.getId()));
            }

            // Asserts.hasPrivilege(Privileges.SUPER_MODERATOR);
            if (!removed.isEmpty()) {
                User user = userRepository.findOne(currentLogin);
                for (String userId : removed) {
                    // 1. currentUser removes himself
                    // 2. superMod removes anyone
                    if (!user.isSuperMod() && !StringUtils.equals(userId, currentLogin)) {
                        throw new KalipoException(ErrorCode.PERMISSION_DENIED, "User %s requires %s reputation to become mod");
                    }
                    log.info(String.format("%s removes %s from moderators of thread %s", currentLogin, userId, thread.getId()));
                }
            }
        }
    }

    private Thread save(Thread thread) throws KalipoException {

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

    private String lo(String s) {
        return StringUtils.lowerCase(s);
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
    public Future<List<Comment>> getComments(String id) throws KalipoException {
        return new AsyncResult<List<Comment>>(commentRepository.findByThreadIdAndStatus(id, Arrays.asList(Comment.Status.APPROVED, Comment.Status.PENDING, Comment.Status.DELETED)));
    }

    public void delete(String id) throws KalipoException {
        threadRepository.delete(id);
    }
}
