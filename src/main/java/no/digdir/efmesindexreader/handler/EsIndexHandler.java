package no.digdir.efmesindexreader.handler;

import lombok.RequiredArgsConstructor;
import no.digdir.efmesindexreader.service.ElasticsearchIngestService;
import no.digdir.efmesindexreader.service.LoggingProxyService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class EsIndexHandler {
    private final ElasticsearchIngestService elasticsearchIngestService;
    private final LoggingProxyService loggingProxyService;

    public Mono<ServerResponse> getEsIndex(ServerRequest request) {
        elasticsearchIngestService.getLogsFromIndex(request.queryParam("index").get())
            //.subscribe(hit -> System.out.println(hit.getSource()));
            .subscribe(hit -> loggingProxyService.send(hit.getSource()));
        return ServerResponse.ok().bodyValue("OK, fetching index: " + request.queryParam("index").get());
    }

    public Mono<ServerResponse> getAllCollectedIndex(ServerRequest serverRequest) {
        return ServerResponse.ok().bodyValue("i'm a placeholder for a list of collected indexes which requires a DB");
    }
}
