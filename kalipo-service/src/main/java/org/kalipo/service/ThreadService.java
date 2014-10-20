package org.kalipo.service;

import org.apache.commons.lang3.StringUtils;
import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.aop.Throttled;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Tag;
import org.kalipo.domain.Thread;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.ThreadRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
import org.kalipo.web.rest.KalipoRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

@Service
@EnableArgumentValidation
public class ThreadService {

    private final Logger log = LoggerFactory.getLogger(ThreadService.class);

    @Inject
    private ThreadRepository threadRepository;

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private CommentService commentService;

    @RolesAllowed(Privileges.CREATE_THREAD)
    @Throttled
    public Thread create(Thread thread) throws KalipoRequestException {

        Asserts.isNotNull(thread, "thread");
        Asserts.isNull(thread.getId(), "id");

        thread.setStatus(Thread.Status.OPEN);

        thread = save(thread);

        Comment comment = new Comment();
        comment.setThreadId(thread.getId());
        comment.setTitle(thread.getTitle());
        comment.setText(thread.getText());

        commentService.create(comment);

        return thread;
    }

    @RolesAllowed(Privileges.CREATE_THREAD)
    @Throttled
    public Thread update(Thread thread) throws KalipoRequestException {
        Asserts.isNotNull(thread, "thread");

        return save(thread);
    }

    private Thread save(Thread thread) throws KalipoRequestException {

        if (StringUtils.isNotBlank(thread.getUriHook())) {
            Asserts.hasPrivilege(Privileges.HOOK_THREAD_TO_URL);
        }

        thread.setAuthorId(SecurityUtils.getCurrentLogin());

        return threadRepository.save(thread);
    }

    @Async
    public Future<List<Thread>> getAll() {
        return new AsyncResult<List<Thread>>(threadRepository.findAll());
    }

    @Async
    public Future<Thread> get(String id) throws KalipoRequestException {
        return new AsyncResult<Thread>(threadRepository.findOne(id));
    }

    @Async
    public Future<List<Comment>> getComments(String id) throws KalipoRequestException {
        return new AsyncResult<List<Comment>>(commentRepository.findByThreadIdAndStatus(id, Arrays.asList(Comment.Status.APPROVED, Comment.Status.PENDING)));
    }

    public void delete(String id) throws KalipoRequestException {
        threadRepository.delete(id);
    }

    @RolesAllowed(Privileges.CREATE_THREAD)
    public void setTagsOfThread(String id, Set<Tag> tags) throws KalipoRequestException {
        Thread thread = threadRepository.findOne(id);

        Asserts.isNotNull(thread, "id");

        // todo check ids if provided
        thread.setTags(tags);

        Asserts.isNotReadOnly(thread);

        threadRepository.save(thread);
    }
}
