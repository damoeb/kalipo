package org.kalipo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.ApiOperation;
import org.kalipo.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;
import java.util.concurrent.ExecutionException;

/**
 * Controller to log outgoing urls per comment.
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

        commentService.logForward(commentId, url, request.getRemoteAddr());

        return "redirect:" + url;
    }

}
