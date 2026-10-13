package com.toneatti.catalogo.synchronization.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CatedraIntegrationProperties.class)
public class CatedraIntegrationConfig {
}
