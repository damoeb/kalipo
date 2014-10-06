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
import org.kalipo.repository.ReputationDefinitionRepository;
import org.kalipo.repository.UserRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.web.rest.CommentResourceTest;
import org.kalipo.web.rest.KalipoRequestException;
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
    private UserRepository userRepository;

    @Inject
    private ReputationDefinitionRepository reputationDefinitionRepository;

    @Inject
    private UserService userService;

    @Inject
    private ReputationService reputationService;

    @Inject
    private CommentService commentService;

    @Inject
    private ThreadService threadService;

    private Comment comment;

    @Before
    public void test_before() throws KalipoRequestException {
        TestUtil.mockSecurityContext("admin", Arrays.asList(Privileges.CREATE_COMMENT, Privileges.CREATE_THREAD));

        Thread thread = ThreadResourceTest.newThread();
        threadService.create(thread);

        comment = CommentResourceTest.newComment();
        comment.setThreadId(thread.getId());
        commentService.create(comment);
    }

    @Test
    public void test_initUser() {

        User userBefore = userRepository.findOne(SecurityUtils.getCurrentLogin());

//        exception.expect(KalipoRequestException.class);
        reputationService.initUser(userBefore);

        // get user reputation
        User userAfter = userRepository.findOne(SecurityUtils.getCurrentLogin());

        assertThat(userBefore.getReputation()).isEqualTo(userAfter.getReputation() - reputationDefinitionRepository.findByType(ReputationDefinition.Type.WELCOME).getReputation());

        // delete revision


        // check reputation again

    }

    @Test
    public void test_likeComment() throws KalipoRequestException {

        User userBefore = userRepository.findOne(SecurityUtils.getCurrentLogin());

        Vote newVote = new Vote();
        newVote.setIsLike(true);
        newVote.setCommentId(comment.getId());

        reputationService.likeOrDislikeComment(newVote);

        User userAfter = userRepository.findOne(SecurityUtils.getCurrentLogin());

        int repLiked = reputationDefinitionRepository.findByType(ReputationDefinition.Type.LIKED).getReputation();
        int repLike = reputationDefinitionRepository.findByType(ReputationDefinition.Type.LIKE).getReputation();

        assertThat(userBefore.getReputation()).isEqualTo(userAfter.getReputation() - repLiked - repLike);

    }

    @Test
    public void test_dislikeComment() throws KalipoRequestException {

        User userBefore = userRepository.findOne(SecurityUtils.getCurrentLogin());

        Vote newVote = new Vote();
        newVote.setIsLike(false);
        newVote.setCommentId(comment.getId());

        reputationService.likeOrDislikeComment(newVote);

        User userAfter = userRepository.findOne(SecurityUtils.getCurrentLogin());

        int repDisliked = reputationDefinitionRepository.findByType(ReputationDefinition.Type.DISLIKED).getReputation();
        int repDislike = reputationDefinitionRepository.findByType(ReputationDefinition.Type.DISLIKE).getReputation();

        assertThat(userBefore.getReputation()).isEqualTo(userAfter.getReputation() - repDisliked - repDislike);

    }

    @Test
    public void test_approveReport() throws KalipoRequestException {

        User userBefore = userRepository.findOne(SecurityUtils.getCurrentLogin());

        Report newReport = new Report();
        newReport.setCommentId(comment.getId());
        newReport.setStatus(Report.Status.APPROVED);
        newReport.setAuthorId(SecurityUtils.getCurrentLogin());

        reputationService.approveOrRejectReport(newReport);

        User userAfter = userRepository.findOne(SecurityUtils.getCurrentLogin());

        int repReport = reputationDefinitionRepository.findByType(ReputationDefinition.Type.REPORT).getReputation();
        int repReported = reputationDefinitionRepository.findByType(ReputationDefinition.Type.REPORTED).getReputation();

        assertThat(userBefore.getReputation()).isEqualTo(userAfter.getReputation() - repReport - repReported);

    }

    @Test
    public void test_rejectReport() throws KalipoRequestException {

        User userBefore = userRepository.findOne(SecurityUtils.getCurrentLogin());

        Report newReport = new Report();
        newReport.setAbused(true);
        newReport.setCommentId(comment.getId());
        newReport.setStatus(Report.Status.REJECTED);
        newReport.setAuthorId(SecurityUtils.getCurrentLogin());

        reputationService.approveOrRejectReport(newReport);

        User userAfter = userRepository.findOne(SecurityUtils.getCurrentLogin());

        int repAbusedReport = reputationDefinitionRepository.findByType(ReputationDefinition.Type.ABUSED_REPORT).getReputation();

        assertThat(userBefore.getReputation()).isEqualTo(userAfter.getReputation() - repAbusedReport);

    }
//
//    @Test
//    public void test_punishDeletingComment() throws KalipoRequestException {
////        assertThat(persistentTokenRepository.findByUser(admin)).hasSize(existingCount + 1);
//        Comment newComment = new Comment();
//        reputationService.punishDeletingComment(newComment);
//    }

}
