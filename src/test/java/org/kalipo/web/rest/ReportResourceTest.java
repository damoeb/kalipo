package org.kalipo.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.repository.ReportRepository;
import org.kalipo.web.rest.dto.ReportDTO;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the ReportResource REST controller.
 *
 * @see ReportResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
@ActiveProfiles("dev")
public class ReportResourceTest {

    private static final String DEFAULT_ID = "1";

    private static final String DEFAULT_SAMPLE_REASON_ATTR = "sampleReasonAttribute";
    private static final String UPD_SAMPLE_REASON_ATTR = "sampleReasonAttributeUpt";

    private static final Long DEFAULT_SAMPLE_COMMENT_ID_ATTR = 1l;
    private static final Long UPD_SAMPLE_COMMENT_ID_ATTR = 2l;

    private static final String UPD_SAMPLE_TITLE_ATTR = "sampleTitleAttributeUpt";

    @Inject
    private ReportRepository reportRepository;

    private MockMvc restReportMockMvc;

    private ReportDTO report;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReportResource reportResource = new ReportResource();
        ReflectionTestUtils.setField(reportResource, "reportRepository", reportRepository);

        this.restReportMockMvc = MockMvcBuilders.standaloneSetup(reportResource).build();

        report = new ReportDTO();
        report.setId(DEFAULT_ID);
        report.setReason(DEFAULT_SAMPLE_REASON_ATTR);
        report.setCommentId(DEFAULT_SAMPLE_COMMENT_ID_ATTR);
    }

    @Test
    public void testCRUDReport() throws Exception {

        // Create Report
        restReportMockMvc.perform(post("/app/rest/reports")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(report)))
                .andExpect(status().isCreated());

        // Try create a empty Comment
        restReportMockMvc.perform(post("/app/rest/reports")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new ReportDTO())))
                .andExpect(status().isBadRequest());

        // Read Report
        restReportMockMvc.perform(get("/app/rest/reports/{id}", DEFAULT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(DEFAULT_ID))
                .andExpect(jsonPath("$.reason").value(DEFAULT_SAMPLE_REASON_ATTR));

        // Update Report
        report.setReason(UPD_SAMPLE_REASON_ATTR);
        report.setCommentId(UPD_SAMPLE_COMMENT_ID_ATTR);

        restReportMockMvc.perform(put("/app/rest/reports/{id}", DEFAULT_ID)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(report)))
                .andExpect(status().isOk());

        // Read updated Report
        restReportMockMvc.perform(get("/app/rest/reports/{id}", DEFAULT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(DEFAULT_ID))
                .andExpect(jsonPath("$.reason").value(UPD_SAMPLE_REASON_ATTR));

        // Delete Report
        restReportMockMvc.perform(delete("/app/rest/reports/{id}", DEFAULT_ID)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Read nonexisting Report
        restReportMockMvc.perform(get("/app/rest/reports/{id}", DEFAULT_ID)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

    }
}
