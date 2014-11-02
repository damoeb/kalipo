package org.kalipo.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Thread;
import org.kalipo.security.Privileges;
import org.kalipo.service.CommentService;
import org.kalipo.service.ThreadService;
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

    private static final String UPD_SAMPLE_TEXT_ATTR = "updSampleText";
    private static final String DEFAULT_TEXT = "sampleText";

    private static final String DEFAULT_TITLE = "sampleTitle";

    private String commentId;

    @Inject
    private CommentService commentService;

    @Inject
    private ThreadService threadService;

    private MockMvc restCommentMockMvc;

    private Comment comment;

    @Before
    public void setup() throws KalipoException {
        MockitoAnnotations.initMocks(this);
        CommentResource commentResource = new CommentResource();
        ReflectionTestUtils.setField(commentResource, "commentService", commentService);

        this.restCommentMockMvc = MockMvcBuilders.standaloneSetup(commentResource).build();

        TestUtil.mockSecurityContext("admin", Arrays.asList(Privileges.CREATE_COMMENT, Privileges.CREATE_THREAD, Privileges.MODERATE_THREAD));

        Thread thread = ThreadResourceTest.newThread();
        threadService.create(thread);

        comment = newComment();
        comment.setThreadId(thread.getId());
    }

    @Test
    public void testCRUDComment() throws Exception {

        // Create Comment
        restCommentMockMvc.perform(post("/app/rest/comments")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(comment)))
                .andExpect(status().isCreated())
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        commentId = TestUtil.toJson(result).getString("id");
                    }
                });

        // Try create a empty Comment
        restCommentMockMvc.perform(post("/app/rest/comments")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new Comment())))
                .andExpect(status().isBadRequest());

        // Read Comment
        restCommentMockMvc.perform(get("/app/rest/comments/{id}", commentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value(DEFAULT_TEXT))
        ;

//        // Update Comment
        comment.setText(UPD_SAMPLE_TEXT_ATTR);
//
        restCommentMockMvc.perform(put("/app/rest/comments/{id}", commentId)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(comment)))
                .andExpect(status().isOk());

        // Read updated Comment
        restCommentMockMvc.perform(get("/app/rest/comments/{id}", commentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value(UPD_SAMPLE_TEXT_ATTR))
        ;

        // Delete Comment
        restCommentMockMvc.perform(delete("/app/rest/comments/{id}", commentId)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Read non-existing Comment
        restCommentMockMvc.perform(get("/app/rest/comments/{id}", commentId)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

    }

    @Test
    public void test_failCommentOnReadOnlyThread() throws Exception {

        Thread thread = ThreadResourceTest.newThread();
        thread = threadService.create(thread);

        thread.setReadOnly(true);
        threadService.update(thread);

        comment = newComment();
        comment.setThreadId(thread.getId());

        // Create Comment
        restCommentMockMvc.perform(post("/app/rest/comments")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(comment)))
                .andExpect(status().isBadRequest());

    }

    public static Comment newComment() {
        Comment comment = new Comment();
        comment.setText(DEFAULT_TEXT);
        return comment;
    }
}
