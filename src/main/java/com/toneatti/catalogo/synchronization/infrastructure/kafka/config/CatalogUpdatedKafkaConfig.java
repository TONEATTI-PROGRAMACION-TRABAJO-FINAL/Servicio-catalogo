package com.toneatti.catalogo.synchronization.infrastructure.kafka.config;

import com.toneatti.catalogo.config.CatedraIntegrationProperties;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.Map;

/**
 * Consumer of the topic of CatalogUpdated, configured from {@link CatedraIntegrationProperties} instead of
 * spring.kafka.*, so the connection data has one validated source (the same as in Servicio-turnos). The value is read
 * as text and parsed by the listener, so an unreadable message never reaches the deserializer. Offsets are committed
 * by the container after each record the listener processed, never automatically: after the synchronization and the
 * eventId were saved (sections 15.1 and 16 of the integration contract).
 * The container factory replaces the one of Spring Boot, so every @KafkaListener uses it.
 */
@Slf4j
@Configuration
public class CatalogUpdatedKafkaConfig {

    static final long INITIAL_RETRY_INTERVAL_MILLIS = 1_000;
    static final double RETRY_MULTIPLIER = 2;
    static final long MAX_RETRY_INTERVAL_MILLIS = 30_000;

    @Bean
    public ConsumerFactory<String, String> catalogUpdatedConsumerFactory(CatedraIntegrationProperties properties) {
        CatedraIntegrationProperties.Kafka kafka = properties.kafka();
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, kafka.consumerGroupId(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafka.autoOffsetReset(),
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, kafka.securityProtocol()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> catalogUpdatedConsumerFactory,
            CatedraIntegrationProperties properties) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(catalogUpdatedConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(errorHandler(properties.kafka()));
        factory.setAutoStartup(properties.kafka().listenerEnabled());
        return factory;
    }

    // Retries the record in place with an exponential backoff, without moving past it, until retryMaxElapsedTime:
    // the cátedra or Redis down, or another synchronization running. Then it is logged with its payload and skipped,
    // and its offset committed: retrying forever would block the partition (section 18.4: limited and observable
    // retries), and the periodic comparison of versions recovers the catalog. A rejected JWT is not retried.
    static DefaultErrorHandler errorHandler(CatedraIntegrationProperties.Kafka kafka) {
        ExponentialBackOff backOff = new ExponentialBackOff(INITIAL_RETRY_INTERVAL_MILLIS, RETRY_MULTIPLIER);
        backOff.setMaxInterval(MAX_RETRY_INTERVAL_MILLIS);
        backOff.setMaxElapsedTime(kafka.retryMaxElapsedTime().toMillis());
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, e) -> {
            if (causedByARejectedJwt(e)) {
                log.error("Skipped the CatalogUpdated at {}-{}@{}, check CATEDRA_JWT: {}", consumerRecord.topic(),
                        consumerRecord.partition(), consumerRecord.offset(), consumerRecord.value(), e);
            } else {
                log.error("Skipped the CatalogUpdated at {}-{}@{} after retrying it for {}: {}",
                        consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(),
                        kafka.retryMaxElapsedTime(), consumerRecord.value(), e);
            }
        }, backOff);
        errorHandler.addNotRetryableExceptions(CatedraAuthenticationException.class);
        return errorHandler;
    }

    private static boolean causedByARejectedJwt(Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof CatedraAuthenticationException) {
                return true;
            }
        }
        return false;
    }
}
