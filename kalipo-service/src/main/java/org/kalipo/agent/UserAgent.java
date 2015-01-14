package org.kalipo.agent;

import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.domain.User;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
@KalipoExceptionHandler
public class UserAgent {

    private final Logger log = LoggerFactory.getLogger(UserAgent.class);

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private UserRepository userRepository;

    // todo midnight
    @Scheduled(fixedDelay = 20000)
    public void setTrustworthiness() {

        try {
            List<User> users = userRepository.findAll();

            for (User user : users) {

                double rejectedCount = commentRepository.getRejectedCommentCountOfUser(user.getLogin());
                double approvedCount = commentRepository.getApprovedCommentCountOfUser(user.getLogin());
                double trustworthiness = approvedCount / Math.max(1, rejectedCount);

                log.debug(String.format("%s with %s approved, %s rejected => %s trustworthiness", user.getLogin(), approvedCount, rejectedCount, trustworthiness));

                user.setTrustworthiness(trustworthiness);
            }

            userRepository.save(users);

        } catch (Exception e) {
            log.error("Failed calculating reputation.", e);
        }
    }

}
