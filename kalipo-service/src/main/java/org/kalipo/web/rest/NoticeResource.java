package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.json.JSONException;
import org.kalipo.domain.Notice;
import org.kalipo.service.NoticeService;
import org.kalipo.service.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for managing Notice.
 */
@RestController
@RequestMapping("/app")
public class NoticeResource {

    private final Logger log = LoggerFactory.getLogger(NoticeResource.class);

    @Inject
    private NoticeService noticeService;

    /**
     * POST  /rest/notices/{id} -> Update an existing notice.
     */
    @RequestMapping(value = "/rest/notices/{userId}/seen",
        method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void allSeen(@PathVariable String userId) throws KalipoException {
        log.debug("REST request to set Notice of user {} seen", userId);
        Asserts.isNotNull(userId, "id");

        noticeService.setAllSeen(userId);
    }

    /**
     * GET  /rest/notices/:id -> get the "id" notice.
     */
    @RequestMapping(value = "/rest/notices/{login}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Page<Notice>> get(@PathVariable String login, @RequestParam(value = "page", required = false) Integer page) {
        log.debug("REST request to get Notice : {}", login);
        if (page == null) {
            page = 0;
        }
        return new ResponseEntity<Page<Notice>>(noticeService.findByUserWithPages(login, page), HttpStatus.OK);
    }

    /**
     * GET  /rest/notices/:login/unseen -> TRUE, iff user "login" has unseen notices
     */
    @RequestMapping(value = "/rest/notices/{login}/unseen",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Map> hasUnseen(@PathVariable String login) throws JSONException {
        log.debug("REST request to check if {} has unseen Notice", login);
        Map<String, Boolean> response = new HashMap<String, Boolean>();
        response.put("hasUnseen", noticeService.hasUnseen(login));
        return new ResponseEntity<Map>(response, HttpStatus.OK);
    }
}
