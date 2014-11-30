package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.kalipo.domain.Feedback;
import org.kalipo.service.FeedbackService;
import org.kalipo.service.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Feedback.
 * <p>
 */
@RestController
@RequestMapping("/app")
public class FeedbackResource {

    private final Logger log = LoggerFactory.getLogger(FeedbackResource.class);

    @Inject
    private FeedbackService feedbackService;

    @Inject
    private HttpServletRequest request;

    /**
     * POST  /rest/tags -> Create a new tag.
     */
    @RequestMapping(value = "/rest/feedbacks",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new feedback")
    public Feedback create(@NotNull @RequestBody Feedback feedback) throws KalipoException {
        log.debug("REST request to save Feedback : {}", feedback);

        Asserts.isNull(feedback.getId(), "id");

        feedback.setIp(request.getRemoteHost());

        return feedbackService.create(feedback);
    }

    /**
     * GET  /rest/feedbacks -> get all the feedbacks.
     */
    @RequestMapping(value = "/rest/feedbacks",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get all the feedbacks")
    public List<Feedback> getAll() throws KalipoException {
        log.debug("REST request to get all Feedbacks");
        return feedbackService.getAll();
    }

    /**
     * GET  /rest/feedbacks/:id -> get the "id" feedback.
     */
    @RequestMapping(value = "/rest/feedbacks/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get the \"id\" feedback.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Feedback not found")
    })
    public ResponseEntity<Feedback> get(@PathVariable String id) throws KalipoException {
        log.debug("REST request to get Feedback : {}", id);
        Asserts.isNotNull(id, "id");

        return Optional.ofNullable(feedbackService.get(id))
                .map(feedback -> new ResponseEntity<>(
                        feedback,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /rest/feedbacks/:id -> delete the "id" feedback.
     */
    @RequestMapping(value = "/rest/feedbacks/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete the \"id\" feedback")
    public void delete(@PathVariable String id) throws KalipoException {
        log.debug("REST request to delete Feedback : {}", id);
        Asserts.isNotNull(id, "id");

        feedbackService.delete(id);
    }
}
