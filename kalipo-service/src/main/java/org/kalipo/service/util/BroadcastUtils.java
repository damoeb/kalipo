package org.kalipo.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.cpr.BroadcasterFactory;
import org.kalipo.domain.Anonymizable;
import org.kalipo.web.rest.KalipoException;
import org.kalipo.web.websocket.LiveChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by damoeb on 10.12.14.
 */
public final class BroadcastUtils {

    private static final Logger log = LoggerFactory.getLogger(BroadcastUtils.class);

    private static ObjectMapper jsonMapper = new ObjectMapper();

    public static void broadcast(String name, Anonymizable data) throws KalipoException {
        try {
            Asserts.isNotNull(name, "name");
            Asserts.isNotNull(data, "data");
            Wrapper wrapper = new Wrapper(name, data);

            // todo broadcast does not work as expected
            BroadcasterFactory.getDefault().lookup(LiveChannelService.URL, true).broadcast(jsonMapper.writeValueAsString(wrapper));
        } catch (JsonProcessingException e) {
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
