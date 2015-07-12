package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.kalipo.config.Constants;
import org.kalipo.domain.Report;
import org.kalipo.service.ReportService;
import org.kalipo.service.util.ParamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * REST controller for managing Report.
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
    public Report create(@NotNull @RequestBody Report report, @Context HttpServletRequest httpServletRequest) throws KalipoException {
        log.debug("REST request to save Report : {}", report);

        report.setIp(httpServletRequest.getRemoteAddr());

        try {
            return reportService.create(report);

        } catch (AuthenticationCredentialsNotFoundException e) {
            return reportService.createAnonymous(report);
        }
    }

    /**
     * PUT  /rest/reports/{id}/approve -> Approve report.
     */
    @RequestMapping(value = "/rest/reports/{id}/approve",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Approve report")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Report not found")
    })
    public void approve(@NotNull @PathVariable String id) throws KalipoException {
        log.debug("REST request to approve Report : {}", id);

        reportService.approve(id);
    }

    /**
     * PUT  /rest/reports/{id}/reject -> Reject report.
     */
    @RequestMapping(value = "/rest/reports/{id}/reject",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Reject report")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Report not found")
    })
    public void reject(@NotNull @PathVariable String id) throws KalipoException {
        log.debug("REST request to reject Report : {}", id);

        reportService.reject(id);
    }

    /**
     * GET  /rest/reports -> get all filtered reports.
     */
    @RequestMapping(value = "/rest/reports",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get filtered the reports")
    public Page<Report> filtered(@QueryParam("status") Report.Status status, @QueryParam("page") @DefaultValue("0") int page) throws ExecutionException, InterruptedException {
        log.debug("REST request to get filtered Reports");

        return reportService.filtered(status, page).get();
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
    public ResponseEntity<Report> get(@PathVariable String id) throws KalipoException, ExecutionException, InterruptedException {
        log.debug("REST request to get Report : {}", id);

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
    public void delete(@PathVariable String id) throws KalipoException {
        log.debug("REST request to delete Report : {}", id);

        reportService.delete(id);
    }
}
