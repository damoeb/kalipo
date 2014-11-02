package org.kalipo.throttle;

import org.kalipo.config.ErrorCode;
import org.kalipo.web.rest.KalipoException;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the access stats of a method.
 * Created by damoeb on 22.09.14.
 */
@Singleton
@Service
public class MethodThrottleService {

    private final Map<String, Integer> methods = new HashMap<String, Integer>();

    /**
     * Checks throttle constraints and increases the access count
     *
     * @param methodName the unique method name
     * @param poolSize   max concurrent request allowed
     * @throws org.kalipo.web.rest.KalipoException if request limit is exceeded
     */
    public void initAndEnter(final String methodName, final int poolSize) throws KalipoException {

        if (!methods.containsKey(methodName)) {
            methods.put(methodName, poolSize);
        }

        if (methods.get(methodName) <= 0) {
            throw new KalipoException(ErrorCode.METHOD_REQUEST_LIMIT_REACHED);
        }

        methods.put(methodName, methods.get(methodName) - 1);
    }

    /**
     * Decreases the access count
     *
     * @param methodName the unique method name
     */
    public void exit(final String methodName) {
        if (methods.containsKey(methodName)) {
            methods.put(methodName, methods.get(methodName) + 1);
        }
    }
}
