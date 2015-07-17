package org.kalipo.service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kalipo.domain.Anonymizable;
import org.kalipo.web.rest.KalipoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Broadcast events via websockets
 *
 * Created by damoeb on 10.12.14.
 */
public final class BroadcastUtils {

    private static final Logger log = LoggerFactory.getLogger(BroadcastUtils.class);

    public enum Type {
        COMMENT, COMMENT_DELETED, VOTE
    }

    private static ObjectMapper jsonMapper = new ObjectMapper();

    public static void broadcast(String threadId, Type type, Anonymizable data) throws KalipoException {
        try {
            Asserts.isNotNull(type, "type");
            Asserts.isNotNull(data, "data");
            Wrapper wrapper = new Wrapper(type.name(), data);

            // todo there should be a random delay to increase anonymity

//            BroadcasterFactory.lookup(LiveChannelService.URL, true).broadcast(jsonMapper.writeValueAsString(wrapper));
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
