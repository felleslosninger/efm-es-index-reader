package no.digdir.efmesindexreader.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.digdir.efmesindexreader.config.EsIndexReaderProperties;
import no.digdir.efmesindexreader.domain.data.ClearScrollDTO;
import no.digdir.efmesindexreader.domain.data.EsIndexDTO;
import no.digdir.efmesindexreader.domain.data.HitDTO;
import no.digdir.efmesindexreader.domain.data.SourceDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchIngestService {
    @Qualifier("EsWebClient")
    private final WebClient webClient;
    private final EsIndexReaderProperties properties;
    private static List<String> oldStatuses = List.of("AAPNING", "KLAR_FOR_MOTTAK", "POPPET", "LEST_FRA_SERVICEBUS",
            "LEVERING", "VARSLING_FEILET", "KLAR_FOR_PRINT");
    private static final String ARKIVMELDING = "pre:eformidling:2.0:bestedu:arkivmelding";
    private static final String EINNSYN_JOURNALPOST = "pre:eformidling:2.0:bestedu:journalpost";
    private static final String EINNSYN_INNSYN = "pre:eformidling:2.0:bestedu:innsynskrav";
    private static final String DIGITALPOST = "pre:eformidling:2.0:mxa:digitalpost";


    public Flux<HitDTO> getLogsFromIndex(String index) {
        return Flux.create(fluxSink -> {
            openScrollIndex(index)
                    .onErrorResume(WebClientRequestException.class, wcre -> {
                        log.error("Error occurred while trying to access index, skipping index...", wcre);
                        return Mono.empty();
                    })
                    .subscribe(esDto -> {
                        filterStatusAndAddProcessIdentifier(esDto.getHits().getHitDtoList(), fluxSink);
                        getNextScrollFromIndex(esDto.getScrollId(), fluxSink);
                        log.info("Total status-log events in index is: " + esDto.getHits().getTotal());
                    });
        });
    }

    public Flux<EsIndexDTO> openScrollIndex(String index) {
        URI uri = getScrollDownloadURI(index);
        log.trace("Fetching event data from Elasticsearch on URL: {}", uri);
        String initiateScroll = "{\n" +
                "  \"size\": 10000,\n" +
                "  \"query\": {\n" +
                "      \"match\": {\n" +
                "          \"logger_name\": \"STATUS\"\n" +
                "      }\n" +
                "  }\n" +
                "}";

        return webClient.post()
                .uri(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(initiateScroll)
                .retrieve()
                .bodyToFlux(EsIndexDTO.class);
    }

    private void getNextScrollFromIndex(String scrollId, FluxSink fluxSink) {
        getNextScroll(scrollId)
                .onErrorResume(WebClientRequestException.class, wcre -> {
                    log.error("Error occured when fetching the next part of the index... ", wcre);
                    return Mono.empty();
                } )
                .subscribe(esDto -> {
                    filterStatusAndAddProcessIdentifier(esDto.getHits().getHitDtoList(), fluxSink);
                    if (esDto.getHits().getHitDtoList().isEmpty()) {
                        clearScroll(scrollId)
                                .onErrorResume(WebClientRequestException.class, e -> Mono.empty())
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

    private HitDTO setPossiblyMissingProcessIdentifier(HitDTO hit) {
        if (hit.getSource().getProcess_identifier() == null) {
            hit.getSource().setProcess_identifier(findProcessIdentifier(hit.getSource()));
            return hit;
        } else {
            return hit;
        }
    }

    public String findProcessIdentifier(SourceDTO source) {
        switch (source.getService_identifier()) {
            case "DPE_DATA":
            case "DPE":
                return EINNSYN_JOURNALPOST;
            case "DPE_INNSYN":
                return EINNSYN_INNSYN;
            case "DPO":
            case "DPV":
            case "DPF":
                return ARKIVMELDING;
            case "DPI":
                return DIGITALPOST;
            default:
                return "";
        }
    }

    public void filterStatusAndAddProcessIdentifier(List<HitDTO> list, FluxSink fluxSink) {
        list.stream()
                .filter(hit -> !oldStatuses.contains(hit.getSource().getStatus()))
                .map(hit -> setPossiblyMissingProcessIdentifier(hit))
                .forEach(fluxSink::next);
    }

    public Flux<ClearScrollDTO> clearScroll(String scrollId) {
        log.debug("Clearing scroll with id: {}", scrollId);
        return webClient.delete()
                .uri(getDeleteScrollURI(scrollId))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToFlux(ClearScrollDTO.class);
    }

    public Flux<EsIndexDTO> getNextScroll(String scrollId) {
        log.trace("Attempting to fetch next scroll with id: {}", scrollId);

        return webClient.get()
                .uri(getNextScrollURI(scrollId))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToFlux(EsIndexDTO.class);
    }

    @SneakyThrows(URISyntaxException.class)
    public URI getScrollDownloadURI(String index) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(properties.getElasticsearch().getEndpointURL().toURI())
                .path(index + "/" + "_search")
                .queryParam("scroll", "1m")
                .queryParam("pretty");

        URI uri = builder.build().toUri();
        log.trace("Built Elasticsearch Scroll API URL: {}", uri);
        return uri;
    }

    @SneakyThrows(URISyntaxException.class)
    public URI getNextScrollURI(String scrollId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(properties.getElasticsearch().getEndpointURL().toURI())
                .path("_search/scroll")
                .queryParam("scroll", "1m")
                .queryParam("scroll_id", scrollId)
                .queryParam("pretty");

        URI uri = builder.build().toUri();
        log.trace("Built Elasticsearch next scroll URL: {}", uri);
        return uri;
    }

    @SneakyThrows(URISyntaxException.class)
    public URI getDeleteScrollURI(String scrollId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(properties.getElasticsearch().getEndpointURL().toURI())
                .path("_search/scroll")
                .queryParam("scroll_id", scrollId)
                .queryParam("pretty");

        URI uri = builder.build().toUri();
        log.trace("Built Elasticsearch clear scroll URL: {}", uri);
        return uri;
    }

}