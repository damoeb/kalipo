package org.kalipo.service;

import org.apache.commons.lang3.StringUtils;
import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.Throttled;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Tag;
import org.kalipo.domain.Thread;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.ThreadRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
import org.kalipo.web.rest.KalipoException;
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
@KalipoExceptionHandler
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
    public Thread create(Thread thread) throws KalipoException {

        Asserts.isNotNull(thread, "thread");
        Asserts.isNull(thread.getId(), "id");

        thread.setStatus(Thread.Status.OPEN);
        thread.setCommentCount(0);
        thread.setLikes(0);
        thread.setDislikes(0);

        thread = save(thread);

        // create lead comment
        Comment comment = new Comment();
        comment.setThreadId(thread.getId());
        comment.setText(thread.getText());

        comment = commentService.create(comment);

        thread.setLeadCommentId(comment.getId());

        save(thread);

        return thread;
    }

    @RolesAllowed(Privileges.MODERATE_THREAD)
    @Throttled
    public Thread update(Thread thread) throws KalipoException {
        Asserts.isNotNull(thread, "thread");
        Asserts.isNotNull(thread.getId(), "id");

        Thread original = threadRepository.findOne(thread.getId());
        Asserts.isNotNull(original, "thread");

        // keep final values
        Asserts.nullOrEqual(thread.getLeadCommentId(), original.getLeadCommentId(), "leadCommentId");
        thread.setLeadCommentId(original.getLeadCommentId());

        Asserts.nullOrEqual(thread.getCommentCount(), original.getCommentCount(), "commentCount");
        thread.setCommentCount(original.getCommentCount());

        Asserts.nullOrEqual(thread.getLikes(), original.getLikes(), "likes");
        thread.setLikes(original.getLikes());

        Asserts.nullOrEqual(thread.getDislikes(), original.getDislikes(), "dislikes");
        thread.setDislikes(original.getDislikes());

        return save(thread);
    }

    private Thread save(Thread thread) throws KalipoException {

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
    public Future<Thread> get(String id) throws KalipoException {
        return new AsyncResult<Thread>(threadRepository.findOne(id));
    }

    @Async
    public Future<List<Comment>> getComments(String id) throws KalipoException {
        return new AsyncResult<List<Comment>>(commentRepository.findByThreadIdAndStatus(id, Arrays.asList(Comment.Status.APPROVED, Comment.Status.PENDING)));
    }

    public void delete(String id) throws KalipoException {
        threadRepository.delete(id);
    }

    @RolesAllowed(Privileges.CREATE_THREAD)
    public void setTagsOfThread(String id, Set<Tag> tags) throws KalipoException {
        Thread thread = threadRepository.findOne(id);

        Asserts.isNotNull(thread, "id");

        // todo check ids if provided
        thread.setTags(tags);

        Asserts.isNotReadOnly(thread);

        threadRepository.save(thread);
    }
}
