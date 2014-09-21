package org.kalipo.service;

import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.domain.Thread;
import org.kalipo.repository.ThreadRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
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
    public void create(Thread thread) throws KalipoRequestException {

        // todo id must not exist id
        save(thread);
    }

    @RolesAllowed(Privileges.CREATE_THREAD)
    public void update(Thread thread) throws KalipoRequestException {
        save(thread);
    }

    private void save(Thread thread) throws KalipoRequestException {

        thread.setAuthorId(SecurityUtils.getCurrentLogin());
        thread.setStatus(Thread.Status.OPEN);

        threadRepository.save(thread);
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
