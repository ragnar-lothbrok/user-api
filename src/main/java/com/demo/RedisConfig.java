package com.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Caching in enabled via Redis.
 * 
 * @author raghunandangupta
 *
 */
@Configuration
public class RedisConfig {

	private @Value("${redis.host-name}") String redisHostName;
	private @Value("${redis.port}") int redisPort;
	private @Value("${redis.env}") String environemnt;
	private @Value("${redis.sentinel.uri}") String sentinels;

	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		if ("production".equalsIgnoreCase(environemnt)) {
			String sentinel[] = sentinels.split(",");
			RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration().master("mymaster");
			for (String uri : sentinel) {
				sentinelConfig.sentinel(uri.split("~")[0], Integer.parseInt(uri.split("~")[1]));
			}
			return new JedisConnectionFactory(sentinelConfig);
		} else {
			JedisConnectionFactory factory = new JedisConnectionFactory();
			factory.setHostName(redisHostName);
			factory.setPort(redisPort);
			factory.setUsePool(true);
			return factory;
		}
	}

	/**
	 * Don't use Json Serializer as Security Context Object doesn't have default
	 * conastructor.
	 * 
	 * @return
	 */
	@Bean
	RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		return redisTemplate;
	}
}
