package com.toneatti.catalogo.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.util.TestPropertyValues;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogSyncPropertiesTest {

    @EnableConfigurationProperties(CatalogSyncProperties.class)
    static class TestConfig {
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void synchronizesOnStartupWithEightAttemptsByDefault() {
        contextRunner.run(context -> {
            CatalogSyncProperties properties = context.getBean(CatalogSyncProperties.class);

            assertThat(properties.onStartup()).isTrue();
            assertThat(properties.maxAttempts()).isEqualTo(8);
            assertThat(properties.initialBackoff()).isEqualTo(Duration.ofSeconds(2));
            assertThat(properties.maxBackoff()).isEqualTo(Duration.ofSeconds(60));
        });
    }

    // The variables documented in application.properties and .env.example, through the same relaxed binding.
    @Test
    void bindsEverySettingFromEnvironmentVariables() {
        contextRunner.withInitializer(context -> TestPropertyValues
                        .of("CATEDRA_SYNC_ON_STARTUP=false", "CATEDRA_SYNC_MAX_ATTEMPTS=3",
                                "CATEDRA_SYNC_INITIAL_BACKOFF=PT0.5S", "CATEDRA_SYNC_MAX_BACKOFF=PT5S")
                        .applyTo(context.getEnvironment(), TestPropertyValues.Type.SYSTEM_ENVIRONMENT))
                .run(context -> assertThat(context.getBean(CatalogSyncProperties.class))
                        .isEqualTo(new CatalogSyncProperties(false, 3, Duration.ofMillis(500), Duration.ofSeconds(5))));
    }

    @Test
    void failsWithoutAtLeastOneAttempt() {
        contextRunner.withPropertyValues("catedra.sync.max-attempts=0")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void failsWhenABackoffIsNotPositive() {
        contextRunner.withPropertyValues("catedra.sync.initial-backoff=PT0S")
                .run(context -> assertThat(context).hasFailed());
    }
}
