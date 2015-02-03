package org.kalipo.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.cpr.BroadcasterFactory;
import org.kalipo.domain.Vote;
import org.kalipo.web.websocket.LiveChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by damoeb on 10.12.14.
 */
public final class BroadcastUtils {

    private static final Logger log = LoggerFactory.getLogger(BroadcastUtils.class);

    private static ObjectMapper jsonMapper = new ObjectMapper();

    public static void broadcast(Vote vote) {
        try {
            BroadcasterFactory.getDefault().lookup(LiveChannelService.URL, true).broadcast(jsonMapper.writeValueAsString(vote));
        } catch (JsonProcessingException e) {
            log.warn("Failed broadcasting: " + e.getMessage());
        }
    }

}
