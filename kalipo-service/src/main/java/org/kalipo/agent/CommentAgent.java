package org.kalipo.agent;

import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Thread;
import org.kalipo.domain.User;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.ThreadRepository;
import org.kalipo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
@KalipoExceptionHandler
public class CommentAgent {

    private final Logger log = LoggerFactory.getLogger(CommentAgent.class);

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private ThreadRepository threadRepository;

    @Inject
    private UserService userService;

    @Scheduled(fixedDelay = 20000)
    public void setStatusAndQuality() {

        try {
            final PageRequest request = new PageRequest(0, 10);

            List<Thread> threads = threadRepository.findOpenWithPending(request);

            for (Thread thread : threads) {

                // todo create SVM with all approved comments

                List<Comment> pendings = commentRepository.findPendingInThread(thread.getId());

                for (Comment comment : pendings) {

                    final String authorId = comment.getAuthorId();
                    final boolean isSuperMod = userService.isSuperMod(authorId);
                    final boolean isMod = thread.getModIds().contains(authorId);

                    User author = userService.findOne(authorId);

                    double innovative = 0d;
                    double spam = 0d;
                    // todo find spam using a SVM
                    // todo weight 40/60
                    // todo normalize
                    double quality = author.getTrustworthiness();
                    comment.setQuality(quality);

                    if (spam > 0.5d) {
                        comment.setStatus(Comment.Status.SPAM);
                        // todo fix comments
                        log.info(String.format("%s creates spam comment %s ", authorId, comment.toString()));

                    } else {
                        if (isMod || isSuperMod || quality > 0.5) {
                            comment.setStatus(Comment.Status.APPROVED);
                            log.info(String.format("%s comment %s ", authorId, comment.toString()));
                        } else {
                            comment.setStatus(Comment.Status.PENDING);
                            log.info(String.format("%s creates pending comment %s ", authorId, comment.toString()));
                        }
                    }


                }

                commentRepository.save(pendings);
            }

        } catch (Exception e) {
            log.error("Influence estimation failed.", e);
        }
    }

}
