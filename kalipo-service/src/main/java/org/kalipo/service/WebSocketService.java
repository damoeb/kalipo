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
        COMMENT, COMMENT_DELETED, VOTE
    }

    @Inject
    private AtmosphereFramework atmosphereFramework;

    private static ObjectMapper jsonMapper = new ObjectMapper();

    public void broadcast(String threadId, Type type, Anonymizable data) throws KalipoException {
        try {
            Asserts.isNotNull(type, "type");
            Asserts.isNotNull(data, "data");
            Wrapper wrapper = new Wrapper(type.name(), data);

//            BroadcasterFactory.lookup(LiveChannelService.URL, true).broadcast(jsonMapper.writeValueAsString(wrapper));
            String url = "/websocket/live/" + threadId;
//            String url = LiveChannelService.URL;
            atmosphereFramework.getBroadcasterFactory().lookup(url, true).broadcast(jsonMapper.writeValueAsString(wrapper));

        } catch (Exception e) {
            log.warn("Failed broadcasting: " + e.getMessage());
        }
    }

    public static class Wrapper {
        private final String threadId;
        private final String type;
        private final Anonymizable event;

        public Wrapper(String type, Anonymizable event) {
            this.type = type;
            this.event = event;
            this.threadId = event.getThreadId();
        }

        public String getThreadId() {
            return threadId;
        }

        public String getType() {
            return type;
        }

        public Anonymizable getEvent() {
            return event;
        }
    }

}
