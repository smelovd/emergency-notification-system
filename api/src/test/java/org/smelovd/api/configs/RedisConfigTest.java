package org.smelovd.api.configs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DataRedisTest
@AutoConfigureWebTestClient
class RedisConfigTest {

    @InjectMocks
    private RedisConfig redisConfig;

    @BeforeEach
    void setUp() {
    }

    @Test
    void reactiveRedisConnectionFactory() {
        assertInstanceOf(LettuceConnectionFactory.class, redisConfig.reactiveRedisConnectionFactory());
    }
}