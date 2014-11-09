package org.kalipo.web.websocket.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.config.managed.Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LiveDTOJacksonDecoder implements Decoder<String, LiveDTO> {

    private static final Logger log = LoggerFactory.getLogger(LiveDTOJacksonDecoder.class);

    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public LiveDTO decode(String jsonString) {
        try {
            return jsonMapper.readValue(jsonString, LiveDTO.class);
        } catch (IOException e) {
            log.error("Error while decoding the String: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
