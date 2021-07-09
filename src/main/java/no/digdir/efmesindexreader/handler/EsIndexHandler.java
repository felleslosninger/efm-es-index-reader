package no.digdir.efmesindexreader.handler;

import lombok.RequiredArgsConstructor;
import no.digdir.efmesindexreader.domain.data.HitDTO;
import no.digdir.efmesindexreader.service.ElasticsearchIngestService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class EsIndexHandler {
    private final ElasticsearchIngestService service;

    public Mono<ServerResponse> getEsIndex(ServerRequest request) {
        Mono<ServerResponse> index = ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.getLogsFromIndex(request.queryParam("index").get()), HitDTO.class)
                .subscribeOn(Schedulers.boundedElastic());
        System.out.println("Returning serverResponse...");
        return index;
    }

    public Mono<ServerResponse> getTest(ServerRequest serverRequest) {
        return ServerResponse.ok().body("Hi world", String.class);
    }
}
