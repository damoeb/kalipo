package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.kalipo.domain.Notice;
import org.kalipo.repository.NoticeRepository;
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
    private NoticeRepository noticeRepository;

    /**
     * POST  /rest/notices -> Create a new notice.
     */
    @RequestMapping(value = "/rest/notices",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void create(@RequestBody Notice notice) {
        log.debug("REST request to save Notice : {}", notice);
        noticeRepository.save(notice);
    }

    /**
     * GET  /rest/notices -> get all the notices.
     */
    @RequestMapping(value = "/rest/notices",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Notice> getAll() {
        log.debug("REST request to get all Notices");
        return noticeRepository.findAll();
    }

    /**
     * GET  /rest/notices/:id -> get the "id" notice.
     */
    @RequestMapping(value = "/rest/notices/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Notice> get(@PathVariable String id) {
        log.debug("REST request to get Notice : {}", id);
        Notice notice = noticeRepository.findOne(id);
        if (notice == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(notice, HttpStatus.OK);
    }

    /**
     * DELETE  /rest/notices/:id -> delete the "id" notice.
     */
    @RequestMapping(value = "/rest/notices/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void delete(@PathVariable String id) {
        log.debug("REST request to delete Notice : {}", id);
        noticeRepository.delete(id);
    }
}
