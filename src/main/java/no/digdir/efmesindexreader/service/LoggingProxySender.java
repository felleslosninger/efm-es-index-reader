package no.digdir.efmesindexreader.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.digdir.efmesindexreader.domain.data.SourceDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingProxySender {
    @Qualifier("LoggingProxyWebClient")
    private final WebClient webClient;

    public void send(SourceDTO source) {
        try {
            sendToLoggingProxy(source)
                    .subscribe(response -> log.info(response.toString()));
        }
        catch (Exception e) {
            log.error("Error occured while sending log event to logging proxy", e);
        }
    }

    /**
     * Sends the log event to the logging proxy application to be put into Kafka. Each source is a log
     * SourceDTO.class describes the object.
     * @param source
     */
    public Mono<JsonNode> sendToLoggingProxy(SourceDTO source) {
        return webClient.post()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(source)
                .retrieve()
                .bodyToMono(JsonNode.class);
    }
}
