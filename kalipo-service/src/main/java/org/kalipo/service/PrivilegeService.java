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
import org.springframework.data.domain.Sort;
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

    @RolesAllowed(Privileges.UPDATE_PRIVILEGE)
    @RateLimit
    public Privilege update(Privilege privilege) throws KalipoException {
        Asserts.isNotNull(privilege, "privilege");
        Asserts.isNotNull(privilege.getId(), "id");
        Asserts.isNotNull(privilege.getReputation(), "reputation");
        Privilege original = privilegeRepository.findOne(privilege.getId());
        Asserts.isNotNull(original, "id");
        Asserts.nullOrEqual(privilege.getName(), original.getName(), "name");
        log.info(String.format("User '%s' changes privilege %s to %s (before: %s)", SecurityUtils.getCurrentLogin(), original.getId(), privilege.getReputation(), original.getReputation()));
        original.setReputation(privilege.getReputation());
        return privilegeRepository.save(original);
    }

    @Async
    public Future<List<Privilege>> getAll() {
        Sort sort = new Sort(Sort.Direction.ASC, "name");
        return new AsyncResult<>(privilegeRepository.findAll(sort));
    }

    @Async
    public Future<Privilege> get(String id) throws KalipoException {
        return new AsyncResult<>(privilegeRepository.findOne(id));
    }
}
