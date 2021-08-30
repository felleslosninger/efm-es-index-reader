package no.digdir.efmesindexreader.service;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.digdir.efmesindexreader.config.EsIndexReaderProperties;
import no.digdir.efmesindexreader.domain.data.SourceDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LoggingProxyWebClient {
    private final EsIndexReaderProperties properties;
    private final WebClient webClient;
    private final URI uri;

    public LoggingProxyWebClient(EsIndexReaderProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                //.defaultHeaders(header -> header.setBasicAuth("user", "pw"))
                //.filter(ExchangeFilterFunctions.basicAuthentication("user", "pw"))
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(TcpClient
                        .create(ConnectionProvider.create("provider", 50000))
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.connectTimeoutInMs)
                        .doOnConnected(connection -> {
                            connection.addHandlerLast(new ReadTimeoutHandler(properties.getReadTimeoutInMs(), TimeUnit.MILLISECONDS));
                            connection.addHandlerLast(new WriteTimeoutHandler(properties.getWriteTimeoutInMs(), TimeUnit.MILLISECONDS));
                        }))))
                .build();
        this.uri = getLoggingProxyURI();
    }

    @SneakyThrows(URISyntaxException.class)
    public URI getLoggingProxyURI() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(properties.getLoggingProxy().getEndpointURL().toURI())
                .path("api");

        URI uri = builder.build().toUri();
        log.trace("Built logging proxy URL: {}", uri);
        return uri;
    }

    //TODO Autentisere for å bruke logging proxy. 
    public Mono<ResponseEntity> sendLogEvent(SourceDTO source) {
        Mono<ResponseEntity> responseEntityMono = webClient.post()
                .uri(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .headers(h -> h.setBasicAuth("user", "pw"))
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(source)
                .retrieve()
                .bodyToMono(ResponseEntity.class);
                //.onErrorResume(Exception.class, ex -> Mono.empty());
        //responseEntityMono.subscribe(s -> log.info(s.toString()));
        return responseEntityMono;
    }

}
