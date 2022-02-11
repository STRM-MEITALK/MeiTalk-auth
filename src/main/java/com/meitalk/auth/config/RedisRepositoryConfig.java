package com.meitalk.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@RequiredArgsConstructor
@Configuration
@EnableRedisRepositories
public class RedisRepositoryConfig {


    @Value("${spring.redis.port}")
    public int port;
    @Value("${spring.redis.host}")
    public String host;
    @Value("${spring.redis.password}")
    public String password;
    private final Environment env;

    private final RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        String[] profiles = env.getActiveProfiles();
        if (profiles[0].equalsIgnoreCase("local") || profiles[1].equalsIgnoreCase("local") || profiles[2].equalsIgnoreCase("local")) {
            RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
            redisConfiguration.setHostName(host);
//            redisConfiguration.setPassword(password);
            redisConfiguration.setPort(port);
            LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisConfiguration);
            return connectionFactory;
        } else {
            RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
            redisClusterConfiguration.clusterNode(host, port);
//        redisClusterConfiguration.setPassword(RedisPassword.of(password));
            LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisClusterConfiguration);

            return connectionFactory;
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

}
