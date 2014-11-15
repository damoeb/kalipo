package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.kalipo.domain.Comment;
import org.kalipo.service.CommentService;
import org.kalipo.service.util.Asserts;
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
     * PUT  /rest/comments/{id}/approve -> Update existing comment.
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
     * GET  /rest/comments/review -> get all the comments, that have to be reviewed.
     */
    @RequestMapping(value = "/rest/comments/review",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get all the comments for {user}, that it can review")
    public List<Comment> getAllUnderReview(@QueryParam("userId") String userId, @QueryParam("page") Integer page) throws ExecutionException, InterruptedException {
        log.debug("REST request to get all Comments, that have to be reviewed");

        return commentService.findAllUnderReview(page).get();//.stream().filter(Comment::getHidden).collect(Collectors.toList());
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
