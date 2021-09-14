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

    public Flux<HitDTO> getLogsFromIndex(String index) {
        return Flux.create(fluxSink -> {
            client.openScrollIndex(index)
                    .doOnError(fluxSink::error)
                    .onErrorResume(Exception.class, ex -> Mono.empty())
                    .subscribe(esDto -> {
                        esDto.getHits().getHitDtoList().forEach(fluxSink::next);
                        getNextScrollFromIndex(esDto.getScrollId(), fluxSink);
                        log.info("Total status-log events in index is: " + esDto.getHits().getTotal());
                    });
        });
    }

    private void getNextScrollFromIndex(String scrollId, FluxSink sink) {
        client.getNextScroll(scrollId)
                .doOnError(sink::error)
                .subscribe(esDto -> {
                    esDto.getHits().getHitDtoList().forEach(sink::next);
                    if (esDto.getHits().getHitDtoList().isEmpty()) {
                        client.clearScroll(scrollId)
                                .doOnError(sink::error)
                                .subscribe(clearScrollDTO -> {
                                    if (clearScrollDTO.isSucceeded()) {
                                        log.trace("Successfully cleared scroll. Ready for another index");
                                        sink.complete();
                                    } else {
                                        sink.error(new Exception("Failed to clear scroll"));
                                    }
                                });
                    } else {
                        esDto.getHits().setHitDtoList(null);
                        getNextScrollFromIndex(scrollId, sink);
                    }
                });
    }
}