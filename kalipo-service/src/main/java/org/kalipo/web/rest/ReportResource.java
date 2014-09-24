package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.kalipo.domain.Report;
import org.kalipo.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * REST controller for managing Report.
 * <p>
 * todo: on approve: reputation + 5 for every reporter, reputation - 100 for author
 * todo: on reject: reputation - 50 for author
 * todo: on report: iff count > 5 comment will become deleted until peer review
 * todo: peer review reports of users with reputation >= author
 * todo: support updates for approve, reject
 */
@RestController
@RequestMapping("/app")
public class ReportResource {

    private final Logger log = LoggerFactory.getLogger(ReportResource.class);

    @Inject
    private ReportService reportService;

    /**
     * POST  /rest/reports -> Create a new report.
     */
    @RequestMapping(value = "/rest/reports",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new report")
    public void create(@NotNull @RequestBody Report report) throws KalipoRequestException {
        log.debug("REST request to save Report : {}", report);

        reportService.create(report);
    }

    /**
     * PUT  /rest/reports -> Update existing report.
     */
    @RequestMapping(value = "/rest/reports/{id}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Update existing report")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Report not found")
    })
    public void update(@PathVariable String id, @NotNull @RequestBody Report report) throws KalipoRequestException {
        log.debug("REST request to update Report : {}", report);

        if (StringUtils.isBlank(id)) {
            throw new InvalidParameterException("id");
        }

        report.setId(id);

        reportService.update(report);
    }

    /**
     * GET  /rest/reports -> get all the reports.
     */
    @RequestMapping(value = "/rest/reports",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get all the reports")
    public List<Report> getAll() throws ExecutionException, InterruptedException {
        log.debug("REST request to get all Reports");

        return reportService.getAll().get();
    }

    /**
     * GET  /rest/reports/:id -> get the "id" report.
     */
    @RequestMapping(value = "/rest/reports/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get the \"id\" report.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Report not found")
    })
    public ResponseEntity<Report> get(@PathVariable String id) throws KalipoRequestException, ExecutionException, InterruptedException {
        log.debug("REST request to get Report : {}", id);
        if (StringUtils.isBlank(id)) {
            throw new InvalidParameterException("id");
        }

        return Optional.ofNullable(reportService.get(id).get())
                .map(report -> new ResponseEntity<>(
                        report,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /rest/reports/:id -> delete the "id" report.
     */
    @RequestMapping(value = "/rest/reports/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete the \"id\" report")
    public void delete(@PathVariable String id) throws KalipoRequestException {
        log.debug("REST request to delete Report : {}", id);
        if (StringUtils.isBlank(id)) {
            throw new InvalidParameterException("id");
        }

        reportService.delete(id);
    }
}
