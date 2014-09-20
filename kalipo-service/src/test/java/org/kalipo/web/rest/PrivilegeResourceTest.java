package org.kalipo.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.domain.Privilege;
import org.kalipo.security.Privileges;
import org.kalipo.service.PrivilegeService;
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
 * Test class for the PrivilegeResource REST controller.
 *
 * @see PrivilegeResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
@ActiveProfiles("dev")
public class PrivilegeResourceTest {

    private static final String DEFAULT_ID = "1";

    private static final String DEFAULT_SAMPLE_NAME_ATTR = "sampleTitleAttribute";

    private static final String UPD_SAMPLE_NAME_ATTR = "sampleTitleAttributeUpt";

    private static final int DEFAULT_SAMPLE_REPUTATION_ATTR = 5;

    private static final int UPD_SAMPLE_REPUTATION_ATTR = 10;


    @Inject
    private PrivilegeService privilegeService;

    private MockMvc restPrivilegeMockMvc;

    private Privilege privilege;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PrivilegeResource privilegeResource = new PrivilegeResource();
        ReflectionTestUtils.setField(privilegeResource, "privilegeService", privilegeService);

        this.restPrivilegeMockMvc = MockMvcBuilders.standaloneSetup(privilegeResource).build();

        TestUtil.mockSecurityContext("admin", Arrays.asList(Privileges.CREATE_PRIVILEGE));

        privilege = new Privilege();
        privilege.setId(DEFAULT_ID);
        privilege.setName(DEFAULT_SAMPLE_NAME_ATTR);
        privilege.setReputation(DEFAULT_SAMPLE_REPUTATION_ATTR);
    }

    @Test
    public void testCRUDPrivilege() throws Exception {

        // Create Privilege
        restPrivilegeMockMvc.perform(post("/app/rest/privileges")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(privilege)))
                .andExpect(status().isCreated());

        // Try create a empty Comment
        restPrivilegeMockMvc.perform(post("/app/rest/privileges")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(new Privilege())))
                .andExpect(status().isBadRequest());

        // Read Privilege
        restPrivilegeMockMvc.perform(get("/app/rest/privileges/{id}", DEFAULT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(DEFAULT_ID))
//                .andExpect(jsonPath("$.sampleDateAttribute").value(DEFAULT_SAMPLE_DATE_ATTR.toString()))
                .andExpect(jsonPath("$.name").value(DEFAULT_SAMPLE_NAME_ATTR));

        // Update Privilege
        privilege.setName(UPD_SAMPLE_NAME_ATTR);
        privilege.setReputation(UPD_SAMPLE_REPUTATION_ATTR);

        restPrivilegeMockMvc.perform(put("/app/rest/privileges/{id}", DEFAULT_ID)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(privilege)))
                .andExpect(status().isOk());

        // Read updated Privilege
        restPrivilegeMockMvc.perform(get("/app/rest/privileges/{id}", DEFAULT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(DEFAULT_ID))
//                .andExpect(jsonPath("$.sampleDateAttribute").value(UPD_SAMPLE_DATE_ATTR.toString()))
                .andExpect(jsonPath("$.name").value(UPD_SAMPLE_NAME_ATTR));

        // Delete Privilege
        restPrivilegeMockMvc.perform(delete("/app/rest/privileges/{id}", DEFAULT_ID)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Read nonexisting Privilege
        restPrivilegeMockMvc.perform(get("/app/rest/privileges/{id}", DEFAULT_ID)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNotFound());

    }
}
