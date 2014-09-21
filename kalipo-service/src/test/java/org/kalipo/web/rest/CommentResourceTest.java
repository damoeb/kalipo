package org.kalipo.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.domain.Comment;
import org.kalipo.security.Privileges;
import org.kalipo.service.CommentService;
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
 * Test class for the CommentResource REST controller.
 *
 * @see CommentResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
@ActiveProfiles("dev")
public class CommentResourceTest {

    private static final String DEFAULT_ID = "1";

    private static final String UPD_SAMPLE_TITLE_ATTR = "updSampleTitle";
    private static final String UPD_SAMPLE_TEXT_ATTR = "updSampleText";
//    private static final LocalDate UPD_SAMPLE_TEXT_ATTR = new LocalDate();

    private static final String DEFAULT_THREAD_ID = "1";
    private static final String DEFAULT_TEXT = "sampleText";
    private static final String DEFAULT_TITLE = "sampleTitle";

    @Inject
    private CommentService commentService;

    private MockMvc restCommentMockMvc;

    private Comment comment;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CommentResource commentResource = new CommentResource();
        ReflectionTestUtils.setField(commentResource, "commentService", commentService);

        this.restCommentMockMvc = MockMvcBuilders.standaloneSetup(commentResource).build();

        TestUtil.mockSecurityContext("admin", Arrays.asList(Privileges.CREATE_COMMENT));

        comment = new Comment();
        comment.setId(DEFAULT_ID);
        comment.setThreadId(DEFAULT_THREAD_ID);
        comment.setText(DEFAULT_TEXT);
        comment.setTitle(DEFAULT_TITLE);
    }

    @Test
    public void testCRUDComment() throws Exception {

        // Create Comment
        restCommentMockMvc.perform(post("/app/rest/comments")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(comment)))
                .andExpect(status().isCreated());

        // Try create a empty Comment
        restCommentMockMvc.perform(post("/app/rest/comments")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new Comment())))
                .andExpect(status().isBadRequest());

        // Read Comment
        restCommentMockMvc.perform(get("/app/rest/comments/{id}", DEFAULT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(DEFAULT_ID))
                .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
                .andExpect(jsonPath("$.text").value(DEFAULT_TEXT))
//                .andExpect(jsonPath("$.threadId").value(DEFAULT_THREAD_ID))
        ;
//
//        // Update Comment
        comment.setTitle(UPD_SAMPLE_TITLE_ATTR);
        comment.setText(UPD_SAMPLE_TEXT_ATTR);
//
        restCommentMockMvc.perform(put("/app/rest/comments/{id}", DEFAULT_ID)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(comment)))
                .andExpect(status().isOk());

        // Read updated Comment
        restCommentMockMvc.perform(get("/app/rest/comments/{id}", DEFAULT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(DEFAULT_ID))
                .andExpect(jsonPath("$.title").value(UPD_SAMPLE_TITLE_ATTR))
                .andExpect(jsonPath("$.text").value(UPD_SAMPLE_TEXT_ATTR))
        ;

        // Delete Comment
        restCommentMockMvc.perform(delete("/app/rest/comments/{id}", DEFAULT_ID)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Read nonexisting Comment
        restCommentMockMvc.perform(get("/app/rest/comments/{id}", DEFAULT_ID)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

    }
}
