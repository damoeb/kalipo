package org.kalipo.service;

import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.domain.Tag;
import org.kalipo.repository.TagRepository;
import org.kalipo.security.Privileges;
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
public class TagService {

    private final Logger log = LoggerFactory.getLogger(TagService.class);

    @Inject
    private TagRepository tagRepository;

    @RolesAllowed(Privileges.CREATE_TAG)
    public void create(Tag tag) throws KalipoRequestException {

        // todo id must not exist id

        tagRepository.save(tag);
    }

    @RolesAllowed(Privileges.CREATE_TAG)
    public void update(Tag tag) throws KalipoRequestException {

        tagRepository.save(tag);
    }

    @Async
    public Future<List<Tag>> getAll() {
        return new AsyncResult<>(tagRepository.findAll());
    }

    @Async
    public Future<Tag> get(String id) throws KalipoRequestException {
        return new AsyncResult<>(tagRepository.findOne(id));
    }

    public void delete(String id) throws KalipoRequestException {
        tagRepository.delete(id);
    }

}
