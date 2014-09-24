package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.kalipo.domain.Comment;
import org.kalipo.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * REST controller for managing Comment.
 *
 * todo: delete your comment: reputation -50
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
    public void create(@NotNull @RequestBody Comment comment) throws KalipoRequestException {
        log.debug("REST request to save Comment : {}", comment);

        commentService.create(comment);
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
    public void update(@PathVariable String id, @NotNull @RequestBody Comment comment) throws KalipoRequestException {
        log.debug("REST request to update Comment : {}", comment);

        if (StringUtils.isBlank(id)) {
            throw new InvalidParameterException("id");
        }

        comment.setId(id);
        commentService.update(comment);
    }

    /**
     * GET  /rest/comments -> get all the comments.
     */
    @RequestMapping(value = "/rest/comments",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get all the comments")
    public List<Comment> getAll() throws ExecutionException, InterruptedException {
//      todo impl pagination  @QueryParam("offset") int offset, @QueryParam("size") int size
        log.debug("REST request to get all Comments");

        return commentService.findAll().get();
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
    public ResponseEntity<Comment> get(@PathVariable String id) throws KalipoRequestException, ExecutionException, InterruptedException {
        log.debug("REST request to get Comment : {}", id);
        if (StringUtils.isBlank(id)) {
            throw new InvalidParameterException("id");
        }

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
    public void delete(@PathVariable String id) throws KalipoRequestException {
        log.debug("REST request to delete Comment : {}", id);

        if (StringUtils.isBlank(id)) {
            throw new InvalidParameterException("id");
        }

        commentService.delete(id);
    }
}
