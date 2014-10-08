package org.kalipo.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.domain.Tag;
import org.kalipo.security.Privileges;
import org.kalipo.service.TagService;
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
 * Test class for the TagResource REST controller.
 *
 * @see TagResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
@ActiveProfiles("dev")
public class TagResourceTest {

    private static final String DEFAULT_SAMPLE_NAME_ATTR = "sampleTitleAttribute";

    private static final String UPD_SAMPLE_NAME_ATTR = "sampleTitleAttributeUpt";

    @Inject
    private TagService tagService;

    private MockMvc restTagMockMvc;

    private Tag tag;

    private String tagId;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        TagResource tagResource = new TagResource();
        ReflectionTestUtils.setField(tagResource, "tagService", tagService);

        this.restTagMockMvc = MockMvcBuilders.standaloneSetup(tagResource).build();

        TestUtil.mockSecurityContext("admin", Arrays.asList(Privileges.CREATE_TAG));

        tag = new Tag();
        tag.setName(DEFAULT_SAMPLE_NAME_ATTR);
    }

    @Test
    public void testCRUDTag() throws Exception {

        // Create Tag
        restTagMockMvc.perform(post("/app/rest/tags")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(tag)))
                .andExpect(status().isCreated())
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        tagId = TestUtil.toJson(result).getString("id");
                    }
                });


        // Try create a empty Comment
        restTagMockMvc.perform(post("/app/rest/tags")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new Tag())))
                .andExpect(status().isBadRequest());

        // Read Tag
        restTagMockMvc.perform(get("/app/rest/tags/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(tagId))
                .andExpect(jsonPath("$.name").value(DEFAULT_SAMPLE_NAME_ATTR));

        // Delete Tag
        restTagMockMvc.perform(delete("/app/rest/tags/{id}", tagId)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Read nonexisting Tag
        restTagMockMvc.perform(get("/app/rest/tags/{id}", tagId)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

    }
}
