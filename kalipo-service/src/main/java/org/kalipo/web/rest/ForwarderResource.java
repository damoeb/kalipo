package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.kalipo.domain.Comment;
import org.kalipo.service.CommentService;
import org.kalipo.service.util.Asserts;
import org.kalipo.service.util.ParamFixer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * REST controller for managing Comment.
 * <p>
 */
@RestController
@RequestMapping("/out")
public class ForwarderResource {

    private final Logger log = LoggerFactory.getLogger(ForwarderResource.class);

    @Inject
    private CommentService commentService;

    /**
     * GET  /out
     */
    @RequestMapping(method = RequestMethod.GET)
    @Timed
    @ApiOperation(value = "")
    public Object forward(@QueryParam("url") String url, @QueryParam("commentId") String commentId, final HttpServletRequest request, final HttpServletResponse response) throws ExecutionException, InterruptedException, KalipoException {
        log.debug("REST request to get all Comments, that have to be reviewed");

        commentService.collectForward(commentId, url, request.getRemoteAddr());

        return "redirect:" + url;
    }

}
