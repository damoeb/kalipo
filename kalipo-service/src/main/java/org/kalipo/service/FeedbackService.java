package org.kalipo.service;

import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.aop.Throttled;
import org.kalipo.config.ErrorCode;
import org.kalipo.domain.Feedback;
import org.kalipo.repository.FeedbackRepository;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.kalipo.service.util.Asserts;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.util.List;

@Service
@KalipoExceptionHandler
public class FeedbackService {

    private final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    @Inject
    private UserService userService;

    @Inject
    private FeedbackRepository feedbackRepository;

    @Throttled
    public Feedback create(Feedback feedback) throws KalipoException {

        Asserts.isNull(feedback.getId(), "id");

        return feedbackRepository.save(feedback);
    }

    @RolesAllowed(Privileges.CREATE_TAG)
    public Feedback get(String id) throws KalipoException {
        assertSupermodPrivilege();
        return feedbackRepository.findOne(id);
    }

    public List<Feedback> getAll() throws KalipoException {
        assertSupermodPrivilege();
        return feedbackRepository.findAll();
    }

    public void delete(String id) throws KalipoException {
        assertSupermodPrivilege();
        feedbackRepository.delete(id);
    }

    private void assertSupermodPrivilege() throws KalipoException {
        boolean isSuperMod = userService.isSuperMod(SecurityUtils.getCurrentLogin());
        if (!isSuperMod) {
            throw new KalipoException(ErrorCode.PERMISSION_DENIED);
        }
    }
}
