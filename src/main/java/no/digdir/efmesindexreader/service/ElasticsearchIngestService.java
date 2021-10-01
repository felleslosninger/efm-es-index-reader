package no.digdir.efmesindexreader.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.digdir.efmesindexreader.domain.data.HitDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchIngestService {
    private final ElasticsearchWebClient client;
    private static List<String> oldStatuses = List.of(new String[]{"AAPNING", "KLAR_FOR_MOTTAK", "POPPET", "LEST_FRA_SERVICEBUS",
            "LEVERING", "VARSLING_FEILET", "KLAR_FOR_PRINT"});

    public Flux<HitDTO> getLogsFromIndex(String index) {
        return Flux.create(fluxSink -> {
            client.openScrollIndex(index)
                    .doOnError(fluxSink::error)
                    .onErrorResume(Exception.class, ex -> Mono.empty())
                    .subscribe(esDto -> {
                        filterOldStatusAndPutInFlux(esDto.getHits().getHitDtoList(), fluxSink);
                        getNextScrollFromIndex(esDto.getScrollId(), fluxSink);
                        log.info("Total status-log events in index is: " + esDto.getHits().getTotal());
                    });
        });
    }

    private void getNextScrollFromIndex(String scrollId, FluxSink fluxSink) {
        client.getNextScroll(scrollId)
                .doOnError(fluxSink::error)
                .subscribe(esDto -> {
                    filterOldStatusAndPutInFlux(esDto.getHits().getHitDtoList(), fluxSink);
                    if (esDto.getHits().getHitDtoList().isEmpty()) {
                        client.clearScroll(scrollId)
                                .doOnError(fluxSink::error)
                                .subscribe(clearScrollDTO -> {
                                    if (clearScrollDTO.isSucceeded()) {
                                        log.trace("Successfully cleared scroll. Ready for another index");
                                        fluxSink.complete();
                                    } else {
                                        fluxSink.error(new Exception("Failed to clear scroll"));
                                    }
                                });
                    } else {
                        esDto.getHits().setHitDtoList(null);
                        getNextScrollFromIndex(scrollId, fluxSink);
                    }
                });
    }

    public void filterOldStatusAndPutInFlux(List<HitDTO> list, FluxSink fluxSink) {
        list.stream()
                .filter(hit -> !oldStatuses.contains(hit.getSource().getStatus()))
                .forEach(fluxSink::next);
    }

}