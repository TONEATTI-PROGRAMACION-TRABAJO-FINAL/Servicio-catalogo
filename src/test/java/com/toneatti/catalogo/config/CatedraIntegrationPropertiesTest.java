package com.toneatti.catalogo.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.validation.FieldError;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CatedraIntegrationPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(CatedraIntegrationConfig.class)
            .withPropertyValues(
                    "catedra.api-base-url=http://localhost:8089",
                    "catedra.jwt=test-jwt",
                    "catedra.redis.host=localhost",
                    "catedra.redis.port=6379",
                    "catedra.redis.username=grp_test",
                    "catedra.redis.password=test-redis-password",
                    "catedra.redis.read-namespace=catedra:sync:*",
                    "catedra.kafka.bootstrap-servers=localhost:9092",
                    "catedra.kafka.consumer-group-id=alumnos-test",
                    "catedra.kafka.catalog-topic=catedra.catalog.test");

    @Test
    void bindsAllPropertiesWhenTheyArePresent() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            CatedraIntegrationProperties properties = context.getBean(CatedraIntegrationProperties.class);

            assertThat(properties.apiBaseUrl()).isEqualTo("http://localhost:8089");
            assertThat(properties.jwt()).isEqualTo("test-jwt");
            assertThat(properties.connectTimeout()).isEqualTo(Duration.ofSeconds(2));
            assertThat(properties.readTimeout()).isEqualTo(Duration.ofSeconds(10));
            assertThat(properties.redis().host()).isEqualTo("localhost");
            assertThat(properties.redis().port()).isEqualTo(6379);
            assertThat(properties.redis().username()).isEqualTo("grp_test");
            assertThat(properties.redis().password()).isEqualTo("test-redis-password");
            assertThat(properties.redis().readNamespace()).isEqualTo("catedra:sync:*");
            assertThat(properties.redis().keyPrefix()).isEqualTo("catedra:sync:");
            assertThat(properties.redis().connectTimeout()).isEqualTo(Duration.ofSeconds(2));
            assertThat(properties.redis().commandTimeout()).isEqualTo(Duration.ofSeconds(2));
            assertThat(properties.kafka().bootstrapServers()).isEqualTo("localhost:9092");
            assertThat(properties.kafka().consumerGroupId()).isEqualTo("alumnos-test");
            assertThat(properties.kafka().catalogTopic()).isEqualTo("catedra.catalog.test");
            assertThat(properties.kafka().securityProtocol()).isEqualTo("PLAINTEXT");
            assertThat(properties.kafka().autoOffsetReset()).isEqualTo("earliest");
            assertThat(properties.kafka().retryMaxElapsedTime()).isEqualTo(Duration.ofMinutes(2));
            assertThat(properties.kafka().listenerEnabled()).isTrue();
        });
    }

    @Test
    void failsWhenTheJwtIsMissing() {
        assertFailsOn("jwt", "catedra.jwt=");
    }

    @Test
    void failsWhenTheApiBaseUrlIsMissing() {
        assertFailsOn("apiBaseUrl", "catedra.api-base-url=");
    }

    @Test
    void failsWhenTheApiBaseUrlIsNotAUrl() {
        assertFailsOn("apiBaseUrl", "catedra.api-base-url=not a url");
    }

    @Test
    void failsWhenARedisPropertyIsMissing() {
        assertFailsOn("redis.password", "catedra.redis.password=");
    }

    @Test
    void failsWhenTheRedisPortIsMissing() {
        assertFailsOn("redis.port", "catedra.redis.port=");
    }

    @Test
    void failsWhenTheRedisPortIsOutOfRange() {
        assertFailsOn("redis.port", "catedra.redis.port=70000");
    }

    @Test
    void failsWhenAKafkaPropertyIsMissing() {
        assertFailsOn("kafka.catalogTopic", "catedra.kafka.catalog-topic=");
    }

    @Test
    void failsWhenTheSecurityProtocolIsUnknown() {
        assertFailsOn("kafka.securityProtocol", "catedra.kafka.security-protocol=KERBEROS");
    }

    @Test
    void failsWhenTheAutoOffsetResetIsUnknown() {
        assertFailsOn("kafka.autoOffsetReset", "catedra.kafka.auto-offset-reset=none");
    }

    @Test
    void failsWhenTheRetryTimeIsNotPositive() {
        assertFailsOn("kafka.retryMaxElapsedTime", "catedra.kafka.retry-max-elapsed-time=PT0S");
    }

    @Test
    void bindsTheKafkaConsumerFromEnvironmentVariables() {
        contextRunner.withInitializer(context -> TestPropertyValues
                        .of("CATEDRA_KAFKA_SECURITY_PROTOCOL=SASL_SSL", "CATEDRA_KAFKA_AUTO_OFFSET_RESET=latest",
                                "CATEDRA_KAFKA_RETRY_MAX_ELAPSED_TIME=PT30S", "CATEDRA_KAFKA_LISTENER_ENABLED=false")
                        .applyTo(context.getEnvironment(), TestPropertyValues.Type.SYSTEM_ENVIRONMENT))
                .run(context -> {
                    CatedraIntegrationProperties.Kafka kafka =
                            context.getBean(CatedraIntegrationProperties.class).kafka();

                    assertThat(kafka.securityProtocol()).isEqualTo("SASL_SSL");
                    assertThat(kafka.autoOffsetReset()).isEqualTo("latest");
                    assertThat(kafka.retryMaxElapsedTime()).isEqualTo(Duration.ofSeconds(30));
                    assertThat(kafka.listenerEnabled()).isFalse();
                });
    }

    // The variables documented in application.properties and .env.example, through the same relaxed binding.
    @Test
    void bindsTheTimeoutsFromEnvironmentVariables() {
        contextRunner.withInitializer(context -> TestPropertyValues
                        .of("CATEDRA_CONNECT_TIMEOUT=PT1S", "CATEDRA_READ_TIMEOUT=PT0.5S",
                        "CATEDRA_REDIS_CONNECT_TIMEOUT=PT3S", "CATEDRA_REDIS_COMMAND_TIMEOUT=PT0.25S")
                        .applyTo(context.getEnvironment(), TestPropertyValues.Type.SYSTEM_ENVIRONMENT))
                .run(context -> {
                    CatedraIntegrationProperties properties = context.getBean(CatedraIntegrationProperties.class);

                    assertThat(properties.connectTimeout()).isEqualTo(Duration.ofSeconds(1));
                    assertThat(properties.readTimeout()).isEqualTo(Duration.ofMillis(500));
                    assertThat(properties.redis().connectTimeout()).isEqualTo(Duration.ofSeconds(3));
                    assertThat(properties.redis().commandTimeout()).isEqualTo(Duration.ofMillis(250));
                });
    }

    @Test
    void failsWhenTheReadNamespaceIsNotAKeyPattern() {
        assertFailsOn("redis.readNamespace", "catedra.redis.read-namespace=catedra:sync");
    }

    @Test
    void failsWhenTheRedisCommandTimeoutIsNotPositive() {
        assertFailsOn("redis.commandTimeout", "catedra.redis.command-timeout=PT0S");
    }

    @Test
    void failsWhenTheConnectTimeoutIsNotPositive() {
        assertFailsOn("connectTimeout", "catedra.connect-timeout=PT0S");
    }

    @Test
    void failsWhenTheReadTimeoutIsNotPositive() {
        assertFailsOn("readTimeout", "catedra.read-timeout=PT0S");
    }

    @Test
    void toStringMasksTheSecrets() {
        contextRunner.run(context -> {
            String text = context.getBean(CatedraIntegrationProperties.class).toString();

            assertThat(text).doesNotContain("test-jwt", "test-redis-password");
            assertThat(text).contains("http://localhost:8089", "localhost:9092", "readTimeout=PT10S",
                    "commandTimeout=PT2S");
        });
    }

    private void assertFailsOn(String field, String override) {
        contextRunner.withPropertyValues(override).run(context -> {
            assertThat(context).hasFailed();
            Throwable rootCause = NestedExceptionUtils.getRootCause(context.getStartupFailure());
            assertThat(rootCause).isInstanceOf(BindValidationException.class);
            assertThat(((BindValidationException) rootCause).getValidationErrors().getAllErrors())
                    .extracting(error -> ((FieldError) error).getField())
                    .containsExactly(field);
        });
    }
}
