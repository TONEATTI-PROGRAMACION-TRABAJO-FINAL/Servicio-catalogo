package com.toneatti.catalogo.config;

import java.time.Duration;

/**
 * The Kafka properties of application-test.properties, for the tests that build {@link CatedraIntegrationProperties}
 * by hand.
 */
public final class CatedraKafkaTestProperties {

    public static final CatedraIntegrationProperties.Kafka KAFKA = new CatedraIntegrationProperties.Kafka(
            "localhost:9092", "alumnos-test", "catedra.catalog.test", "PLAINTEXT", "earliest",
            Duration.ofMinutes(2), false);

    private CatedraKafkaTestProperties() {
    }
}
