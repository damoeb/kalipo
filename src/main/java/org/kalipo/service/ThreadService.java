package org.kalipo.service;

import org.apache.commons.lang3.StringUtils;
import org.kalipo.domain.Thread;
import org.kalipo.repository.ThreadRepository;
import org.kalipo.web.rest.IllegalParameterException;
import org.kalipo.web.rest.KalipoRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class ThreadService {

    private final Logger log = LoggerFactory.getLogger(ThreadService.class);

    @Inject
    private ThreadRepository threadRepository;

    public void create(Thread thread) {

        thread.setAuthorId("d"); // todo SecurityUtils.getCurrentLogin() is null during tests
        thread.setStatus(Thread.Status.OPEN);

        threadRepository.save(thread);
    }

    public void update(Thread thread) throws KalipoRequestException {

        if (StringUtils.isBlank(thread.getId())) {
            throw new IllegalParameterException();
        }

        thread.setAuthorId("d"); // todo SecurityUtils.getCurrentLogin() is null during tests
        thread.setStatus(Thread.Status.OPEN);

        threadRepository.save(thread);
    }

    public List<Thread> getAll() {
        return threadRepository.findAll();
    }

    public Thread get(String id) throws KalipoRequestException {
        if (StringUtils.isBlank(id)) {
            throw new IllegalParameterException();
        }

        return threadRepository.findOne(id);
    }

    public void delete(String id) throws KalipoRequestException {
        if (StringUtils.isBlank(id)) {
            throw new IllegalParameterException();
        }

        threadRepository.delete(id);
    }

}
