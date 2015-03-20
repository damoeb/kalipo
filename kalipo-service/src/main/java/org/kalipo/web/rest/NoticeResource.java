package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
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
     * POST  /rest/notices/{login}/seen -> Mark all notices of user 'login' as seen.
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
}
