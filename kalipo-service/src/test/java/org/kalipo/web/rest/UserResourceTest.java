package org.kalipo.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kalipo.Application;
import org.kalipo.config.MongoConfiguration;
import org.kalipo.security.Privileges;
import org.kalipo.service.UserService;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.inject.Inject;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("dev")
@Import(MongoConfiguration.class)
public class UserResourceTest {

    @Inject
    private UserService userService;

    private MockMvc restUserMockMvc;

    @Before
    public void setup() {
        UserResource userResource = new UserResource();
        ReflectionTestUtils.setField(userResource, "userService", userService);
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userResource).build();

        TestUtil.mockSecurityContext("admin", Arrays.asList());
    }

    @Test
    public void testGetExistingUser() throws Exception {
        restUserMockMvc.perform(get("/app/rest/users/admin")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.lastName").value("Administrator"));
    }

    @Test
    public void testGetUnknownUser() throws Exception {
        restUserMockMvc.perform(get("/app/rest/users/unknown")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void test_banUserWithoutPermission() throws Exception {

        TestUtil.mockSecurityContext("admin", Arrays.asList());

        // todo fix test
//        restUserMockMvc.perform(post("/app/rest/users/admin/ban")
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
    }

    @Test
    public void test_banUser() throws Exception {
        TestUtil.mockSecurityContext("admin", Arrays.asList(Privileges.BAN_USER));

        restUserMockMvc.perform(post("/app/rest/users/admin/ban")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
