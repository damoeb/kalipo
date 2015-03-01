package org.kalipo.service;

import org.apache.commons.lang3.StringUtils;
import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.RateLimit;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Feedback;
import org.kalipo.repository.FeedbackRepository;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
@KalipoExceptionHandler
public class FeedbackService {

    private final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    @Inject
    private UserService userService;

    @Inject
    private MailService mailService;

    @Inject
    private FeedbackRepository feedbackRepository;

    @Inject
    private Environment env;

    @RateLimit
    public Feedback create(Feedback feedback) throws KalipoException {

        Asserts.isNull(feedback.getId(), "id");

        return feedbackRepository.save(feedback);
    }

    public Feedback get(String id) throws KalipoException {
        assertSupermodPrivilege();
        return feedbackRepository.findOne(id);
    }

    public List<Feedback> getAll() throws KalipoException {
        assertSupermodPrivilege();
        return feedbackRepository.findAll();
    }

    public void delete(String id) throws KalipoException {
        assertSupermodPrivilege();
        feedbackRepository.delete(id);
    }

    // --

    @Scheduled(cron = "0 * * * * ?") // hourly
    public void mailFeedbacks() {

        List<Feedback> list = feedbackRepository.findAll();
        if (!list.isEmpty()) {
            StringBuilder content = new StringBuilder(list.size() * 120);
            for (Feedback feedback : list) {
                if (StringUtils.isNotBlank(feedback.getName())) {
                    content.append("name: ").append(feedback.getName());
                    content.append("\n");
                }
                if (StringUtils.isNotBlank(feedback.getEmail())) {
                    content.append("email: ").append(feedback.getEmail());
                    content.append("\n");
                }
                content.append("ip: ").append(feedback.getIp());
                content.append("\n");
                content.append(feedback.getText());
                content.append("\n\n--\n\n");
            }

            final String to = env.getProperty("mail.feedback.to");
            final String subject = env.getProperty("mail.feedback.subject");

            if (StringUtils.isBlank(to) || StringUtils.isBlank(subject)) {

                log.warn(String.format("Cannot send feedback via mail. Invalid settings to: '%s', subject: '%s'", to, subject));

            } else {

                mailService.sendEmail(to, subject, content.toString(), false, false);
                feedbackRepository.delete(list);
            }
        }
    }

    // --

    private void assertSupermodPrivilege() throws KalipoException {
        boolean isSuperMod = userService.isSuperMod(SecurityUtils.getCurrentLogin());
        if (!isSuperMod) {
            throw new KalipoException(ErrorCode.PERMISSION_DENIED);
        }
    }
}
