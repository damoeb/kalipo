package org.kalipo.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.domain.Tag;
import org.kalipo.repository.TagRepository;
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

    private static final String DEFAULT_ID = "1";

    private static final String DEFAULT_SAMPLE_NAME_ATTR = "sampleTitleAttribute";

    private static final String UPD_SAMPLE_NAME_ATTR = "sampleTitleAttributeUpt";

    @Inject
    private TagRepository tagRepository;

    private MockMvc restTagMockMvc;

    private Tag tag;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        TagResource tagResource = new TagResource();
        ReflectionTestUtils.setField(tagResource, "tagRepository", tagRepository);

        this.restTagMockMvc = MockMvcBuilders.standaloneSetup(tagResource).build();

        TestUtil.mockSecurityContext("admin", Arrays.asList());

        tag = new Tag();
        tag.setId(DEFAULT_ID);
        tag.setName(DEFAULT_SAMPLE_NAME_ATTR);
    }

    @Test
    public void testCRUDTag() throws Exception {

        // Create Tag
        restTagMockMvc.perform(post("/app/rest/tags")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(tag)))
                .andExpect(status().isCreated());

        // Try create a empty Comment
        restTagMockMvc.perform(post("/app/rest/tags")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new Tag())))
                .andExpect(status().isBadRequest());

        // Read Tag
        restTagMockMvc.perform(get("/app/rest/tags/{id}", DEFAULT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(DEFAULT_ID))
//                .andExpect(jsonPath("$.sampleDateAttribute").value(DEFAULT_SAMPLE_DATE_ATTR.toString()))
                .andExpect(jsonPath("$.name").value(DEFAULT_SAMPLE_NAME_ATTR));

        // Update Tag
//        tag.setSampleDateAttribute(UPD_SAMPLE_DATE_ATTR);
        tag.setName(UPD_SAMPLE_NAME_ATTR);

        restTagMockMvc.perform(put("/app/rest/tags/{id}", DEFAULT_ID)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(tag)))
                .andExpect(status().isOk());

        // Read updated Tag
        restTagMockMvc.perform(get("/app/rest/tags/{id}", DEFAULT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(DEFAULT_ID))
//                .andExpect(jsonPath("$.sampleDateAttribute").value(UPD_SAMPLE_DATE_ATTR.toString()))
                .andExpect(jsonPath("$.name").value(UPD_SAMPLE_NAME_ATTR));

        // Delete Tag
        restTagMockMvc.perform(delete("/app/rest/tags/{id}", DEFAULT_ID)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Read nonexisting Tag
        restTagMockMvc.perform(get("/app/rest/tags/{id}", DEFAULT_ID)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

    }
}
