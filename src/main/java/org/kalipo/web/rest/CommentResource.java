package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.kalipo.domain.Comment;
import org.kalipo.repository.CommentRepository;
import org.kalipo.security.SecurityUtils;
import org.kalipo.web.rest.dto.CommentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.QueryParam;
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
    private CommentRepository commentRepository;

    /**
     * POST  /rest/comments -> Create a new comment.
     */
    @RequestMapping(value = "/rest/comments",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void create(@RequestBody CommentDTO commentDTO) {
        log.debug("REST request to save Comment : {}", commentDTO);

        Comment comment = CommentDTO.convert(commentDTO);
        comment.setAuthorId("d");
        comment.setStatus(Comment.Status.APPROVED);
        commentRepository.save(comment);
    }

    /**
     * PUT  /rest/comments -> Update existing comment.
     */
    @RequestMapping(value = "/rest/comments",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void update(@Valid @RequestBody CommentDTO commentDTO) {
        log.debug("REST request to update Comment : {}", commentDTO);
        commentRepository.save(CommentDTO.convert(commentDTO));
    }

    /**
     * GET  /rest/comments -> get all the comments.
     */
    @RequestMapping(value = "/rest/comments",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Comment> getAll() {
//      todo impl pagination  @QueryParam("offset") int offset, @QueryParam("size") int size
        log.debug("REST request to get all Comments");
        return commentRepository.findAll();
    }

    /**
     * GET  /rest/comments/:id -> get the "id" comment.
     */
    @RequestMapping(value = "/rest/comments/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Comment> get(@PathVariable String id) {
        log.debug("REST request to get Comment : {}", id);
        return Optional.ofNullable(commentRepository.findOne(id))
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
    public void delete(@PathVariable String id) {
        log.debug("REST request to delete Comment : {}", id);
        commentRepository.delete(id);
    }
}
