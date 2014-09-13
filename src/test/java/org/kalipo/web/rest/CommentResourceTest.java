package org.kalipo.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.web.rest.dto.CommentDTO;
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

import org.kalipo.Application;
import org.kalipo.domain.Comment;
import org.kalipo.repository.CommentRepository;


/**
 * Test class for the CommentResource REST controller.
 *
 * @see CommentResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class })
@ActiveProfiles("dev")
public class CommentResourceTest {
    
    private static final String DEFAULT_ID = "1";

    private static final LocalDate UPD_SAMPLE_DATE_ATTR = new LocalDate();

    private static final Long DEFAULT_THREAD_ID = 1l;
    private static final String DEFAULT_TEXT = "sampleText";
    private static final String DEFAULT_TITLE = "sampleTitle";

    @Inject
    private CommentRepository commentRepository;

    private MockMvc restCommentMockMvc;

    private CommentDTO comment;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CommentResource commentResource = new CommentResource();
        ReflectionTestUtils.setField(commentResource, "commentRepository", commentRepository);

        this.restCommentMockMvc = MockMvcBuilders.standaloneSetup(commentResource).build();

        comment = new CommentDTO();
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
                .andExpect(status().isOk());

        // Read Comment
//        restCommentMockMvc.perform(get("/app/rest/comments/{id}", DEFAULT_ID))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id").value(DEFAULT_ID))
//                .andExpect(jsonPath("$.sampleDateAttribute").value(DEFAULT_SAMPLE_DATE_ATTR.toString()))
//                .andExpect(jsonPath("$.sampleTextAttribute").value(DEFAULT_SAMPLE_TEXT_ATTR));
//
//        // Update Comment
//        comment.setSampleDateAttribute(UPD_SAMPLE_DATE_ATTR);
//        comment.setSampleTextAttribute(UPD_SAMPLE_TEXT_ATTR);
//
//        restCommentMockMvc.perform(post("/app/rest/comments")
//                .contentType(TestUtil.APPLICATION_JSON_UTF8)
//                .content(TestUtil.convertObjectToJsonBytes(comment)))
//                .andExpect(status().isOk());
//
//        // Read updated Comment
//        restCommentMockMvc.perform(get("/app/rest/comments/{id}", DEFAULT_ID))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id").value(DEFAULT_ID))
//                .andExpect(jsonPath("$.sampleDateAttribute").value(UPD_SAMPLE_DATE_ATTR.toString()))
//                .andExpect(jsonPath("$.sampleTextAttribute").value(UPD_SAMPLE_TEXT_ATTR));
//
//        // Delete Comment
//        restCommentMockMvc.perform(delete("/app/rest/comments/{id}", DEFAULT_ID)
//                .accept(TestUtil.APPLICATION_JSON_UTF8))
//                .andExpect(status().isOk());
//
//        // Read nonexisting Comment
//        restCommentMockMvc.perform(get("/app/rest/comments/{id}", DEFAULT_ID)
//                .accept(TestUtil.APPLICATION_JSON_UTF8))
//                .andExpect(status().isNotFound());

    }
}
