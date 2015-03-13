package org.kalipo.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Report;
import org.kalipo.domain.Thread;
import org.kalipo.security.Privileges;
import org.kalipo.service.CommentService;
import org.kalipo.service.ReportService;
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

    private static final String DEFAULT_SAMPLE_REASON_ATTR = "sampleReasonAttribute";

    @Inject
    private CommentService commentService;

    @Inject
    private ThreadService threadService;

    @Inject
    private ReportService reportService;

    private MockMvc restReportMockMvc;

    private String reportId;

    private Report report;

    @Before
    public void setup() throws KalipoException {
        MockitoAnnotations.initMocks(this);
        ReportResource reportResource = new ReportResource();
        ReflectionTestUtils.setField(reportResource, "reportService", reportService);

        this.restReportMockMvc = MockMvcBuilders.standaloneSetup(reportResource).build();

        TestUtil.mockSecurityContext("admin", Arrays.asList(Privileges.CREATE_REPORT, Privileges.CREATE_COMMENT_SOLO, Privileges.REVIEW_COMMENT, Privileges.CREATE_THREAD));

        Thread thread = ThreadResourceTest.newThread();
        threadService.create(thread);
        Comment comment = CommentResourceTest.newComment();
        comment.setThreadId(thread.getId());
        commentService.create(comment);

        report = new Report();
        report.setReasonId(0);
        report.setCommentId(comment.getId());
    }

    @Test
    public void testCRUDReport() throws Exception {

        // Create Report
        restReportMockMvc.perform(post("/app/rest/reports")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(report)))
                .andExpect(status().isCreated()).andDo(new ResultHandler() {
            @Override
            public void handle(MvcResult result) throws Exception {
                reportId = TestUtil.toJson(result).getString("id");
            }
        });

        // Try create a empty Comment
        restReportMockMvc.perform(post("/app/rest/reports")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new Report())))
                .andExpect(status().isBadRequest());

        // Read Report
        restReportMockMvc.perform(get("/app/rest/reports/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(reportId))
                .andExpect(jsonPath("$.reason").value(DEFAULT_SAMPLE_REASON_ATTR));

        // Delete Report
        restReportMockMvc.perform(delete("/app/rest/reports/{id}", reportId)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Read nonexisting Report
        restReportMockMvc.perform(get("/app/rest/reports/{id}", reportId)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

    }
}
