package org.kalipo.service;

import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.domain.Privilege;
import org.kalipo.repository.PrivilegeRepository;
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
public class PrivilegeService {

    private final Logger log = LoggerFactory.getLogger(PrivilegeService.class);

    @Inject
    private PrivilegeRepository privilegeRepository;

    @RolesAllowed(Privileges.CREATE_PRIVILEGE)
    public void create(Privilege privilege) throws KalipoRequestException {

        // todo id must not exist id
        privilegeRepository.save(privilege);
    }

    @RolesAllowed(Privileges.CREATE_PRIVILEGE)
    public void update(Privilege privilege) throws KalipoRequestException {

        privilegeRepository.save(privilege);
    }

    public List<Privilege> getAll() {
        return privilegeRepository.findAll();
    }

    public Privilege get(String id) throws KalipoRequestException {
        return privilegeRepository.findOne(id);
    }

    public void delete(String id) throws KalipoRequestException {
        privilegeRepository.delete(id);
    }

}
