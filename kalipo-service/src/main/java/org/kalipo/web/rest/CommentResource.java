package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.kalipo.domain.Comment;
import org.kalipo.service.CommentService;
import org.kalipo.web.rest.dto.CommentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Comment.
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
    public void create(@Valid @RequestBody CommentDTO commentDTO) throws KalipoRequestException {
        log.debug("REST request to save Comment : {}", commentDTO);

        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment);

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
    public void update(@PathVariable String id, @Valid @RequestBody CommentDTO commentDTO) throws KalipoRequestException {
        log.debug("REST request to update Comment : {}", commentDTO);

        if (StringUtils.isBlank(id)) {
            throw new InvalidParameterException("id");
        }

        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment);

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
    public List<CommentDTO> getAll() {
//      todo impl pagination  @QueryParam("offset") int offset, @QueryParam("size") int size
        log.debug("REST request to get all Comments");

        List<CommentDTO> list = new LinkedList<>();
        commentService.findAll().forEach(comment -> list.add(new CommentDTO().fields(comment)));

        return list;
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
    public ResponseEntity<CommentDTO> get(@PathVariable String id) throws KalipoRequestException {
        log.debug("REST request to get Comment : {}", id);
        if (StringUtils.isBlank(id)) {
            throw new InvalidParameterException("id");
        }

        return Optional.ofNullable(commentService.get(id))
                .map(comment -> new ResponseEntity<>(
                        new CommentDTO().fields(comment),
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
