package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import org.kalipo.domain.Vote;
import org.kalipo.service.VoteService;
import org.kalipo.service.util.ParamFixer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * REST controller for managing Vote.
 */
@RestController
@RequestMapping("/app")
public class VoteResource {

    private final Logger log = LoggerFactory.getLogger(VoteResource.class);

    @Inject
    private VoteService voteService;

    /**
     * POST  /rest/votes -> Create a new vote.
     */
    @RequestMapping(value = "/rest/votes",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new vote")
    public Vote create(@NotNull @RequestBody Vote vote) throws KalipoException {
        log.debug("REST request to save Vote : {}", vote);

        return voteService.create(vote);
    }

    /**
     * GET  /rest/votes/:userId -> get votes of user.
     */
    @RequestMapping(value = "/rest/votes/{userId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get votes for {user}")
    public List<Vote> getUsersVotes(@Valid @NotNull @PathVariable String userId, @QueryParam("page") Integer page) throws ExecutionException, InterruptedException {

        log.debug("REST request to get Votes of user");

        return voteService.getVotes(userId, ParamFixer.fixPage(page)).get();
    }

    /**
     * GET  /rest/votes/:id -> get the "id" vote.
     */
//    @RequestMapping(value = "/rest/votes/{id}",
//            method = RequestMethod.GET,
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Timed
//    @ApiOperation(value = "Get the \"id\" vote.")
//    @ApiResponses(value = {
//            @ApiResponse(code = 400, message = "Invalid ID supplied"),
//            @ApiResponse(code = 404, message = "Vote not found")
//    })
//    public ResponseEntity<Vote> get(@PathVariable String id) throws KalipoException, ExecutionException, InterruptedException {
//        log.debug("REST request to get Vote : {}", id);
//        Asserts.isNotNull(id, "id");
//
//        return Optional.ofNullable(voteService.get(id).get())
//                .map(vote -> new ResponseEntity<>(
//                        vote,
//                        HttpStatus.OK))
//                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
//    }

    /**
     * DELETE  /rest/votes/:id -> delete the "id" vote.
     */
//    @RequestMapping(value = "/rest/votes/{id}",
//            method = RequestMethod.DELETE,
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    @Timed
//    @ResponseStatus(HttpStatus.OK)
//    @ApiOperation(value = "Delete the \"id\" vote")
//    public void delete(@PathVariable String id) throws KalipoException {
//        log.debug("REST request to delete Vote : {}", id);
//
//        Asserts.isNotNull(id, "id");
//
//        voteService.delete(id);
//    }
}
