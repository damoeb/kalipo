package org.kalipo.service;

import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.domain.RepRevision;
import org.kalipo.repository.RepRevisionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Future;

@Service
@KalipoExceptionHandler
public class RepRevisionService {

    private final Logger log = LoggerFactory.getLogger(RepRevisionService.class);

    @Inject
    private RepRevisionRepository repRevisionRepository;

    @Async
    public Future<List<RepRevision>> getRevisions(String userId, int pageNumber) {
        PageRequest pageable = new PageRequest(pageNumber, 10, Sort.Direction.DESC, "createdDate");
        // todo this does not work
        return new AsyncResult<>(repRevisionRepository.findByUserId(userId, pageable));
    }

}
