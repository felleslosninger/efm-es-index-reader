package no.digdir.efmesindexreader.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@EnableWebFluxSecurity
public class EsIndexReaderSecurityConfig {
    @Bean
    @Order(0)
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.securityMatcher(ServerWebExchangeMatchers.anyExchange())
                .cors().and().csrf().disable()
                .authorizeExchange(c -> c.pathMatchers("/actuator/**").permitAll()
                        .anyExchange().authenticated())
                .httpBasic().and().build();
    }
}
