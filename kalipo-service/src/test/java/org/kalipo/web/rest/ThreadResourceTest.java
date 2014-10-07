package org.kalipo.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.domain.Thread;
import org.kalipo.security.Privileges;
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
 * Test class for the ThreadResource REST controller.
 *
 * @see ThreadResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
@ActiveProfiles("dev")
public class ThreadResourceTest {

    private static final String DEFAULT_SAMPLE_TITLE_ATTR = "sampleTitleAttribute";

    private static final String UPD_SAMPLE_TITLE_ATTR = "sampleTitleAttributeUpt";

    @Inject
    private ThreadService threadService;

    private MockMvc restThreadMockMvc;

    private String threadId;

    private Thread thread;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ThreadResource threadResource = new ThreadResource();
        ReflectionTestUtils.setField(threadResource, "threadService", threadService);

        this.restThreadMockMvc = MockMvcBuilders.standaloneSetup(threadResource).build();

        TestUtil.mockSecurityContext("admin", Arrays.asList(Privileges.CREATE_THREAD));

        thread = newThread();
    }

    @Test
    public void testCRUDThread() throws Exception {

        // Create Thread
        restThreadMockMvc.perform(post("/app/rest/threads")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(thread)))
                .andExpect(status().isCreated())
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        threadId = TestUtil.toJson(result).getString("id");
                    }
                });

        // Try create a empty Comment
        restThreadMockMvc.perform(post("/app/rest/threads")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new Thread())))
                .andExpect(status().isBadRequest());

        // Read Thread
        restThreadMockMvc.perform(get("/app/rest/threads/{id}", threadId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(threadId))
                .andExpect(jsonPath("$.title").value(DEFAULT_SAMPLE_TITLE_ATTR));

        // Update Thread
        thread.setTitle(UPD_SAMPLE_TITLE_ATTR);

        restThreadMockMvc.perform(put("/app/rest/threads/{id}", threadId)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(thread)))
                .andExpect(status().isOk());

        // Read updated Thread
        restThreadMockMvc.perform(get("/app/rest/threads/{id}", threadId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(threadId))
                .andExpect(jsonPath("$.title").value(UPD_SAMPLE_TITLE_ATTR));

        // Delete Thread
        restThreadMockMvc.perform(delete("/app/rest/threads/{id}", threadId)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Read nonexisting Thread
        restThreadMockMvc.perform(get("/app/rest/threads/{id}", threadId)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

    }

    public static Thread newThread() {
        Thread thread = new Thread();
        thread.setTitle(DEFAULT_SAMPLE_TITLE_ATTR);
        return thread;
    }
}
