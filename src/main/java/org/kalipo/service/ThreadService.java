package org.kalipo.service;

import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.domain.Thread;
import org.kalipo.repository.ThreadRepository;
import org.kalipo.security.SecurityUtils;
import org.kalipo.web.rest.KalipoRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
@EnableArgumentValidation
public class ThreadService {

    private final Logger log = LoggerFactory.getLogger(ThreadService.class);

    @Inject
    private ThreadRepository threadRepository;

    //    @RolesAllowed(Roles.EDIT_THREAD)
    public void create(Thread thread) throws KalipoRequestException {

        thread.setAuthorId(SecurityUtils.getCurrentLogin());
        thread.setStatus(Thread.Status.OPEN);

        threadRepository.save(thread);
    }

    //    @RolesAllowed(Privileges.EDIT_THREAD)
    public void update(Thread thread) throws KalipoRequestException {

        thread.setAuthorId(SecurityUtils.getCurrentLogin());
        thread.setStatus(Thread.Status.OPEN);

        threadRepository.save(thread);
    }

    public List<Thread> getAll() {
        return threadRepository.findAll();
    }

    public Thread get(String id) throws KalipoRequestException {
        return threadRepository.findOne(id);
    }

    //    @RolesAllowed(Roles.EDIT_THREAD)
    public void delete(String id) throws KalipoRequestException {
        threadRepository.delete(id);
    }

}
