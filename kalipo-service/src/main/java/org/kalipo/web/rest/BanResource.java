package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.kalipo.config.Constants;
import org.kalipo.domain.Ban;
import org.kalipo.service.BanService;
import org.kalipo.service.util.Asserts;
import org.kalipo.service.util.ParamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * REST controller for managing Vote.
 */
@RestController
@RequestMapping("/app")
public class BanResource {

    private final Logger log = LoggerFactory.getLogger(BanResource.class);

    @Inject
    private BanService banService;

    /**
     * POST  /rest/bans -> Create a new ban.
     */
    @RequestMapping(value = "/rest/bans",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new ban")
    public Ban create(@NotNull @RequestBody Ban ban) throws KalipoException {
        log.debug("REST request to save Ban : {}", ban);

        return banService.create(ban);
    }

    /**
     * GET  /rest/bans/:userId -> get bans.
     */
    @RequestMapping(value = "/rest/bans",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get bans for {user}")
    public List<Ban> getUsersBans(
        @QueryParam("siteId") String siteId,
        @QueryParam(Constants.PARAM_PAGE) Integer page
    ) throws ExecutionException, InterruptedException {

        log.debug("REST request to get Bans");

        return banService.getBansWithPages(siteId, ParamUtils.fixPage(page)).get();
    }

    /**
     * GET  /rest/bans/:id -> get the "id" ban.
     */
    @RequestMapping(value = "/rest/bans/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get the \"id\" ban.")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid ID supplied"),
        @ApiResponse(code = 404, message = "Ban not found")
    })
    public ResponseEntity<Ban> get(@PathVariable String id) throws KalipoException, ExecutionException, InterruptedException {
        log.debug("REST request to get Ban : {}", id);
        Asserts.isNotNull(id, "id");

        return Optional.ofNullable(banService.get(id))
            .map(ban -> new ResponseEntity<>(
                ban,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /rest/bans/:id -> delete the "id" ban.
     */
    @RequestMapping(value = "/rest/bans/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete the \"id\" ban")
    public void delete(@PathVariable String id) throws KalipoException {
        log.debug("REST request to delete Ban : {}", id);

        Asserts.isNotNull(id, "id");

        banService.delete(id);
    }
}
