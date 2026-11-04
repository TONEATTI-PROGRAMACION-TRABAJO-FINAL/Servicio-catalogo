package com.toneatti.catalogo.synchronization.infrastructure.kafka.config;

import com.toneatti.catalogo.config.CatedraIntegrationProperties;
import com.toneatti.catalogo.synchronization.domain.exception.CatalogSynchronizationPendingException;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraAuthenticationException;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class CatalogUpdatedKafkaConfigTest {

    // Values different from the defaults, so the configuration is seen taking them
    private static final CatedraIntegrationProperties.Kafka KAFKA = new CatedraIntegrationProperties.Kafka(
            "localhost:19092", "alumnos-test", "catedra.catalog.test", "SASL_SSL", "latest",
            Duration.ofSeconds(30), false);
    private static final CatedraIntegrationProperties PROPERTIES = new CatedraIntegrationProperties(
            "http://localhost:8089", "test-jwt", Duration.ofSeconds(2), Duration.ofSeconds(10),
            new CatedraIntegrationProperties.Redis("localhost", 6379, "grp_test", "test-redis-password",
                    "catedra:sync:*", Duration.ofSeconds(2), Duration.ofSeconds(2)),
            KAFKA);

    private final CatalogUpdatedKafkaConfig config = new CatalogUpdatedKafkaConfig();

    // Offsets are committed by the container after processing, never automatically in the background
    @Test
    void consumesTextFromTheConfiguredConnectionWithoutAutoCommit() {
        ConsumerFactory<String, String> consumerFactory = config.catalogUpdatedConsumerFactory(PROPERTIES);

        assertThat(consumerFactory.getConfigurationProperties()).contains(
                entry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092"),
                entry(ConsumerConfig.GROUP_ID_CONFIG, "alumnos-test"),
                entry(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"),
                entry(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false),
                entry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class),
                entry(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class),
                entry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL"));
    }

    @Test
    void commitsTheOffsetOfEachRecordAfterProcessingItAndHonorsListenerEnabled() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = config.kafkaListenerContainerFactory(
                config.catalogUpdatedConsumerFactory(PROPERTIES), PROPERTIES);

        assertThat(factory.getContainerProperties().getAckMode()).isEqualTo(ContainerProperties.AckMode.RECORD);
        // Created without connecting, only to read what the factory gives the containers
        assertThat(factory.createContainer("catedra.catalog.test").isAutoStartup()).isFalse();
    }

    // A rejected JWT stays rejected; the cátedra down or a synchronization running may pass later
    @Test
    void retriesEverythingButARejectedJwt() {
        DefaultErrorHandler errorHandler = CatalogUpdatedKafkaConfig.errorHandler(KAFKA);

        assertThat(errorHandler.removeClassification(CatedraAuthenticationException.class)).isFalse();
        assertThat(errorHandler.removeClassification(CatedraUnavailableException.class)).isNull();
        assertThat(errorHandler.removeClassification(CatalogSynchronizationPendingException.class)).isNull();
    }
}
