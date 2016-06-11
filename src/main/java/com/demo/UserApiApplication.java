package com.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.demo.account.service.ThreadRejectedExecutionHandler;

@SpringBootApplication
@EnableTransactionManagement
@EnableDiscoveryClient
@EnableResourceServer
public class UserApiApplication {

	@Value("${thread.pool.core.size}")
	private Integer coreSize;

	@Value("${thread.pool.max.size}")
	private Integer maxSize;

	public static void main(String[] args) {
		SpringApplication.run(UserApiApplication.class, args);
	}

	@Bean
	ThreadPoolTaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(coreSize);
		threadPoolTaskExecutor.setMaxPoolSize(maxSize);
		threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadRejectedExecutionHandler());
		return threadPoolTaskExecutor;
	}

}
