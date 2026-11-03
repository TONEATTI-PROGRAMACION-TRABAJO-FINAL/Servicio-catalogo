package com.toneatti.catalogo.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Connection to the cátedra Redis with the user and password of the integration (section 14.1). Defining it here
 * makes Spring Boot back off from its own Redis connection; the StringRedisTemplate it creates uses this one.
 * It connects on the first command, so starting the application does not need Redis.
 */
@Configuration
public class CatedraRedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(CatedraIntegrationProperties properties) {
        CatedraIntegrationProperties.Redis redis = properties.redis();
        RedisStandaloneConfiguration server = new RedisStandaloneConfiguration(redis.host(), redis.port());
        server.setUsername(redis.username());
        server.setPassword(RedisPassword.of(redis.password()));
        // Fail fast while disconnected instead of queueing commands until the command timeout
        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(SocketOptions.builder().connectTimeout(redis.connectTimeout()).build())
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build();
        LettuceClientConfiguration client = LettuceClientConfiguration.builder()
                .commandTimeout(redis.commandTimeout())
                .clientOptions(clientOptions)
                .build();
        return new LettuceConnectionFactory(server, client);
    }
}
