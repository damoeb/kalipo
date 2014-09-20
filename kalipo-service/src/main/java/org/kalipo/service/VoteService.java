package org.kalipo.service;

import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.domain.Vote;
import org.kalipo.repository.VoteRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.web.rest.KalipoRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.util.List;

@Service
@EnableArgumentValidation
public class VoteService {

    private final Logger log = LoggerFactory.getLogger(VoteService.class);

    @Inject
    private VoteRepository voteRepository;

    @RolesAllowed(Privileges.CREATE_VOTE)
    public void create(Vote vote) throws KalipoRequestException {

        // todo id must not exist id

        vote.setAuthorId(SecurityUtils.getCurrentLogin());

        voteRepository.save(vote);
    }

    @RolesAllowed(Privileges.CREATE_VOTE)
    public void update(Vote vote) throws KalipoRequestException {

        vote.setAuthorId(SecurityUtils.getCurrentLogin());

        voteRepository.save(vote);
    }

    public List<Vote> getAll() {
        return voteRepository.findAll();
    }

    public Vote get(String id) throws KalipoRequestException {
        return voteRepository.findOne(id);
    }

    public void delete(String id) throws KalipoRequestException {
        voteRepository.delete(id);
    }

}
