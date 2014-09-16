package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.kalipo.domain.Vote;
import org.kalipo.repository.VoteRepository;
import org.kalipo.security.SecurityUtils;
import org.kalipo.web.rest.dto.VoteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Vote.
 */
@RestController
@RequestMapping("/app")
public class VoteResource {

    private final Logger log = LoggerFactory.getLogger(VoteResource.class);

    @Inject
    private VoteRepository voteRepository;

    /**
     * POST  /rest/votes -> Create a new vote.
     */
    @RequestMapping(value = "/rest/votes",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new vote")
    public void create(@Valid @RequestBody VoteDTO voteDTO) {
        log.debug("REST request to save Vote : {}", voteDTO);
        Vote vote = new Vote();
        BeanUtils.copyProperties(voteDTO, vote);

        vote.setAuthorId(SecurityUtils.getCurrentLogin());
        voteRepository.save(vote);
    }

    /**
     * PUT  /rest/votes -> Update existing vote.
     */
    @RequestMapping(value = "/rest/votes/{id}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Update existing vote")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Vote not found")
    })
    public void update(@PathVariable String id, @Valid @RequestBody VoteDTO voteDTO) throws KalipoRequestException {
        log.debug("REST request to update Vote : {}", voteDTO);

        if (StringUtils.isBlank(id)) {
            throw new IllegalParameterException();
        }

        Vote vote = new Vote();
        BeanUtils.copyProperties(voteDTO, vote);
        vote.setId(id);
        vote.setAuthorId(SecurityUtils.getCurrentLogin());
        voteRepository.save(vote);
    }

    /**
     * GET  /rest/votes -> get all the votes.
     */
    @RequestMapping(value = "/rest/votes",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get all the votes")
    public List<Vote> getAll() {
        log.debug("REST request to get all Votes");
        return voteRepository.findAll();
    }

    /**
     * GET  /rest/votes/:id -> get the "id" vote.
     */
    @RequestMapping(value = "/rest/votes/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get the \"id\" vote.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Vote not found")
    })
    public ResponseEntity<Vote> get(@PathVariable String id) throws KalipoRequestException {
        log.debug("REST request to get Vote : {}", id);
        if (StringUtils.isBlank(id)) {
            throw new IllegalParameterException();
        }

        return Optional.ofNullable(voteRepository.findOne(id))
                .map(vote -> new ResponseEntity<>(
                        vote,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /rest/votes/:id -> delete the "id" vote.
     */
    @RequestMapping(value = "/rest/votes/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete the \"id\" vote")
    public void delete(@PathVariable String id) throws KalipoRequestException {
        log.debug("REST request to delete Vote : {}", id);

        if (StringUtils.isBlank(id)) {
            throw new IllegalParameterException();
        }

        voteRepository.delete(id);
    }
}
