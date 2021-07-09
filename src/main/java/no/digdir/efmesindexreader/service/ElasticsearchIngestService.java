package no.digdir.efmesindexreader.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.digdir.efmesindexreader.domain.data.HitDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchIngestService {
    private final ElasticsearchWebClient client;
    int i = 0;

    public Flux<HitDTO> getLogsFromIndex(String index) {
        return Flux.create(fluxSink -> {
            client.openScrollIndex(index)
                    //.timeout(Duration.ofSeconds(5))
                    //.retryWhen(Retry.backoff(10, Duration.ofSeconds(1)))

                    .doOnError(fluxSink::error)
                    .onErrorResume(Exception.class, ex -> Mono.empty())
                    .subscribe(esDto -> {
                        esDto.getHits().getHitDtoList().forEach(fluxSink::next);
                        getNextScrollFromIndex(esDto.getScrollId(), fluxSink);
                        System.out.println("total is " + esDto.getHits().getTotal());
                    });
        });
    }

    private void getNextScrollFromIndex(String scrollId, FluxSink sink) {
        client.getNextScroll(scrollId)
                //.timeout(Duration.ofSeconds(10))
                //.onErrorResume(Exception.class, ex -> Mono.empty())
                //.retryWhen(Retry.backoff(10, Duration.ofSeconds(1)))
                .doOnError(sink::error)
                .subscribe(esDto -> {
//                    esDto.getHits().getHitDtoList().forEach(HitDTO::getSource);
                    //TODO sende json data til logging proxy endepunkt. Kan dette startast på ein anna tråd så eg slepp vente på det før den går vidare.
                    esDto.getHits().getHitDtoList().forEach(sink::next);
                    if (esDto.getHits().getHitDtoList().isEmpty()) {
                        client.clearScroll(scrollId)
                                //.timeout(Duration.ofSeconds(5))
                                //.retryWhen(Retry.backoff(10, Duration.ofSeconds(1)))
                                .doOnError(sink::error)
                                .subscribe(clearScrollDTO -> {
                                    if (clearScrollDTO.isSucceeded()) {
                                        i+=10000;
                                        log.trace("total loaded from ES index: " + i);
                                        log.trace("Successfully cleared scroll. Ready for another index");
                                            sink.complete();
                                    } else {
                                        sink.error(new Exception("Failed to clear scroll"));
                                    }
                                });
                    } else {
                        i+=esDto.getHits().getHitDtoList().size();
                        System.out.println("tot = " + i);
                        esDto.getHits().setHitDtoList(null);
                        getNextScrollFromIndex(scrollId, sink);
                    }
                });
    }
}

