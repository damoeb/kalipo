package org.kalipo.agent;

import org.apache.commons.lang.BooleanUtils;
import org.joda.time.DateTime;
import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Site;
import org.kalipo.domain.Thread;
import org.kalipo.domain.User;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.SiteRepository;
import org.kalipo.repository.ThreadRepository;
import org.kalipo.service.NotificationService;
import org.kalipo.service.UserService;
import org.kalipo.service.util.BroadcastUtils;
import org.kalipo.service.util.NumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleFunction;

/**
 * Scheduled jobs for Comment entity
 */
@Service
@KalipoExceptionHandler
public class CommentAgent {

    private final Logger log = LoggerFactory.getLogger(CommentAgent.class);

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private SiteRepository siteRepository;

    @Inject
    private ThreadRepository threadRepository;

    @Inject
    private UserService userService;

    @Inject
    private NotificationService notificationService;

    @Scheduled(fixedDelay = 2000)
    public void setStatusAndQuality() {

        try {
            final PageRequest pageable = new PageRequest(0, 50);

            // todo create SVM with all approved comments

            Page<Comment> listWithNoneStatus = commentRepository.findByStatus(Comment.Status.NONE, pageable);

            if (listWithNoneStatus.hasContent()) {

                /*
                 todo difference between quality and influence?
                 quality: quality of comment refrlecting author and
                */

//                    todo use SVM of R to classify spam
//                    List<Comment> approved = commentRepository.findByStatusAndThreadId(Comment.Status.APPROVED, thread.getId());

                for (Comment comment : listWithNoneStatus) {

                    Thread thread = threadRepository.findOne(comment.getThreadId());
                    Site site = siteRepository.findOne(thread.getSiteId());

                    final String authorId = comment.getAuthorId();
                    final boolean isSuperMod = userService.isSuperMod(authorId);

                    final boolean isMod = site.getModeratorIds().contains(authorId);

                    User author = userService.findOne(authorId);

                    double innovative = 1d; // todo compare to other approved comments in this thread using SVM
//                        double spam = 0d;
                    double quality = author.getTrustworthiness();

                    // todo map quality in [0..1]
                    comment.setQuality(quality);

//                        if (spam > 0.8d) {
////                            comment.setStatus(Comment.Status.SPAM);
//                            log.info(String.format("%s creates spam comment %s ", authorId, comment.toString()));
//
//                        } else {

                    if (isMod || isSuperMod) {
                        comment.setStatus(Comment.Status.APPROVED);

                        BroadcastUtils.broadcast(BroadcastUtils.Type.COMMENT, comment);

                        onApproval(comment);

                        log.info(String.format("Auto-approved comment %s cause author is mod", comment.getId()));

                    } else if (quality > 0.5) {
                        Comment.Status status;
                        if (excessiveUpperCase(comment)) {
                            status = Comment.Status.REJECTED;
                            comment.setReviewMsg("Excessive upper-case usage");
                            log.info(String.format("Auto-rejected comment %s, due to excessive uppercase usage", comment.getId()));
                            notificationService.announceCommentRejected(comment);

                        } else if (excessiveSpecialChars(comment)) {
                            status = Comment.Status.REJECTED;
                            comment.setReviewMsg("Excessive special-char usage");
                            log.info(String.format("Auto-rejected comment %s, due to excessive special chars usage", comment.getId()));
                            notificationService.announceCommentRejected(comment);

                        } else {
                            status = Comment.Status.APPROVED;
                            onApproval(comment);
                            log.info(String.format("Auto-approved comment %s, due to good quality %s", comment.getId(), quality));
                        }

                        comment.setStatus(status);

                    } else {
                        comment.setStatus(Comment.Status.PENDING);
                        log.info(String.format("Pending comment %s (q:%s)", comment.getId(), quality));

                        notificationService.announcePendingComment(thread, comment);
                    }
                }

                commentRepository.save(listWithNoneStatus);
            }

        } catch (Exception e) {
            log.error("Influence estimation failed.", e);
        }
    }

    private void onApproval(Comment comment) {
        final String authorId = getAuthorIdRespectingAnonymity(comment);
        notificationService.notifyMentionedUsers(comment, authorId);

        if (comment.getParentId() != null) {
            notificationService.notifyAuthorOfParent(comment, authorId);
        }
    }

    private String getAuthorIdRespectingAnonymity(Comment comment) {
        return BooleanUtils.isTrue(comment.getAnonymous()) ? "Anon" : comment.getAuthorId();
    }

    private boolean excessiveSpecialChars(Comment comment) {
        // todo implement: reject comments only written in UPPER CASE or so
        return false;
    }

    private boolean excessiveUpperCase(Comment comment) {
        String text = comment.getBody();
        long ucCount = text.chars().filter(Character::isUpperCase).count();
        int len = text.length();
        return len > 20 && ucCount > 15;
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void estimateCommentsInfluence() {

        try {
            /*
            Notes from "Identifying the Influential Bloggers in a Community"
            */
            // weight incoming
            final DoubleFunction<Double> w_in = influence -> log(influence) * 1.7;
            // weight outgoing
            final DoubleFunction<Double> w_out = influence -> log(influence) * 1.7;
            // weight dislikes
            final DoubleFunction<Double> w_dislikes = dislikes -> log(dislikes) * 1.7;
            // weight likes
            final DoubleFunction<Double> w_likes = likes -> log(likes) * 1.7;

            Sort sortByDate = new Sort(Sort.Direction.ASC, "lastModifiedDate");
            PageRequest pageable = new PageRequest(0, 10, sortByDate);

            List<Thread> threads = threadRepository.findByStatus(Thread.Status.OPEN, pageable);
            for (Thread thread : threads) {

                thread.setLastModifiedDate(DateTime.now());
                threadRepository.save(thread);

                Sort sortByLevel = new Sort(Sort.Direction.DESC, "level");
                List<Comment> comments = commentRepository.findByThreadId(thread.getId(), sortByLevel);

                log.debug(String.format("Updating comment-influence of thread %s with %s comments", thread.getId(), comments.size()));

//                Map<String,Comment> thisComments = new HashMap<>(comments.size() * 2);
//                comments.forEach(c -> thisComments.put(c.getId(), c));

                // id to influence map
                final Map<String, Double> influenceMap = new HashMap<>();

                // todo influence should be calculated in one loop, not several
                int changed = 0;
                for (Comment comment : comments) {

                    // = replies
                    Set<Comment> i = commentRepository.findByParentId(comment.getId()); //iota - incoming influence

//                    double transitiveInfluence = w_in.apply(influence_incoming(i, influenceMap));// - w_out.apply(NumUtils.nullToZero(θ == null ? null : θ.getInfluence()));

                    // = parent, linked
//                    Comment θ = comment.getParentId()==null ? null : thisComments.get(comment.getParentId()); //theta - outgoing influence
                    Double i_out = 0d; //w_out.apply(NumUtils.nullToZero(θ == null ? null : θ.getInfluence()));
                    int i_inCount = i.isEmpty() ? 0 : 1 + i.size();

                    Double i_in = w_in.apply(i_inCount + influence_incoming(i, influenceMap));
                    double transitiveInfluence = i_in - i_out;

                    // todo include comment.getQuality()
                    double selfInfluence = w_likes.apply(NumUtils.nullToZero(comment.getLikes())) - w_dislikes.apply(NumUtils.nullToZero(comment.getDislikes()));
                    double influence = selfInfluence + transitiveInfluence;

                    influenceMap.put(comment.getId(), influence);

                    if (comment.getInfluence() == null) {
                        changed++;
                        log.debug(String.format("comment %s first influence %s", comment.getId(), influence));
                    } else if (comment.getInfluence() != influence) {
                        changed++;
                        log.debug(String.format("comment %s changed influence (%s, %s) %s -> %s", comment.getId(), i_out, i_in, comment.getInfluence(), influence));
                    }

                    comment.setInfluence(influence);
                }

                if (changed > 0) {
                    log.debug(String.format("influence changed in %s comments in thread %s", changed, thread.getId()));
                    commentRepository.save(comments);
                }
            }
        } catch (Exception e) {
            log.error("Influence estimation failed.", e);
        }
    }

    private Double influence_incoming(Set<Comment> incoming, Map<String, Double> influenceMap) {
        if (incoming.isEmpty()) {
            return 0d;
        }

        double total = 0d;
        for (Comment comment : incoming) {
            if (influenceMap.containsKey(comment.getId())) {
                total += influenceMap.get(comment.getId());
            }
        }
        return total;
    }

    private double log(Double num) {
        return Math.log(Math.max(1, NumUtils.nullToZero(num) + 1));
    }



}
