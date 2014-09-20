package org.kalipo.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.security.Privileges;
import org.kalipo.service.VoteService;
import org.kalipo.web.rest.dto.VoteDTO;
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

    private static final String DEFAULT_ID = "1";
    private static final Long DEFAULT_COMMENT_ID = 1l;

    private static final Boolean DEFAULT_SAMPLE_ISLIKE_ATTR = false;
    private static final Boolean UPD_SAMPLE_ISLIKE_ATTR = true;

    @Inject
    private VoteService voteService;

    private MockMvc restVoteMockMvc;

    private VoteDTO vote;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        VoteResource voteResource = new VoteResource();
        ReflectionTestUtils.setField(voteResource, "voteService", voteService);

        this.restVoteMockMvc = MockMvcBuilders.standaloneSetup(voteResource).build();

        TestUtil.mockSecurityContext("admin", Arrays.asList(Privileges.CREATE_VOTE));

        vote = new VoteDTO();
        vote.setId(DEFAULT_ID);
        vote.setCommentId(DEFAULT_COMMENT_ID);
        vote.setIsLike(DEFAULT_SAMPLE_ISLIKE_ATTR);
    }

    @Test
    public void testCRUDVote() throws Exception {

        // Create Vote
        restVoteMockMvc.perform(post("/app/rest/votes")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(vote)))
                .andExpect(status().isCreated());

        // Try create a empty Comment
        restVoteMockMvc.perform(post("/app/rest/votes")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new VoteDTO())))
                .andExpect(status().isBadRequest());

        // Read Vote
        restVoteMockMvc.perform(get("/app/rest/votes/{id}", DEFAULT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(DEFAULT_ID))
                .andExpect(jsonPath("$.commentId").value(DEFAULT_COMMENT_ID.intValue()))
                .andExpect(jsonPath("$.isLike").value(DEFAULT_SAMPLE_ISLIKE_ATTR))
        ;

        // Update Vote
        vote.setIsLike(UPD_SAMPLE_ISLIKE_ATTR);

        restVoteMockMvc.perform(put("/app/rest/votes/{id}", DEFAULT_ID)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(vote)))
                .andExpect(status().isOk());

        // Read updated Vote
        restVoteMockMvc.perform(get("/app/rest/votes/{id}", DEFAULT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(DEFAULT_ID))
                .andExpect(jsonPath("$.isLike").value(UPD_SAMPLE_ISLIKE_ATTR))
        ;

        // Delete Vote
        restVoteMockMvc.perform(delete("/app/rest/votes/{id}", DEFAULT_ID)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Read nonexisting Vote
        restVoteMockMvc.perform(get("/app/rest/votes/{id}", DEFAULT_ID)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

    }
}
