package org.kalipo.throttle;

import org.kalipo.web.rest.KalipoException;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;

/**
 * Manages the access stats of a user.
 * Created by damoeb on 22.09.14.
 */
@Singleton
@Service
public class RateLimitService {

    /**
     * Checks access rate constraints
     *
     * @param methodName the unique method name
     * @throws org.kalipo.web.rest.KalipoException if rate limit is exceeded
     */
    public void enter(final String methodName) throws KalipoException {
        // todo implement
    }

    public void exit(final String methodName) {

    }
}
