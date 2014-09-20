package org.kalipo.service;

import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.domain.Tag;
import org.kalipo.repository.TagRepository;
import org.kalipo.security.Privileges;
import org.kalipo.web.rest.KalipoRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.util.List;

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

    public List<Tag> getAll() {
        return tagRepository.findAll();
    }

    public Tag get(String id) throws KalipoRequestException {
        return tagRepository.findOne(id);
    }

    public void delete(String id) throws KalipoRequestException {
        tagRepository.delete(id);
    }

}
