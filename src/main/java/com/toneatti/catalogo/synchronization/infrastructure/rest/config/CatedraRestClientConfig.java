package com.toneatti.catalogo.synchronization.infrastructure.rest.config;

import com.toneatti.catalogo.config.CatedraIntegrationProperties;
import com.toneatti.catalogo.config.RestClientBuilders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

/**
 * Client for the cátedra REST API. Every request carries the technical JWT (section 4 of the integration contract);
 * it lives only in this bean and is never logged.
 */
@Configuration
public class CatedraRestClientConfig {

    @Bean
    public RestClient catedraRestClient(CatedraIntegrationProperties properties) {
        return RestClientBuilders.withTimeouts(
                        properties.apiBaseUrl(), properties.connectTimeout(), properties.readTimeout())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.jwt())
                .build();
    }
}
