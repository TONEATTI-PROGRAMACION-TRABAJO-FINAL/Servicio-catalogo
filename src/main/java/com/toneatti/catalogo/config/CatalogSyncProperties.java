package com.toneatti.catalogo.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Loading of the catalog snapshot at startup. When it fails because the cátedra is unavailable, it is retried up to
 * {@code maxAttempts} in total, waiting {@code initialBackoff} and doubling the wait up to {@code maxBackoff}
 * (section 18.4: limited and observable retries). {@code snapshotOnStartup} false skips it, for tests.
 */
@Validated
@ConfigurationProperties(prefix = "catedra.sync")
public record CatalogSyncProperties(
        @DefaultValue("true") boolean snapshotOnStartup,
        @Min(1) @DefaultValue("8") int maxAttempts,
        @NotNull @DurationMin(millis = 1) @DefaultValue("PT2S") Duration initialBackoff,
        @NotNull @DurationMin(millis = 1) @DefaultValue("PT60S") Duration maxBackoff) {
}
