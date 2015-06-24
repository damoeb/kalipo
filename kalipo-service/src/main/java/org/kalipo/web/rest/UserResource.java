package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.kalipo.config.Constants;
import org.kalipo.domain.User;
import org.kalipo.security.AuthoritiesConstants;
import org.kalipo.service.UserService;
import org.kalipo.service.util.Asserts;
import org.kalipo.service.util.ParamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/app")
public class UserResource {

    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    @Inject
    private UserService userService;

    /**
     * GET  /rest/users/:login -> get the "login" user.
     */
    @RequestMapping(value = "/rest/users/{login}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get existing user")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "User not found")
    })
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<User> getUser(@PathVariable String login) {
        log.debug("REST request to get User : {}", login);
        return Optional.ofNullable(userService.findOne(login))
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * PUT  /rest/users/:login -> update the "login" user.
     */
    @RequestMapping(value = "/rest/users/{login}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Update existing user")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "User not found")
    })
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<User> updateUser(@PathVariable @NotNull String login, @NotNull @RequestBody User user) throws KalipoException {
        log.debug("REST request to update User : {}", login);
        user.setLogin(login);
        return new ResponseEntity<User>(userService.updateUser(user), HttpStatus.OK);
    }

    /**
     * POST  /rest/users/:login/ban -> ban the "login" user.
     * todo rename "ban", cause this is already used in thread
     */
    @RequestMapping(value = "/rest/users/{login}/ban",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    ResponseEntity<User> ban(@PathVariable String login) throws KalipoException {
        log.debug("REST request to ban User : {}", login);
        return new ResponseEntity<>(userService.ban(login), HttpStatus.OK);
    }


    /**
     * GET  /rest/users/:login/threads -> get the threads of "login" user.
     */
    @RequestMapping(value = "/rest/users/{login}/threads",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get threads of a user")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid ID supplied"),
        @ApiResponse(code = 404, message = "User not found")
    })
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<User> getThreadsOfUser(
        @PathVariable String login,
        @QueryParam(Constants.PARAM_PAGE) Integer page,
        @QueryParam(Constants.PARAM_SORT_FIELD) String sortFieldValue,
        @QueryParam(Constants.PARAM_SORT_ORDER) String sortOrderValue
    ) throws KalipoException {
        log.debug("REST request to get threads of User : {}", login);
        // todo implement
//        List of threads, pagination:yes, sort by newest first/upvotes, type:[thread]

        String defaultSortField = Constants.PARAM_CREATED_DATE;
        final List<String> expectedSortFields = Arrays.asList(defaultSortField, "likes");
        String sortField = Optional.ofNullable(sortFieldValue).filter(expectedSortFields::contains).orElse(defaultSortField);

        String defaultSortOrder = "desc";
        final List<String> expectedSortOrders = Arrays.asList("asc", defaultSortOrder);
        String sortOrder = Optional.ofNullable(sortFieldValue).filter(expectedSortOrders::contains).orElse(defaultSortOrder);


//        Arrays.sort(expectedSortFields);
//        Asserts.isTrue(Arrays.binarySearch(expectedSortFields, sortField) > -1, String.format("%s must be one of %s", Constants.PARAM_SORT_FIELD, expectedSortFields));

        return null;
    }

    /**
     * GET  /rest/users/:login/comments -> get the comments of "login" user.
     */
    @RequestMapping(value = "/rest/users/{login}/comments",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get comments of a user")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid ID supplied"),
        @ApiResponse(code = 404, message = "User not found")
    })
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<User> getCommentsOfUser(
        @PathVariable String login,
        @QueryParam(Constants.PARAM_PAGE) Integer page,
        @QueryParam(Constants.PARAM_SORT_FIELD) String sortField,
        @QueryParam(Constants.PARAM_SORT_ORDER) String sortOrder
    ) {
        log.debug("REST request to get comments of User : {}", login);

        // todo implement
//        List of comments, pagination:yes, sort by newest first/upvotes, type:[comment]
        return null;
    }

    /**
     * GET  /rest/users/:login/likes -> get the likes of "login" user.
     */
    @RequestMapping(value = "/rest/users/{login}/likes",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get likes of a user")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid ID supplied"),
        @ApiResponse(code = 404, message = "User not found")
    })
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<User> getLikesOfUser(
        @PathVariable String login,
        @QueryParam(Constants.PARAM_PAGE) Integer page,
        @QueryParam(Constants.PARAM_SORT_FIELD) String sortField,
        @QueryParam(Constants.PARAM_SORT_ORDER) String sortOrder
    ) {
        log.debug("REST request to get comments of User : {}", login);
        // todo implement
//        List of comments, pagination:yes, sort by newest first/upvotes, type:[comment]
        return null;
    }

    /**
     * GET  /rest/users/:login/notifications -> get the notifications of "login" user.
     */
    @RequestMapping(value = "/rest/users/{login}/notifications",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get notifications of a user")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid ID supplied"),
        @ApiResponse(code = 404, message = "User not found")
    })
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<User> getNotificationsOfUser(
        @PathVariable String login
    ) {
        log.debug("REST request to get notifications of User : {}", login);
        // newest first
        // todo implement
//        List of notifications, pagination:no, limited to 10, sort by newest first, type:[thread,comment]
        return null;
    }

    /**
     * GET  /rest/users/:login/achievements -> get the achievements of "login" user.
     */
    @RequestMapping(value = "/rest/users/{login}/achievements",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get achievements of a user")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid ID supplied"),
        @ApiResponse(code = 404, message = "User not found")
    })
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<User> getAchievementsOfUser(
        @PathVariable String login,
        @QueryParam(Constants.PARAM_PAGE) Integer page
    ) {
        log.debug("REST request to get achievements of User : {}", login);
        // newest first
        // todo implement
//        List of achievements, pagination:yes, sort by newest first, type:[achievement]
        return null;
    }


    /**
     * GET  /rest/users/:login/achievements -> get the achievements of "login" user.
     */
    @RequestMapping(value = "/rest/users/{login}/ignored-users",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get ignored-users of a user")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid ID supplied"),
        @ApiResponse(code = 404, message = "User not found")
    })
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    public ResponseEntity<User> getIgnoredUsersOfUser(
        @PathVariable String login,
        @QueryParam(Constants.PARAM_PAGE) Integer page
    ) {
        log.debug("REST request to get ignored-users of User : {}", login);
        // newest first
        // todo implement
//        List of usernames, pagination:yes, sort by name
        return null;
    }
}
