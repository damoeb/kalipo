package org.kalipo.web.rest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.domain.Comment;
import org.kalipo.domain.Notice;
import org.kalipo.security.Privileges;
import org.kalipo.service.CommentService;
import org.kalipo.service.NoticeService;
import org.kalipo.service.ThreadService;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
@EnableAsync
public class NoticeResourceTest {

    private static final String DEFAULT_SAMPLE_NAME_ATTR = "sampleTitleAttribute";

    private static final String UPD_SAMPLE_NAME_ATTR = "sampleTitleAttributeUpt";

    @Inject
    private NoticeService noticeService;

    @Inject
    private ThreadService threadService;

    @Inject
    private CommentService commentService;

    private MockMvc restNoticeMockMvc;

    private Notice notice;

    private String noticeId;

    @Before
    public void setup() throws KalipoException {
        MockitoAnnotations.initMocks(this);
        NoticeResource noticeResource = new NoticeResource();
        ReflectionTestUtils.setField(noticeResource, "noticeService", noticeService);

        this.restNoticeMockMvc = MockMvcBuilders.standaloneSetup(noticeResource).build();

        TestUtil.mockSecurityContext("admin", Arrays.asList(Privileges.CREATE_TAG));

        notice = new Notice();
//        notice.setName(DEFAULT_SAMPLE_NAME_ATTR);


        TestUtil.mockSecurityContext("admin", Arrays.asList(Privileges.CREATE_COMMENT, Privileges.REVIEW_COMMENT, Privileges.CREATE_THREAD));

        /*
         create comment,
         reply on that
        */


        org.kalipo.domain.Thread thread = ThreadResourceTest.newThread();
        thread = threadService.create(thread);

        Comment comment = CommentResourceTest.newComment();
        comment.setThreadId(thread.getId());
        comment = commentService.create(comment);

        Comment reply = CommentResourceTest.newComment();
        reply.setThreadId(thread.getId());
        reply.setParentId(comment.getId());
        reply = commentService.create(reply);
    }

    @Test
    public void testCRUDNotice() throws Exception {

        /*
         read notices
         update read field
        */

        Thread.sleep(1000);

        // Read My Notices
        restNoticeMockMvc.perform(get("/app/rest/notices/admin/{page}", 0))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)
                ).andDo(new ResultHandler() {
            @Override
            public void handle(MvcResult result) throws Exception {
                noticeId = ((JSONObject) new JSONArray(result.getResponse().getContentAsString()).get(0)).getString("id");
            }
        });

        // Set one notice is read=true
        Notice update = new Notice();
        update.setRead(true);

        restNoticeMockMvc.perform(put("/app/rest/notices/{id}", noticeId)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(update)))
                .andExpect(status().isOk());

        // todo read again
    }
}
