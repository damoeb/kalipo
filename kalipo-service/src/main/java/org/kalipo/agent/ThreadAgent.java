package org.kalipo.agent;

import org.joda.time.DateTime;
import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.domain.Thread;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.ThreadRepository;
import org.kalipo.repository.VoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Scheduled jobs for Thread entity
 * Created by damoeb on 13.07.15.
 */
@Service
@KalipoExceptionHandler
public class ThreadAgent {

    private final Logger log = LoggerFactory.getLogger(ThreadAgent.class);

    @Inject
    private ThreadRepository threadRepository;

    @Inject
    private VoteRepository voteRepository;

    @Inject
    private CommentRepository commentRepository;

    @Scheduled(fixedDelay = 20000)
    public void updateThreadStats() {

        Sort sort = new Sort(Sort.Direction.ASC, "lastModifiedDate");
        PageRequest request = new PageRequest(0, 10, sort);

        List<Thread> threads = threadRepository.findByStatusAndReadOnly(Thread.Status.OPEN, false, request);
        for (Thread thread : threads) {
            log.debug("Updating stats of thread {}", thread.getId());

            thread.setLikes(voteRepository.countLikesOfThread(thread.getId()));
            thread.setCommentCount(commentRepository.countApprovedInThread(thread.getId()));
            thread.setPendingCount(commentRepository.countPendingInThread(thread.getId()));
            thread.setLastModifiedDate(DateTime.now());

//          todo likes, unique authors, views

            threadRepository.save(thread);
        }
    }
}
