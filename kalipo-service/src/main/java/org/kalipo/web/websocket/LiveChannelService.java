package org.kalipo.web.websocket;

import org.atmosphere.config.service.ManagedService;

@ManagedService(
    path = LiveChannelService.URL)
public class LiveChannelService {

    public static final String URL = "/websocket/live/channel";
}
