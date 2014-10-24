package org.kalipo.web.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.domain.Tag;
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
import java.util.*;

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
    private static final String DEFAULT_SAMPLE_TEXT_ATTR = "sampleTextAttribute";
    private static final Boolean DEFAULT_SAMPLE_READONLY_ATTR = false;

    private static final String UPD_SAMPLE_TITLE_ATTR = "sampleTitleAttributeUpt";
    private static final Boolean UPD_SAMPLE_READONLY_ATTR = true;
    public static final List<String> DEFAULT_PRIVILEGES = Arrays.asList(Privileges.CREATE_THREAD, Privileges.MODERATE_THREAD, Privileges.CREATE_COMMENT);

    @Rule
    public ExpectedException exception = ExpectedException.none();

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

        TestUtil.mockSecurityContext("admin", DEFAULT_PRIVILEGES);

    }

    @Test
    public void testCRUDThread() throws Exception {

        thread = newThread();

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

        // Try create without Thread
        restThreadMockMvc.perform(post("/app/rest/threads")
                .contentType(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest());

        // Try create a empty Thread
        restThreadMockMvc.perform(post("/app/rest/threads")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new Thread())))
                .andExpect(status().isBadRequest());

        // Read Thread
        restThreadMockMvc.perform(get("/app/rest/threads/{id}", threadId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(threadId))
                .andExpect(jsonPath("$.title").value(DEFAULT_SAMPLE_TITLE_ATTR))
                .andExpect(jsonPath("$.readOnly").value(DEFAULT_SAMPLE_READONLY_ATTR));

        // Update Thread
        thread.setTitle(UPD_SAMPLE_TITLE_ATTR);
        thread.setReadOnly(UPD_SAMPLE_READONLY_ATTR);

        restThreadMockMvc.perform(put("/app/rest/threads/{id}", threadId)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(thread)))
                .andExpect(status().isOk());

        // Read updated Thread
        restThreadMockMvc.perform(get("/app/rest/threads/{id}", threadId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(threadId))
                .andExpect(jsonPath("$.title").value(UPD_SAMPLE_TITLE_ATTR))
                .andExpect(jsonPath("$.readOnly").value(UPD_SAMPLE_READONLY_ATTR));

        // Delete Thread
        restThreadMockMvc.perform(delete("/app/rest/threads/{id}", threadId)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Read nonexisting Thread
        restThreadMockMvc.perform(get("/app/rest/threads/{id}", threadId)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

    }

    @Test
    public void test_UrlHooksWithoutPermission() throws Exception {

        thread = newThread();
        thread.setUriHook("http://example.com");

        restThreadMockMvc.perform(post("/app/rest/threads")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(thread)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test_UrlHooksWithPermission() throws Exception {

        thread = newThread();
        thread.setUriHook("http://example.com");

        List<String> privileges = new LinkedList<>();
        privileges.add(Privileges.HOOK_THREAD_TO_URL);
        privileges.addAll(DEFAULT_PRIVILEGES);

        TestUtil.mockSecurityContext("admin", privileges);

        restThreadMockMvc.perform(post("/app/rest/threads")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(thread)))
                .andExpect(status().isCreated());
    }

    @Test
    public void test_addAndRemoveTags() throws Exception {

        thread = newThread();

        Set<Tag> tags = new HashSet<Tag>();
        tags.add(new Tag("something"));

        thread.setTags(tags);

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

        // Read updated Thread
        restThreadMockMvc.perform(get("/app/rest/threads/{id}", threadId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tags").value(new BaseMatcher() {

                    @Override
                    public boolean matches(Object item) {
                        return equalsTags(tags, (net.minidev.json.JSONArray) item);
                    }

                    @Override
                    public void describeTo(Description description) {

                    }
                }));

        tags.clear();
        tags.add(new Tag("something2"));

        // update tags only
        restThreadMockMvc.perform(put(String.format("/app/rest/threads/%s/tags", threadId))
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(tags)))
                .andExpect(status().isOk());


        // Read updated Thread
        restThreadMockMvc.perform(get("/app/rest/threads/{id}", threadId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tags").value(new BaseMatcher() {

                    @Override
                    public boolean matches(Object item) {
                        return equalsTags(tags, (net.minidev.json.JSONArray) item);
                    }

                    @Override
                    public void describeTo(Description description) {

                    }
                }));

    }

    private boolean equalsTags(Set<Tag> orgTags, net.minidev.json.JSONArray tagsArr) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Set<Tag> updatedTags = new HashSet<Tag>();
        for (Tag tag : mapper.convertValue(tagsArr, Tag[].class)) {
            updatedTags.add(tag);
        }

        if (orgTags.size() == updatedTags.size()) {
            for (Tag tag : orgTags) {
                if (!updatedTags.contains(tag)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Thread newThread() {
        Thread thread = new Thread();
        thread.setTitle(DEFAULT_SAMPLE_TITLE_ATTR);
        thread.setText(DEFAULT_SAMPLE_TEXT_ATTR);
        thread.setReadOnly(DEFAULT_SAMPLE_READONLY_ATTR);
        return thread;
    }
}
