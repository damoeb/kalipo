package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.kalipo.domain.Thread;
import org.kalipo.service.ThreadService;
import org.kalipo.web.rest.dto.ThreadDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Thread.
 */
@RestController
@RequestMapping("/app/rest")
public class ThreadResource extends BaseResource<Thread, ThreadDTO> {

    private final Logger log = LoggerFactory.getLogger(ThreadResource.class);

    @Inject
    private ThreadService threadService;

    /**
     * POST  /rest/threads -> Create a new thread.
     */
    @RequestMapping(value = "/threads",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new thread")
    public void create(@Valid @RequestBody ThreadDTO threadDTO) throws KalipoRequestException {
        log.debug("REST request to save Thread : {}", threadDTO);

        threadService.create(toOrigin(threadDTO));
    }

    /**
     * PUT  /rest/threads -> Update existing thread.
     */
    @RequestMapping(value = "/threads/{id}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Update existing thread")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Thread not found")
    })
    public void update(@PathVariable String id, @Valid @RequestBody ThreadDTO threadDTO) throws KalipoRequestException {
        log.debug(" request to update Thread : {}", threadDTO);

        if (StringUtils.isBlank(id)) {
            throw new InvalidParameterException("id");
        }

        Thread thread = toOrigin(threadDTO);

        thread.setId(id);
        threadService.update(thread);
    }

    /**
     * GET  /rest/threads -> get all the threads.
     */
    @RequestMapping(value = "/threads",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get all the threads")
    public List<ThreadDTO> getAll() {
        log.debug("REST request to get all Threads");

        List<ThreadDTO> list = new LinkedList<>();
        threadService.getAll().forEach(thread -> list.add(fromOrigin(thread)));

        return list;
    }

    /**
     * GET  /rest/threads/:id -> get the "id" thread.
     */
    @RequestMapping(value = "/threads/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get the \"id\" thread.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Thread not found")
    })
    public ResponseEntity<ThreadDTO> get(@PathVariable String id) throws KalipoRequestException {
        log.debug("REST request to get Thread : {}", id);
        if (StringUtils.isBlank(id)) {
            throw new InvalidParameterException("id");
        }

        return Optional.ofNullable(threadService.get(id))
                .map(thread -> new ResponseEntity<>(
                        fromOrigin(thread),
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /rest/threads/:id -> delete the "id" thread.
     */
    @RequestMapping(value = "/threads/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete the \"id\" thread")
    public void delete(@PathVariable String id) throws KalipoRequestException {
        log.debug("REST request to delete Thread : {}", id);
        if (StringUtils.isBlank(id)) {
            throw new InvalidParameterException("id");
        }

        threadService.delete(id);
    }

    // --

    @Override
    protected ThreadDTO newDTOInstance() {
        return new ThreadDTO();
    }

    @Override
    protected Thread newOriginInstance() {
        return new Thread();
    }
}
