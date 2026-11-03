package com.toneatti.catalogo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Only the periodic catalog synchronization is scheduled, so scheduling is off when it is
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "catedra.sync", name = "poll-enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
