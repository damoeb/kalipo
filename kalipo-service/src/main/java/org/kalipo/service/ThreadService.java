package org.kalipo.service;

import org.apache.commons.lang3.StringUtils;
import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.aop.Throttled;
import org.kalipo.domain.Thread;
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
import java.util.List;
import java.util.concurrent.Future;

@Service
@EnableArgumentValidation
public class ThreadService {

    private final Logger log = LoggerFactory.getLogger(ThreadService.class);

    @Inject
    private ThreadRepository threadRepository;

    @RolesAllowed(Privileges.CREATE_THREAD)
    @Throttled
    public Thread create(Thread thread) throws KalipoRequestException {

        Asserts.isNotNull(thread, "thread");
        Asserts.isNull(thread.getId(), "id");

        return save(thread);
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
        thread.setStatus(Thread.Status.OPEN);

        return threadRepository.save(thread);
    }

    @Async
    public Future<List<Thread>> getAll() {
        return new AsyncResult<>(threadRepository.findAll());
    }

    @Async
    public Future<Thread> get(String id) throws KalipoRequestException {
        return new AsyncResult<>(threadRepository.findOne(id));
    }

    public void delete(String id) throws KalipoRequestException {
        threadRepository.delete(id);
    }

}
