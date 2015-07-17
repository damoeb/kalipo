package org.kalipo.web.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Message;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.BroadcasterFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.kalipo.web.websocket.dto.ActivityDTO;
import org.kalipo.web.websocket.dto.ActivityDTOJacksonDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Calendar;

@ManagedService(
        path = "/websocket/activity")
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    @Inject
    private BroadcasterFactory broadcasterFactory;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private ObjectMapper jsonMapper = new ObjectMapper();

    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) throws IOException {
        log.debug("Browser {} disconnected", event.getResource().uuid());
        ActivityDTO activityDTO = new ActivityDTO();
        activityDTO.setUuid(event.getResource().uuid());
        activityDTO.setPage("logout");
        String json = jsonMapper.writeValueAsString(activityDTO);
//        broadcasterFactory.lookup("/websocket/tracker", true).broadcast(json);
    }

    @Message(decoders = {ActivityDTOJacksonDecoder.class})
    public void onMessage(AtmosphereResource atmosphereResource, ActivityDTO activityDTO) throws IOException {
        AtmosphereRequest request = atmosphereResource.getRequest();
        activityDTO.setUuid(atmosphereResource.uuid());
        activityDTO.setIpAddress(request.getRemoteAddr());
        activityDTO.setTime(dateTimeFormatter.print(Calendar.getInstance().getTimeInMillis()));
        String json = jsonMapper.writeValueAsString(activityDTO);
        log.debug("Sending user tracking data {}", json);
//        broadcasterFactory.lookup("/websocket/tracker", true).broadcast(json);
    }
}
