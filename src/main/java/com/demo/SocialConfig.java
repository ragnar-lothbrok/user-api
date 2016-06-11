package com.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.google.connect.GoogleConnectionFactory;

@Configuration
public class SocialConfig {
	@Value("${facebook.appID}")
	private String fbAppId;
	@Value("${facebook.appSecret}")
	private String fbAppSecret;
	@Value("${google.appID}")
	private String googleClientId;
	@Value("${google.appSecret}")
	private String googleClientSecret;

	@Bean
	public FacebookConnectionFactory facebookConnectionFactory() {
		return new FacebookConnectionFactory(fbAppId, fbAppSecret);
	}

	@Bean
	public GoogleConnectionFactory googleConnectionFactory() {
		return new GoogleConnectionFactory(googleClientId, googleClientSecret);
	}
}
