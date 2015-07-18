package org.kalipo.web.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.config.service.*;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.interceptor.AtmosphereResourceStateRecovery;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.kalipo.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;

@ManagedService(
    path = "/websocket/live/{thread: [a-zA-Z_0-9]+}",
    interceptors = {
        HeartbeatInterceptor.class,
        AtmosphereResourceStateRecovery.class,
    }
)
public class LiveChannelService {

    public static final String URL = "/websocket/live/";

    private final Logger log = LoggerFactory.getLogger(LiveChannelService.class);

    private final ConcurrentHashMap<String, Integer> usersInThread = new ConcurrentHashMap<>();

    @PathParam("thread")
    private String threadId;

    // For demonstrating injection.
    @Inject
    private BroadcasterFactory broadcasterFactory;

    @Heartbeat
    public void onHeartbeat(final AtmosphereResourceEvent event) {
        log.trace("Heartbeat send by {}", event.getResource());
    }

    /**
     * Invoked when the connection as been fully established and suspended, e.g ready for receiving messages.
     */
    @Ready
    public void onReady(AtmosphereResource r) {
        log.debug("Browser {} connected", r.uuid());
        log.debug("BroadcasterFactory used {}", broadcasterFactory.getClass().getName());
        log.debug("Broadcaster injected {}", r.getBroadcaster().getID());
        if (!usersInThread.containsKey(threadId)) {
            usersInThread.put(threadId, 0);
        }
        usersInThread.put(threadId, usersInThread.get(threadId) + 1);
        broadcastStats(r.getBroadcaster());
    }

    /**
     * Invoked when the client disconnect or when an unexpected closing of the underlying connection happens.
     */
    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) {
        if (event.isCancelled()) {
            log.debug("Browser {} unexpectedly disconnected", event.getResource().uuid());
        } else if (event.isClosedByClient()) {
            log.debug("Browser {} closed the connection", event.getResource().uuid());
        }
        if (usersInThread.containsKey(threadId)) {
            usersInThread.put(threadId, Math.max(0, usersInThread.get(threadId) - 1));
            broadcastStats(event.broadcaster());
        }
    }

    private static ObjectMapper jsonMapper = new ObjectMapper();

    private void broadcastStats(Broadcaster broadcaster) {
        try {
            WebSocketService.Wrapper wrapper = new WebSocketService.Wrapper(WebSocketService.Type.STATS.name(), usersInThread.get(threadId));
            broadcaster.broadcast(jsonMapper.writeValueAsString(wrapper));
        } catch (JsonProcessingException e) {
            // nothing
        }
    }

//    /**
//     * Simple annotated class that demonstrate how {@link org.atmosphere.config.managed.Encoder} and {@link org.atmosphere.config.managed.Decoder
//     * can be used.
//     *
//     * @param message an instance of {@link Message}
//     * @return
//     * @throws IOException
//     */
//    @org.atmosphere.config.service.Message(encoders = {JacksonEncoder.class}, decoders = {JacksonDecoder.class})
//    public Message onMessage(Message message) throws IOException {
//        log.info("{} just send {}", message.getAuthor(), message.getMessage());
//        return message;
//    }
}
