package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.kalipo.domain.Privilege;
import org.kalipo.service.PrivilegeService;
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
public class PrivilegeResource {

    private final Logger log = LoggerFactory.getLogger(PrivilegeResource.class);

    @Inject
    private PrivilegeService privilegeService;

    /**
     * POST  /rest/privileges -> Create a new privilege.
     */
    @RequestMapping(value = "/privileges",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new privilege")
    public void create(@NotNull @RequestBody Privilege privilege) throws KalipoRequestException {
        log.debug("REST request to save Privilege : {}", privilege);
        privilegeService.create(privilege);
    }

    /**
     * PUT  /rest/privileges -> Update existing privilege.
     */
    @RequestMapping(value = "/privileges/{id}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Update existing privilege")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Privilege not found")
    })
    public void update(@PathVariable String id, @NotNull @RequestBody Privilege privilege) throws KalipoRequestException {
        log.debug("REST request to update Privilege : {}", privilege);

        if (StringUtils.isBlank(id)) {
            throw new InvalidParameterException("id");
        }

        privilege.setId(id);
        privilegeService.update(privilege);
    }

    /**
     * GET  /rest/privileges -> get all the privileges.
     */
    @RequestMapping(value = "/privileges",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get all the privileges")
    public List<Privilege> getAll() throws ExecutionException, InterruptedException {
        log.debug("REST request to get all Privileges");
        return privilegeService.getAll().get();
    }

    /**
     * GET  /rest/privileges/:id -> get the "id" privilege.
     */
    @RequestMapping(value = "/privileges/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get the \"id\" privilege.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Privilege not found")
    })
    public ResponseEntity<Privilege> get(@PathVariable String id) throws KalipoRequestException, ExecutionException, InterruptedException {
        log.debug("REST request to get Privilege : {}", id);
        if (StringUtils.isBlank(id)) {
            throw new InvalidParameterException("id");
        }

        return Optional.ofNullable(privilegeService.get(id).get())
                .map(privilege -> new ResponseEntity<>(
                        privilege,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /rest/privileges/:id -> delete the "id" privilege.
     */
    @RequestMapping(value = "/privileges/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete the \"id\" privilege")
    public void delete(@PathVariable String id) throws KalipoRequestException {
        log.debug("REST request to delete Privilege : {}", id);
        if (StringUtils.isBlank(id)) {
            throw new InvalidParameterException("id");
        }

        privilegeService.delete(id);
    }
}
