package org.kalipo.service;

import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.domain.Privilege;
import org.kalipo.repository.PrivilegeRepository;
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
public class PrivilegeService {

    private final Logger log = LoggerFactory.getLogger(PrivilegeService.class);

    @Inject
    private PrivilegeRepository privilegeRepository;

    @RolesAllowed(Privileges.CREATE_PRIVILEGE)
    public void create(Privilege privilege) throws KalipoRequestException {

        // todo id must not exist id
        save(privilege);
    }

    @RolesAllowed(Privileges.CREATE_PRIVILEGE)
    public void update(Privilege privilege) throws KalipoRequestException {

        save(privilege);
    }

    private void save(Privilege privilege) throws KalipoRequestException {
        privilegeRepository.save(privilege);
    }

    @Async
    public Future<List<Privilege>> getAll() {
        return new AsyncResult<>(privilegeRepository.findAll());
    }

    @Async
    public Future<Privilege> get(String id) throws KalipoRequestException {
        return new AsyncResult<>(privilegeRepository.findOne(id));
    }

    public void delete(String id) throws KalipoRequestException {
        privilegeRepository.delete(id);
    }

}
