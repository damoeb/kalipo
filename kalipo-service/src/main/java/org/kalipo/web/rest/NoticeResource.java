package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
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
import java.util.List;

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
    @RequestMapping(value = "/rest/notices/{id}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public Notice update(@PathVariable String id, @RequestBody Notice notice) throws KalipoException {
        log.debug("REST request to update Notice : {}", id);
        Asserts.isNotNull(id, "id");
        Asserts.isNotNull(notice, "payload");

        notice.setId(id);

        return noticeService.update(notice);
    }

    /**
     * GET  /rest/notices/:id -> get the "id" notice.
     */
    @RequestMapping(value = "/rest/notices/{login}/{page}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Notice>> get(@PathVariable String login, @PathVariable int page) {
        log.debug("REST request to get Notice : {}", login);
        return new ResponseEntity<List<Notice>>(noticeService.findByUser(login, page), HttpStatus.OK);
    }
}
