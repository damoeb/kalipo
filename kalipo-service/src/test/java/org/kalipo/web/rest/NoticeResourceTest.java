package org.kalipo.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.domain.Notice;
import org.kalipo.security.Privileges;
import org.kalipo.service.NoticeService;
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
 * Test class for the NoticeResource REST controller.
 *
 * @see org.kalipo.web.rest.NoticeResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
@ActiveProfiles("dev")
public class NoticeResourceTest {

    private static final String DEFAULT_SAMPLE_NAME_ATTR = "sampleTitleAttribute";

    private static final String UPD_SAMPLE_NAME_ATTR = "sampleTitleAttributeUpt";

    @Inject
    private NoticeService noticeService;

    private MockMvc restNoticeMockMvc;

    private Notice notice;

    private String noticeId;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        NoticeResource noticeResource = new NoticeResource();
        ReflectionTestUtils.setField(noticeResource, "noticeService", noticeService);

        this.restNoticeMockMvc = MockMvcBuilders.standaloneSetup(noticeResource).build();

        TestUtil.mockSecurityContext("admin", Arrays.asList(Privileges.CREATE_TAG));

        notice = new Notice();
//        notice.setName(DEFAULT_SAMPLE_NAME_ATTR);
    }

    @Test
    public void testCRUDNotice() throws Exception {

        // Create Notice
        restNoticeMockMvc.perform(post("/app/rest/notices")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(notice)))
                .andExpect(status().isCreated())
                .andDo(new ResultHandler() {
                    @Override
                    public void handle(MvcResult result) throws Exception {
                        noticeId = TestUtil.toJson(result).getString("id");
                    }
                });


        // Try create a empty Comment
        restNoticeMockMvc.perform(post("/app/rest/notices")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new Notice())))
                .andExpect(status().isBadRequest());

        // Read Notice
        restNoticeMockMvc.perform(get("/app/rest/notices/{id}", noticeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(noticeId))
                .andExpect(jsonPath("$.name").value(DEFAULT_SAMPLE_NAME_ATTR));

        // Delete Notice
        restNoticeMockMvc.perform(delete("/app/rest/notices/{id}", noticeId)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Read nonexisting Notice
        restNoticeMockMvc.perform(get("/app/rest/notices/{id}", noticeId)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

    }
}
