package org.kalipo.web.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.Message;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.kalipo.web.websocket.dto.LiveDTO;
import org.kalipo.web.websocket.dto.LiveDTOJacksonDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LiveService {

    private static final Logger log = LoggerFactory.getLogger(LiveService.class);

    private ObjectMapper jsonMapper = new ObjectMapper();

    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) throws IOException {
        log.debug("Browser {} disconnected", event.getResource().uuid());
    }

    @Message(decoders = {LiveDTOJacksonDecoder.class})
    public void onMessage(AtmosphereResource event, LiveDTO liveDTO) throws IOException {
        String json = jsonMapper.writeValueAsString(liveDTO);
        log.debug("Sending user live data {}", json);
//        BroadcasterFactory.getDefault().lookup(LiveChannelService.URL, true).broadcast(json);
    }
}
