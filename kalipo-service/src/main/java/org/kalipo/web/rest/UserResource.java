package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.kalipo.config.Constants;
import org.kalipo.domain.User;
import org.kalipo.security.AuthoritiesConstants;
import org.kalipo.service.UserService;
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
}
