package org.kalipo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.cpr.AtmosphereFramework;
import org.kalipo.domain.Anonymizable;
import org.kalipo.service.util.Asserts;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Broadcast events via websockets
 *
 * Created by damoeb on 10.12.14.
 */
@Service
public class WebSocketService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);

    public enum Type {
        COMMENT, COMMENT_DELETED, VOTE, STATS
    }

    @Inject
    private AtmosphereFramework atmosphereFramework;

    private static ObjectMapper jsonMapper = new ObjectMapper();

    public void broadcast(String threadId, Type type, Anonymizable data) throws KalipoException {
        try {
            Asserts.isNotNull(type, "type");
            Asserts.isNotNull(data, "data");
            Wrapper wrapper = new Wrapper(type.name(), data);

            String url = "/websocket/live/" + threadId;
            atmosphereFramework.getBroadcasterFactory().lookup(url, true).broadcast(jsonMapper.writeValueAsString(wrapper));

        } catch (Exception e) {
            log.warn("Failed broadcasting: " + e.getMessage());
        }
    }

    public static class Wrapper {
        private final String type;
        private final Object data;

        public Wrapper(String type, Object data) {
            this.type = type;
            this.data = data;
        }

        public String getType() {
            return type;
        }

        public Object getData() {
            return data;
        }
    }

}
