package no.digdir.efmesindexreader.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.digdir.efmesindexreader.config.EsIndexReaderProperties;
import no.digdir.efmesindexreader.service.ElasticsearchIngestService;
import no.digdir.efmesindexreader.service.LoggingProxySender;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class EsIndexHandler {
    private final ElasticsearchIngestService elasticsearchIngestService;
    private final LoggingProxySender loggingProxySender;
    private final EsIndexReaderProperties properties;

    public Mono<ServerResponse> getEsIndex(ServerRequest request) {
        if(request.queryParam("index").isPresent()) {
            String esIndex = request.queryParam("index").get();
            elasticsearchIngestService.getLogsFromIndex(esIndex)
                    .limitRate(100)
                    .flatMap(hit -> loggingProxySender.send(hit.getSource())
                            .retryWhen(Retry.fixedDelay(100, Duration.ofSeconds(3)))
                            .onErrorResume(WebClientRequestException.class, wcre ->  {
                                log.error("Error while sending log status message to the logging proxy...", wcre);
                                return Mono.empty();
                            }))
                    .subscribeOn(Schedulers.boundedElastic())
                    .onErrorResume(InternalError.class, it -> Mono.empty()).next()
                    .subscribe(
                            System.out::println,
                            Throwable::printStackTrace,
                            () -> log.info("Finished sending index: {} to logging-proxy", esIndex),
                            s -> s.request(properties.getLoggingProxy().getRequestSize())
                    );
            return ServerResponse.ok().bodyValue("OK, fetching index: " + esIndex);
        } else {
            log.error("Could not find the requested index name.");
            return ServerResponse.notFound().build();
        }
    }

}
