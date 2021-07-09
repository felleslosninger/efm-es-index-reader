package no.digdir.efmesindexreader.handler;

import lombok.RequiredArgsConstructor;
import no.digdir.efmesindexreader.service.ElasticsearchIngestService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class EsIndexHandler {
    private final ElasticsearchIngestService service;

    public Mono<ServerResponse> getEsIndex(ServerRequest request) {
        service.getLogsFromIndex(request.queryParam("index").get());
        //Subscribe her og gjer det som må gjerast med kall til anna webvcleitn som sender til logging proxy. returner berre ok til brukar.
        return ServerResponse.ok().body("OK", String.class);

    }

    public Mono<ServerResponse> getTest(ServerRequest serverRequest) {
        return ServerResponse.ok().body("Hi world", String.class);
    }
}
