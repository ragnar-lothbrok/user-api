package com.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;
import org.springframework.stereotype.Component;

@Component
public class CustomInMemoryAuthorizationCodeServices extends RandomValueAuthorizationCodeServices {

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@Override
	protected void store(String code, OAuth2Authentication authentication) {
		this.redisTemplate.opsForValue().set(code, authentication);
	}

	@Override
	public OAuth2Authentication remove(String code) {
		Object obj = this.redisTemplate.opsForValue().get(code);
		if (obj != null) {
			this.redisTemplate.delete(code);
			return (OAuth2Authentication) obj;
		}
		return null;
	}

}