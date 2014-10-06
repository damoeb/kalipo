package org.kalipo.service;

import org.kalipo.aop.EnableArgumentValidation;
import org.kalipo.aop.Throttled;
import org.kalipo.domain.Vote;
import org.kalipo.repository.VoteRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.web.rest.KalipoRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.Future;

@Service
@EnableArgumentValidation
public class VoteService {

    private final Logger log = LoggerFactory.getLogger(VoteService.class);

    @Inject
    private VoteRepository voteRepository;

    @Inject
    private ReputationService reputationService;

    @RolesAllowed(Privileges.CREATE_VOTE)
    @Throttled
    public void create(@Valid Vote vote) throws KalipoRequestException {

        // todo id must not exist id

        // todo check that comment exists

        reputationService.likeOrDislikeComment(vote);

        vote.setAuthorId(SecurityUtils.getCurrentLogin());

        voteRepository.save(vote);
    }

    @Async
    public Future<List<Vote>> getAll() {
        return new AsyncResult<>(voteRepository.findAll());
    }

    @Async
    public Future<Vote> get(String id) throws KalipoRequestException {
        return new AsyncResult<>(voteRepository.findOne(id));
    }

    public void delete(String id) throws KalipoRequestException {
        voteRepository.delete(id);
    }
}
