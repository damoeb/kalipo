package org.kalipo.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.config.MongoConfiguration;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Report;
import org.kalipo.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.Assert;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Test class for the thymeleaf templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("dev")
@Import(MongoConfiguration.class)
public class NotificationServiceTest {

    private final Logger log = LoggerFactory.getLogger(NotificationServiceTest.class);

    @Inject
    private NotificationService notificationService;

    @Test
    public void testEmailTemplates() {

        User user = new User();
        user.setDisplayName("moderator");

        Comment comment = new Comment();
        comment.setId("idOFComment");
        comment.setDisplayName("authorOfComment");
        comment.setBodyHtml("content of comment");

        String pendingCommentEmailMarkup = notificationService.createPendingCommentEmailFromTemplate(user, comment, Locale.ENGLISH);
        Assert.notNull(pendingCommentEmailMarkup, "rendering pending-comment-template returns null");

        Report report = new Report();
        report.setId("idOfReport");
        report.setReason(Report.Reason.Hate_Speech);

        String pendingReportEmailMarkup = notificationService.createPendingReportEmailFromTemplate(user, report, comment, Locale.ENGLISH);
        Assert.notNull(pendingReportEmailMarkup, "rendering pending-report-template returns null");
    }
}
