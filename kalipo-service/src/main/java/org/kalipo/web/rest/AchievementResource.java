package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import org.kalipo.domain.Achievement;
import org.kalipo.service.AchievementService;
import org.kalipo.service.util.Asserts;
import org.kalipo.service.util.ParamFixer;
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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * REST controller for managing Vote.
 */
@RestController
@RequestMapping("/app")
public class AchievementResource {

    private final Logger log = LoggerFactory.getLogger(AchievementResource.class);

    @Inject
    private AchievementService achievementService;

    /**
     * GET  /rest/achievements/:userId -> get all the votes.
     */
    @RequestMapping(value = "/rest/achievements/{userId}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get reputation achievements for {user}")
    public ResponseEntity<Page<Achievement>> getAchievements(@Valid @NotNull @PathVariable String userId, @QueryParam("page") Integer page) throws ExecutionException, InterruptedException, KalipoException {

        log.debug("REST request to Achievement of user {}", userId);

        Asserts.isNotNull(userId, "userId");

        return Optional.ofNullable(achievementService.getRevisionsWithPages(userId, ParamFixer.fixPage(page)).get())
            .map(revisions -> new ResponseEntity<>(
                revisions,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
