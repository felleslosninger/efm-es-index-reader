package no.digdir.efmesindexreader.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoggingProxyService {
    private final LoggingProxyWebClient webClient;

    /**
     * Sends the log event to the logging proxy application to be put into Kafka. Each source is a log
     * SourceDTO.class describes the object.
     * @param source
     */
    public void send(JsonNode source) {
        webClient.sendLogEvent(source);
    }

}
