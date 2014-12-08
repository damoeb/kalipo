package org.kalipo.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.config.MongoConfiguration;
import org.kalipo.domain.*;
import org.kalipo.domain.Thread;
import org.kalipo.repository.ReportRepository;
import org.kalipo.repository.ReputationRepository;
import org.kalipo.repository.UserRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.web.rest.CommentResourceTest;
import org.kalipo.web.rest.KalipoException;
import org.kalipo.web.rest.TestUtil;
import org.kalipo.web.rest.ThreadResourceTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the ReputationService.
 *
 * @see org.kalipo.service.UserService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@ActiveProfiles("dev")
@Import(MongoConfiguration.class)
public class ReputationServiceTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Inject
    private CommentService commentService;

    @Inject
    private ThreadService threadService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ReputationRepository reputationRepository;

    @Inject
    private ReputationService reputationService;

    @Inject
    private ReportRepository reportRepository;

    private Comment comment;

    @Before
    public void test_before() throws KalipoException {
        TestUtil.mockSecurityContext("admin", Arrays.asList(Privileges.CREATE_COMMENT, Privileges.REVIEW_COMMENT, Privileges.CREATE_THREAD));

        Thread thread = ThreadResourceTest.newThread();
        threadService.create(thread);

        comment = CommentResourceTest.newComment();
        comment.setThreadId(thread.getId());
        commentService.create(comment);
    }

    @Test
    public void test_initUser() {

        User userBefore = userRepository.findOne(SecurityUtils.getCurrentLogin());

//        exception.expect(KalipoException.class);
        reputationService.onUserCreation(userBefore);

        // get user reputation
        User userAfter = userRepository.findOne(SecurityUtils.getCurrentLogin());

        assertThat(userBefore.getReputation()).isEqualTo(userAfter.getReputation() - reputationRepository.findByType(Reputation.Type.WELCOME).getReputation());

        // delete revision


        // check reputation again

    }

    @Test
    public void test_likeComment() throws KalipoException {

        User userBefore = userRepository.findOne(SecurityUtils.getCurrentLogin());

        Vote newVote = new Vote();
        newVote.setLike(true);
        newVote.setCommentId(comment.getId());

        reputationService.onCommentVoting(newVote);

        User userAfter = userRepository.findOne(SecurityUtils.getCurrentLogin());

        int repLiked = reputationRepository.findByType(Reputation.Type.LIKED).getReputation();
        int repLike = reputationRepository.findByType(Reputation.Type.LIKE).getReputation();

        assertThat(userBefore.getReputation()).isEqualTo(userAfter.getReputation() - repLiked - repLike);

    }

    @Test
    public void test_dislikeComment() throws KalipoException {

        User userBefore = userRepository.findOne(SecurityUtils.getCurrentLogin());

        Vote newVote = new Vote();
        newVote.setLike(false);
        newVote.setCommentId(comment.getId());

        reputationService.onCommentVoting(newVote);

        User userAfter = userRepository.findOne(SecurityUtils.getCurrentLogin());

        int repDisliked = reputationRepository.findByType(Reputation.Type.DISLIKED).getReputation();
        int repDislike = reputationRepository.findByType(Reputation.Type.DISLIKE).getReputation();

        assertThat(userBefore.getReputation()).isEqualTo(userAfter.getReputation() - repDisliked - repDislike);

    }

    @Test
    public void test_approveReport() throws KalipoException {

        User userBefore = userRepository.findOne(SecurityUtils.getCurrentLogin());

        Report newReport = new Report();
        newReport.setCommentId(comment.getId());
        newReport.setThreadId(comment.getThreadId());
        newReport.setStatus(Report.Status.APPROVED);
        newReport.setAuthorId(SecurityUtils.getCurrentLogin());
        newReport.setReason("something");

        newReport = reportRepository.save(newReport);

        reputationService.onReportApprovalOrRejection(newReport);

        User userAfter = userRepository.findOne(SecurityUtils.getCurrentLogin());

        int repReport = reputationRepository.findByType(Reputation.Type.REPORT).getReputation();
        int repReported = reputationRepository.findByType(Reputation.Type.REPORTED).getReputation();

        assertThat(userBefore.getReputation()).isEqualTo(userAfter.getReputation() - repReport - repReported);

    }

    @Test
    public void test_rejectReport() throws KalipoException {

        User userBefore = userRepository.findOne(SecurityUtils.getCurrentLogin());

        Report newReport = new Report();
        newReport.setAbused(true);
        newReport.setCommentId(comment.getId());
        newReport.setThreadId(comment.getThreadId());
        newReport.setStatus(Report.Status.REJECTED);
        newReport.setAuthorId(SecurityUtils.getCurrentLogin());
        newReport.setReason("something");

        newReport = reportRepository.save(newReport);

        reputationService.onReportApprovalOrRejection(newReport);

        User userAfter = userRepository.findOne(SecurityUtils.getCurrentLogin());

        int repAbusedReport = reputationRepository.findByType(Reputation.Type.ABUSED_REPORT).getReputation();

        assertThat(userBefore.getReputation()).isEqualTo(userAfter.getReputation() - repAbusedReport);

    }

    @Test
    public void test_punishDeletingComment() throws KalipoException {

        User userBefore = userRepository.findOne(SecurityUtils.getCurrentLogin());

        reputationService.onCommentDeletion(comment);

        User userAfter = userRepository.findOne(SecurityUtils.getCurrentLogin());

        int repRmComment = reputationRepository.findByType(Reputation.Type.RM_COMMENT).getReputation();

        assertThat(userBefore.getReputation()).isEqualTo(userAfter.getReputation() - repRmComment);
    }

}
