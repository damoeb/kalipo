package org.kalipo.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Thread;
import org.kalipo.domain.Vote;
import org.kalipo.security.Privileges;
import org.kalipo.service.CommentService;
import org.kalipo.service.ThreadService;
import org.kalipo.service.VoteService;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.inject.Inject;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the VoteResource REST controller.
 *
 * @see VoteResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
@ActiveProfiles("dev")
public class VoteResourceTest {

    private String voteId;
    private String commentId;

    private static final Boolean DEFAULT_SAMPLE_ISLIKE_ATTR = false;

    @Inject
    private VoteService voteService;

    @Inject
    private CommentService commentService;

    @Inject
    private ThreadService threadService;

    private MockMvc restVoteMockMvc;

    private Vote vote;

    @Before
    public void setup() throws KalipoRequestException {
        MockitoAnnotations.initMocks(this);
        VoteResource voteResource = new VoteResource();
        ReflectionTestUtils.setField(voteResource, "voteService", voteService);

        this.restVoteMockMvc = MockMvcBuilders.standaloneSetup(voteResource).build();

        TestUtil.mockSecurityContext("admin", Arrays.asList(Privileges.CREATE_VOTE, Privileges.CREATE_THREAD, Privileges.CREATE_COMMENT));

        Thread thread = ThreadResourceTest.newThread();
        threadService.create(thread);
        Comment comment = CommentResourceTest.newComment();
        comment.setThreadId(thread.getId());
        commentService.create(comment);

        commentId = comment.getId();

        vote = new Vote();
        vote.setCommentId(commentId);
        vote.setIsLike(DEFAULT_SAMPLE_ISLIKE_ATTR);
    }

    @Test
    public void testCRUDVote() throws Exception {

        // Create Vote
        restVoteMockMvc.perform(post("/app/rest/votes")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(vote)))
                .andExpect(status().isCreated())
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        voteId = TestUtil.toJson(result).getString("id");
                    }
                });

        // Try create a empty Comment
        restVoteMockMvc.perform(post("/app/rest/votes")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new Vote())))
                .andExpect(status().isBadRequest());

        // Read Vote
        restVoteMockMvc.perform(get("/app/rest/votes/{id}", voteId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(voteId))
                .andExpect(jsonPath("$.commentId").value(commentId))
                .andExpect(jsonPath("$.isLike").value(DEFAULT_SAMPLE_ISLIKE_ATTR))
        ;

        // Delete Vote
        restVoteMockMvc.perform(delete("/app/rest/votes/{id}", voteId)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Read nonexisting Vote
        restVoteMockMvc.perform(get("/app/rest/votes/{id}", voteId)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

    }
}
