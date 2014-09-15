package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.kalipo.domain.Report;
import org.kalipo.repository.ReportRepository;
import org.kalipo.web.rest.dto.ReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Report.
 */
@RestController
@RequestMapping("/app")
public class ReportResource {

    private final Logger log = LoggerFactory.getLogger(ReportResource.class);

    @Inject
    private ReportRepository reportRepository;

    /**
     * POST  /rest/reports -> Create a new report.
     */
    @RequestMapping(value = "/rest/reports",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new report")
    public void create(@Valid @RequestBody ReportDTO reportDTO) {
        log.debug("REST request to save Report : {}", reportDTO);

        Report report = new Report();
        BeanUtils.copyProperties(reportDTO, report);

        report.setAuthorId("d"); // todo SecurityUtils.getCurrentLogin() is null during tests
        report.setStatus(Report.Status.PENDING);
        report.setThreadId(1l);

        reportRepository.save(report);
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
    public void update(@PathVariable String id, @Valid @RequestBody ReportDTO reportDTO) throws KalipoRequestException {
        log.debug("REST request to update Report : {}", reportDTO);

        if (StringUtils.isBlank(id)) {
            throw new IllegalParameterException();
        }

        Report report = new Report();
        BeanUtils.copyProperties(reportDTO, report);

        report.setId(id);
        report.setAuthorId("d"); // todo SecurityUtils.getCurrentLogin() is null during tests
        report.setStatus(Report.Status.PENDING);
        report.setThreadId(1l);

        reportRepository.save(report);
    }

    /**
     * GET  /rest/reports -> get all the reports.
     */
    @RequestMapping(value = "/rest/reports",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @ApiOperation(value = "Get all the reports")
    public List<Report> getAll() {
        log.debug("REST request to get all Reports");
        return reportRepository.findAll();
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
    public ResponseEntity<Report> get(@PathVariable String id) throws KalipoRequestException {
        log.debug("REST request to get Report : {}", id);
        if (StringUtils.isBlank(id)) {
            throw new IllegalParameterException();
        }

        return Optional.ofNullable(reportRepository.findOne(id))
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
    public void delete(@PathVariable String id) throws IllegalParameterException {
        log.debug("REST request to delete Report : {}", id);
        if (StringUtils.isBlank(id)) {
            throw new IllegalParameterException();
        }

        reportRepository.delete(id);
    }
}
