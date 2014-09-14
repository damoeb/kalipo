package org.kalipo.service;

import org.kalipo.domain.Comment;
import org.kalipo.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class CommentService {

    private final Logger log = LoggerFactory.getLogger(CommentService.class);

    @Inject
    private CommentRepository commentRepository;

    public void create(Comment comment) {
        // todo remove id
        comment.setAuthorId("d");
        comment.setStatus(Comment.Status.APPROVED);
        commentRepository.save(comment);
    }

    public void save(Comment comment) {
        comment.setAuthorId("d");
        comment.setStatus(Comment.Status.APPROVED);
        commentRepository.save(comment);
    }

    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    public Comment findOne(String id) {
        return commentRepository.findOne(id);
    }

    public void delete(String id) {
        commentRepository.delete(id);
    }
}
