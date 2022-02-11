package no.digdir.efmesindexreader.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.digdir.efmesindexreader.service.ElasticsearchIngestService;
import no.digdir.efmesindexreader.service.LoggingProxySender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Component
public class EsIndexReaderConfig {
    private final ElasticsearchIngestService service;
    private final LoggingProxySender loggingProxySender;

    @Scheduled(cron = "${digdir.elasticsearch.schedulerCronExpr}", zone = "Europe/Oslo")
    public void scheduleStatusIndexRead() {
        try {
            String index = getIndexName();
            log.info(("Scheduled read on index %s " + index));
            service.getLogsFromIndex(index)
                    .limitRate(100)
                    .flatMap(hit -> loggingProxySender.send(hit.getSource())
                            .onErrorResume(WebClientRequestException.class, e -> Mono.empty()))
                    .subscribeOn(Schedulers.boundedElastic())
                    .onErrorResume(InternalError.class, it -> Mono.empty()).next()
                    .subscribe(System.out::println);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    public String getIndexName() {
        //Creates a String with the name of the last closed status index. Which will be yesterdays index. i.e status-2022.02.22
        String statusIndex = "status-";
        LocalDate date = LocalDate.now();
        date = date.minusDays(1);
        return statusIndex.concat(date.toString().replace("-", "."));
    }
}
