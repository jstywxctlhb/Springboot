package com.wang.config.redis;

import com.wang.util.common.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Jedis配置，项目启动注入JedisPool
 */
@Data
@Slf4j
@Configuration
@EnableAutoConfiguration
@PropertySource("classpath:config.properties")
@ConfigurationProperties(prefix = "redis")
public class JedisConfig {

    private String host;

    private int port;

    private String password;

    private int timeout;

    @Value("${redis.pool.max-active}")
    private int maxActive;

    @Value("${redis.pool.max-wait}")
    private int maxWait;

    @Value("${redis.pool.max-idle}")
    private int maxIdle;

    @Value("${redis.pool.min-idle}")
    private int minIdle;

    @Bean
    public JedisPool redisPoolFactory() {
        try {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(maxIdle);
            jedisPoolConfig.setMaxWaitMillis(maxWait);
            jedisPoolConfig.setMaxTotal(maxActive);
            jedisPoolConfig.setMinIdle(minIdle);
            // 密码为空设置为null
            if (StringUtil.isBlank(password)) {
                password = null;
            }
            JedisPool jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, password);
            log.info("初始化Redis连接池JedisPool成功!地址: {}:{}", host, port);
            return jedisPool;
        } catch (Exception e) {
            log.error("初始化Redis连接池JedisPool异常:{}", e.getMessage());
        }
        return null;
    }
}