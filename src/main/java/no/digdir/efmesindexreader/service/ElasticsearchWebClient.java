package no.digdir.efmesindexreader.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import no.digdir.efmesindexreader.config.EsIndexReaderProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableRetry
@EnableConfigurationProperties(EsIndexReaderProperties.class)
public class ElasticsearchWebClient {
//Lage ein ny webclient builder som er ei bønne ?  Men den vil jo ha url som endrar seg for kvar gong

    /*@Bean(name = "esWebClient")
    WebClient createEsWebClient(EsIndexReaderProperties properties) {
        return esWebClient(properties);
    }*/

    @Primary
    @Bean(name = "EsWebClient")
    public static WebClient esWebClient(EsIndexReaderProperties properties) {
        return WebClient.builder()
                .exchangeStrategies(getExchangeStrategies())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(TcpClient
                        .create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.connectTimeoutInMs)
                        .doOnConnected(connection -> {
                            connection.addHandlerLast(new ReadTimeoutHandler(properties.readTimeoutInMs, TimeUnit.MILLISECONDS));
                            connection.addHandlerLast(new WriteTimeoutHandler(properties.writeTimeoutInMs, TimeUnit.MILLISECONDS));
                        }))))
                .build();
    }

    /*public ElasticsearchWebClient(EsIndexReaderProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .exchangeStrategies(getExchangeStrategies())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(TcpClient
                        .create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.connectTimeoutInMs)
                        .doOnConnected(connection -> {
                            connection.addHandlerLast(new ReadTimeoutHandler(properties.readTimeoutInMs, TimeUnit.MILLISECONDS));
                            connection.addHandlerLast(new WriteTimeoutHandler(properties.writeTimeoutInMs, TimeUnit.MILLISECONDS));
                        }))))
                .build();
    } */

    private static ExchangeStrategies getExchangeStrategies() {
        ObjectMapper objectMapper = getObjectMapper();
        return ExchangeStrategies.builder()
                .codecs(config -> {
                    config.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                    config.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
                    config.defaultCodecs().maxInMemorySize(32 * 1024 * 1024);
                }).build();
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        objectMapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }
/*
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

    public Flux<EsIndexDTO> getNextScroll(String scrollId) {
        log.trace("Attempting to fetch next scroll with id: {}", scrollId);

        return webClient.get()
                .uri(getNextScrollURI(scrollId))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve()
                .bodyToFlux(EsIndexDTO.class);
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
    } */
}