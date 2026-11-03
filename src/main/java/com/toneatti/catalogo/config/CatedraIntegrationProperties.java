package com.toneatti.catalogo.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Connection data for the cátedra service (sections 3 and 5 of the integration contract) and the timeouts of its
 * REST and Redis clients. The read timeout is longer than in Servicio-turnos because the snapshot is the heaviest response.
 * Values come from environment variables; secrets are masked in toString so they never reach the logs.
 */
@Validated
@ConfigurationProperties(prefix = "catedra")
public record CatedraIntegrationProperties(
        @NotBlank @URL String apiBaseUrl,
        @NotBlank String jwt,
        @NotNull @DurationMin(millis = 1) @DefaultValue("PT2S") Duration connectTimeout,
        @NotNull @DurationMin(millis = 1) @DefaultValue("PT10S") Duration readTimeout,
        @NotNull @Valid Redis redis,
        @NotNull @Valid Kafka kafka) {

    private static final String MASK = "****";

    @Override
    public String toString() {
        return "CatedraIntegrationProperties[apiBaseUrl=" + apiBaseUrl + ", jwt=" + MASK
                + ", connectTimeout=" + connectTimeout + ", readTimeout=" + readTimeout
                + ", redis=" + redis + ", kafka=" + kafka + "]";
    }

    /**
     * {@code readNamespace} is the pattern the ACL lets the integration read (section 14.1), such as
     * {@code catedra:sync:*}; without the asterisk it is the prefix of every catalog key.
     */
    public record Redis(
            @NotBlank String host,
            @NotNull @Min(1) @Max(65535) Integer port,
            @NotBlank String username,
            @NotBlank String password,
            @NotNull @Pattern(regexp = ".+:\\*", message = "must be a key pattern ending in :*") String readNamespace,
            @NotNull @DurationMin(millis = 1) @DefaultValue("PT2S") Duration connectTimeout,
            @NotNull @DurationMin(millis = 1) @DefaultValue("PT2S") Duration commandTimeout) {

        public String keyPrefix() {
            return readNamespace.substring(0, readNamespace.length() - 1);
        }

        @Override
        public String toString() {
            return "Redis[host=" + host + ", port=" + port + ", username=" + username
                    + ", password=" + MASK + ", readNamespace=" + readNamespace
                    + ", connectTimeout=" + connectTimeout + ", commandTimeout=" + commandTimeout + "]";
        }
    }

    public record Kafka(
            @NotBlank String bootstrapServers,
            @NotBlank String consumerGroupId,
            @NotBlank String catalogTopic) {
    }
}
