package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.kalipo.domain.ReputationModifier;
import org.kalipo.service.ReputationModifierService;
import org.kalipo.service.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * REST controller for managing Privilege.
 */
@RestController
@RequestMapping("/app/rest")
public class ReputationModifierResource {

    private final Logger log = LoggerFactory.getLogger(ReputationModifierResource.class);

    @Inject
    private ReputationModifierService reputationModifierService;

    /**
     * PUT  /rest/reputations -> Update existing reputation.
     */
    @RequestMapping(value = "/reputations/{id}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Update existing reputation")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Reputation not found")
    })
    public void update(@PathVariable String id, @NotNull @RequestBody ReputationModifier reputationModifier) throws KalipoException {
        log.debug("REST request to update Reputation : {}", reputationModifier);

        Asserts.isNotNull(id, "id");

        reputationModifier.setId(id);
        reputationModifierService.update(reputationModifier);
    }

    /**
     * GET  /rest/reputations -> get all the reputations.
     */
    @RequestMapping(value = "/reputations",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get all the reputations")
    public List<ReputationModifier> getAll() throws ExecutionException, InterruptedException {
        log.debug("REST request to get all Reputations");
        return reputationModifierService.getAll().get();
    }

    /**
     * GET  /rest/reputations/:id -> get the "id" reputation.
     */
    @RequestMapping(value = "/reputations/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get the \"id\" reputation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Reputation not found")
    })
    public ResponseEntity<ReputationModifier> get(@PathVariable String id) throws KalipoException, ExecutionException, InterruptedException {
        log.debug("REST request to get Reputation : {}", id);
        Asserts.isNotNull(id, "id");

        return Optional.ofNullable(reputationModifierService.get(id).get())
                .map(reputation -> new ResponseEntity<>(
                        reputation,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
