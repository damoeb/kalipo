package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.kalipo.config.Constants;
import org.kalipo.domain.Notification;
import org.kalipo.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.ws.rs.QueryParam;

/**
 * REST controller for managing Notice.
 */
@RestController
@RequestMapping("/app")
public class NotificationResource {

    private final Logger log = LoggerFactory.getLogger(NotificationResource.class);

    @Inject
    private NotificationService notificationService;

    /**
     * GET  /rest/notifications/:id -> get the "id" notice.
     */
    @RequestMapping(value = "/rest/notifications/{userId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get notifications of a user")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid ID supplied"),
        @ApiResponse(code = 404, message = "User not found")
    })
    public ResponseEntity<Page<Notification>> get(
        @PathVariable String userId,
        @QueryParam(Constants.PARAM_PAGE) Integer page
    ) throws KalipoException {

        log.debug("REST request to get Notice : {}", userId);
        if (page == null) {
            page = 0;
        }
        return new ResponseEntity<Page<Notification>>(notificationService.findByUserWithPages(userId, page), HttpStatus.OK);
    }
}
