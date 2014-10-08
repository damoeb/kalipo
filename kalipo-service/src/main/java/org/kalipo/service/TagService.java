package org.kalipo.service;

import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.aop.Throttled;
import org.kalipo.domain.Tag;
import org.kalipo.repository.TagRepository;
import org.kalipo.security.Privileges;
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
public class TagService {

    private final Logger log = LoggerFactory.getLogger(TagService.class);

    @Inject
    private TagRepository tagRepository;

    @RolesAllowed(Privileges.CREATE_TAG)
    @Throttled
    public Tag create(Tag tag) throws KalipoRequestException {

        Asserts.isNull(tag.getId(), "id");

        return save(tag);
    }

    @RolesAllowed(Privileges.CREATE_TAG)
    @Throttled
    public Tag update(Tag tag) throws KalipoRequestException {

        return save(tag);
    }

    private Tag save(Tag tag) throws KalipoRequestException {

        return tagRepository.save(tag);
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
