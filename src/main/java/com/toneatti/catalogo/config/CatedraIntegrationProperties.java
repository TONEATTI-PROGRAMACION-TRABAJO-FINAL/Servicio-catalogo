package com.toneatti.catalogo.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Connection data for the cátedra service (sections 3 and 5 of the integration contract).
 * Values come from environment variables; secrets are masked in toString so they never reach the logs.
 */
@Validated
@ConfigurationProperties(prefix = "catedra")
public record CatedraIntegrationProperties(
        @NotBlank @URL String apiBaseUrl,
        @NotBlank String jwt,
        @NotNull @Valid Redis redis,
        @NotNull @Valid Kafka kafka) {

    private static final String MASK = "****";

    @Override
    public String toString() {
        return "CatedraIntegrationProperties[apiBaseUrl=" + apiBaseUrl + ", jwt=" + MASK
                + ", redis=" + redis + ", kafka=" + kafka + "]";
    }

    public record Redis(
            @NotBlank String host,
            @NotNull @Min(1) @Max(65535) Integer port,
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String readNamespace) {

        @Override
        public String toString() {
            return "Redis[host=" + host + ", port=" + port + ", username=" + username
                    + ", password=" + MASK + ", readNamespace=" + readNamespace + "]";
        }
    }

    public record Kafka(
            @NotBlank String bootstrapServers,
            @NotBlank String consumerGroupId,
            @NotBlank String catalogTopic) {
    }
}
