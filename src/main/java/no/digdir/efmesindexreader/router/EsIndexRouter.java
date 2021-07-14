package no.digdir.efmesindexreader.router;

import no.digdir.efmesindexreader.handler.EsIndexHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Objects;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.queryParam;

@Configuration
public class EsIndexRouter {

    /**
     * The router configuration for EsIndexHandler
     */
    @Bean
    public RouterFunction<ServerResponse> esRoute(EsIndexHandler esIndexHandler) {
        return RouterFunctions
                .route(GET("/esindex")
                                .and(queryParam("index", Objects::nonNull))
                        , esIndexHandler::getEsIndex)
                .andRoute(GET("/esindex/all"), esIndexHandler::getAllCollectedIndex);
    }
}

