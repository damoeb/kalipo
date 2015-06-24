package org.kalipo.service;

import org.kalipo.aop.KalipoExceptionHandler;
import org.kalipo.config.Constants;
import org.kalipo.domain.Achievement;
import org.kalipo.repository.AchievementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.concurrent.Future;

@Service
@KalipoExceptionHandler
public class AchievementService {

    private final Logger log = LoggerFactory.getLogger(AchievementService.class);

    @Inject
    private AchievementRepository achievementRepository;

    @Async
    public Future<Page<Achievement>> getRevisionsWithPages(String userId, int pageNumber) {
        PageRequest pageable = new PageRequest(pageNumber, 10, Sort.Direction.DESC, Constants.PARAM_CREATED_DATE);
        return new AsyncResult<>(achievementRepository.findByUserId(userId, pageable));
    }

}
