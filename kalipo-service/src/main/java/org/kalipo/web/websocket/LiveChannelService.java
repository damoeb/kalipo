package org.kalipo.web.websocket;

import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.Heartbeat;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.BroadcasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.PathParam;

@ManagedService(
    path = "/websocket/live/{thread: [a-zA-Z_0-9]+}")
public class LiveChannelService {

    public static final String URL = "/websocket/live/";

    private final Logger log = LoggerFactory.getLogger(LiveChannelService.class);

//    private final ConcurrentHashMap<String, String> users = new ConcurrentHashMap<String, String>();

    @PathParam("thread")
    private String threadId;

//    @Inject
//    private BroadcasterFactory factory;
//
//    @Inject
//    private CommentService commentService;
//
//    @Inject
//    private AtmosphereResourceFactory resourceFactory;
//
//    @Inject
//    private MetaBroadcaster metaBroadcaster;


    // For demonstrating injection.
    @Inject
    private BroadcasterFactory factory;

    // For demonstrating javax.inject.Named
//    @Inject
//    @Named("/chat")
//    private Broadcaster broadcaster;

//    @Inject
//    private AtmosphereResource r;

//    @Inject
//    private AtmosphereResourceEvent event;

    @Heartbeat
    public void onHeartbeat(final AtmosphereResourceEvent event) {
        log.trace("Heartbeat send by {}", event.getResource());
    }

    /**
     * Invoked when the connection as been fully established and suspended, e.g ready for receiving messages.
     */
    @Ready
    public void onReady(AtmosphereResource r) {
        log.info("Browser {} connected", r.uuid());
        log.info("BroadcasterFactory used {}", factory.getClass().getName());
        log.info("Broadcaster injected {}", r.getBroadcaster().getID());
    }

    /**
     * Invoked when the client disconnect or when an unexpected closing of the underlying connection happens.
     */
    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) {
        if (event.isCancelled()) {
            log.info("Browser {} unexpectedly disconnected", event.getResource().uuid());
        } else if (event.isClosedByClient()) {
            log.info("Browser {} closed the connection", event.getResource().uuid());
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
