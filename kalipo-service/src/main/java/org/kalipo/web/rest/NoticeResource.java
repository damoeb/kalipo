package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.json.JSONException;
import org.kalipo.domain.Notice;
import org.kalipo.service.NoticeService;
import org.kalipo.service.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
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
     * PUT  /rest/notices/{id} -> Update an existing notice.
     */
    @RequestMapping(value = "/rest/notices/{id}/seen",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void allSeen(@PathVariable String userId) throws KalipoException {
        log.debug("REST request to set Notice of user {} seen", userId);
        Asserts.isNotNull(userId, "id");

        // todo test and impl in ui
        noticeService.setAllSeen(userId);
    }

    /**
     * GET  /rest/notices/:id -> get the "id" notice.
     */
    @RequestMapping(value = "/rest/notices/{login}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Notice>> get(@PathVariable String login, @RequestParam(value = "page", required = false) Integer page) {
        log.debug("REST request to get Notice : {}", login);
        if (page == null) {
            page = 0;
        }
        return new ResponseEntity<List<Notice>>(noticeService.findByUser(login, page), HttpStatus.OK);
    }

    /**
     * GET  /rest/notices/:id/unseen -> get the "id" notice.
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
