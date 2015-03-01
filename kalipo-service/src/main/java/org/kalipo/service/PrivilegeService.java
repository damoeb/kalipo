package org.kalipo.service;

import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.RateLimit;
import org.kalipo.domain.Privilege;
import org.kalipo.repository.PrivilegeRepository;
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
import java.util.List;
import java.util.concurrent.Future;

@Service
@KalipoExceptionHandler
public class PrivilegeService {

    private final Logger log = LoggerFactory.getLogger(PrivilegeService.class);

    @Inject
    private PrivilegeRepository privilegeRepository;

    @RolesAllowed(Privileges.CREATE_PRIVILEGE)
    @RateLimit
    public Privilege create(Privilege privilege) throws KalipoException {
        Asserts.isNotNull(privilege, "privilege");
        log.info(String.format("%s creates privilege %s", SecurityUtils.getCurrentLogin(), privilege));
        return privilegeRepository.save(privilege);
    }

    @RolesAllowed(Privileges.CREATE_PRIVILEGE)
    @RateLimit
    public Privilege update(Privilege privilege) throws KalipoException {
        Asserts.isNotNull(privilege, "privilege");
        Asserts.isNotNull(privilege.getId(), "id");
        log.info(String.format("%s updates privilege %s to %s", SecurityUtils.getCurrentLogin(), privilege.getId(), privilege));
        return privilegeRepository.save(privilege);
    }

    @Async
    public Future<List<Privilege>> getAll() {
        return new AsyncResult<>(privilegeRepository.findAll());
    }

    @Async
    public Future<Privilege> get(String id) throws KalipoException {
        return new AsyncResult<>(privilegeRepository.findOne(id));
    }

    public void delete(String id) throws KalipoException {
        privilegeRepository.delete(id);
    }

}
