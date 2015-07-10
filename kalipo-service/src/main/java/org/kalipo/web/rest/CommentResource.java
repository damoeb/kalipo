package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.kalipo.config.Constants;
import org.kalipo.domain.Comment;
import org.kalipo.service.CommentService;
import org.kalipo.service.util.Asserts;
import org.kalipo.service.util.ParamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * REST controller for managing Comment.
 * <p>
 */
@RestController
@RequestMapping("/app")
public class CommentResource {

    private final Logger log = LoggerFactory.getLogger(CommentResource.class);

    @Inject
    private CommentService commentService;

    /**
     * POST  /rest/comments -> Create a new comment.
     */
    @RequestMapping(value = "/rest/comments",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new comment")
    public Comment create(@NotNull @RequestBody Comment comment) throws KalipoException {
        log.debug("REST request to save Comment : {}", comment);

        return commentService.create(comment);
    }

    /**
     * PUT  /rest/comments -> Update existing comment.
     */
    @RequestMapping(value = "/rest/comments/{id}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Update existing comment")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Comment not found")
    })
    public Comment update(@PathVariable String id, @NotNull @RequestBody Comment comment) throws KalipoException {
        log.debug("REST request to update Comment : {}", comment);

        Asserts.isNotNull(id, "id");

        comment.setId(id);
        return commentService.update(comment);
    }

    /**
     * PUT  /rest/comments/{id}/approve -> Approve existing comment.
     */
    @RequestMapping(value = "/rest/comments/{id}/approve",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Approve comment")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Comment not found")
    })
    public Comment approve(@PathVariable String id) throws KalipoException {
        log.debug("REST request to approve Comment : {}", id);

        return commentService.approve(id);
    }

    /**
     * PUT  /rest/comments/{id}/reject -> Reject existing comment.
     */
    @RequestMapping(value = "/rest/comments/{id}/reject",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Reject comment")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid ID supplied"),
        @ApiResponse(code = 404, message = "Comment not found")
    })
    public Comment reject(@PathVariable String id) throws KalipoException {
        log.debug("REST request to reject Comment : {}", id);

        return commentService.reject(id);
    }

    /**
     * PUT  /rest/comments/{id}/spam -> Label existing comment as "Spam".
     */
    @RequestMapping(value = "/rest/comments/{id}/spam",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Label comment as spam")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid ID supplied"),
        @ApiResponse(code = 404, message = "Comment not found")
    })
    public Comment spam(@PathVariable String id) throws KalipoException {
        log.debug("REST request to label Comment as spam: {}", id);

        return commentService.spam(id);
    }

    /**
     * PUT  /rest/comments/{id}/delete+ban -> Label existing comment as "Spam".
     */
    @RequestMapping(value = "/rest/comments/{id}/delete+ban",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Delete comment and ban user from thread")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid ID supplied"),
        @ApiResponse(code = 404, message = "Comment not found")
    })
    public Comment deleteAndBan(@PathVariable String id) throws KalipoException {
        log.debug("REST request to delete Comment and ban user: {}", id);

        return commentService.deleteAndBan(id);
    }

    /**
     * GET  /rest/comments/:id -> get the "id" comment.
     */
    @RequestMapping(value = "/rest/comments/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get the \"id\" comment.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Comment not found")
    })
    public ResponseEntity<Comment> get(@PathVariable String id) throws KalipoException, ExecutionException, InterruptedException {
        log.debug("REST request to get Comment : {}", id);
        Asserts.isNotNull(id, "id");

        return Optional.ofNullable(commentService.get(id).get())
                .map(comment -> new ResponseEntity<>(
                        comment,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET  /rest/comments -> get the filtered comments.
     */
    @RequestMapping(value = "/rest/comments",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get the filtered comments.")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid ID supplied"),
        @ApiResponse(code = 404, message = "Comment not found")
    })
    public ResponseEntity<Page<Comment>> filtered(@QueryParam("userId") String userId, @QueryParam("status") Comment.Status status, @QueryParam("reported") Boolean reported, @QueryParam("page") @DefaultValue("0") int page) throws KalipoException, ExecutionException, InterruptedException {
        log.debug("REST request to get filtered Comment : {}", userId);

        return Optional.ofNullable(commentService.filtered(userId, status, reported, page))
            .map(comments -> new ResponseEntity<>(
                comments,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

    /**
     * DELETE  /rest/comments/:id -> delete the "id" comment.
     */
    @RequestMapping(value = "/rest/comments/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete the \"id\" comment")
    public void delete(@PathVariable String id) throws KalipoException {
        log.debug("REST request to delete Comment : {}", id);

        Asserts.isNotNull(id, "id");

        commentService.delete(id);
    }
}
